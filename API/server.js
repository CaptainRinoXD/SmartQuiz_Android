const express = require('express');
const multer = require('multer');
const mammoth = require('mammoth');
const { v4: uuidv4 } = require('uuid');

const app = express();
const PORT = process.env.PORT || 3000;

// Configure multer for file uploads
const storage = multer.memoryStorage();
const upload = multer({
    storage: storage,
    fileFilter: (req, file, cb) => {
        // Log the received file mimetype for debugging
        console.log(`Received file: ${file.originalname}, Mimetype: ${file.mimetype}`);

        // Accept only .doc and .docx files
        const allowedTypes = [
            'application/msword',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
            'application/octet-stream'
        ];
        if (allowedTypes.includes(file.mimetype)) {
            cb(null, true);
        } else {
            cb(new Error('Only Microsoft Word documents are allowed!'), false);
        }
    },
    limits: {
        fileSize: 10 * 1024 * 1024 // 10MB limit
    }
});

// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// CORS middleware
app.use((req, res, next) => {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
    res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    next();
});

/**
 * Parse Vietnamese quiz document content - SUPER ROBUST VERSION
 * Handles all kinds of formatting inconsistencies
 */
function parseQuizContent(text) {
    console.log('=== DEBUG: Raw extracted text ===');
    console.log('Raw text:', JSON.stringify(text));
    console.log('================================');
    
    const questions = [];
    // Clean up the text first - remove backslashes and normalize line breaks
    const cleanText = text.replace(/\\\s*$/gm, '').replace(/\r\n/g, '\n').replace(/\r/g, '\n');
    const lines = cleanText.split('\n').map(line => line.trim()).filter(line => line.length > 0);
    
    console.log('=== DEBUG: Lines after split ===');
    lines.forEach((line, index) => {
        console.log(`Line ${index}: "${line}"`);
    });
    console.log('===============================');
    
    let currentQuestion = null;
    
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        console.log(`\n=== Processing line ${i}: "${line}" ===`);
        
        // Check if line starts with "Câu" (Question) - handle both "đáp án" and "Đáp án"
        const questionRegex = /^Câu\s+\d+:/i;
        if (line.match(questionRegex)) {
            console.log('✓ Found question line');
            
            // Save previous question if exists
            if (currentQuestion && currentQuestion.answers && currentQuestion.answers.length > 0) {
                console.log('✓ Saving previous question:', currentQuestion.questionText);
                questions.push(currentQuestion);
            }
            
            // Extract question text and correct answer - handle both "đáp án" and "Đáp án"
            const questionMatch = line.match(/^Câu\s+\d+:\s*(.+?)\s*\([Đđ]áp án\s+([A-D])\)/i);
            if (questionMatch) {
                const questionText = questionMatch[1].trim();
                const correctAnswerLetter = questionMatch[2].toUpperCase();
                
                console.log('✓ Question text:', questionText);
                console.log('✓ Correct answer:', correctAnswerLetter);
                
                const correctAnswerIndex = correctAnswerLetter.charCodeAt(0) - 'A'.charCodeAt(0);
                
                currentQuestion = {
                    id: require('uuid').v4(),
                    questionText: questionText,
                    answers: [],
                    correctAnswerIndex: correctAnswerIndex
                };
                console.log('✓ Created question object');
            } else {
                console.log('✗ Failed to match question pattern');
                console.log('  Trying alternative patterns...');
                
                // Try alternative pattern without parentheses
                const altMatch = line.match(/^Câu\s+\d+:\s*(.+)/i);
                if (altMatch) {
                    console.log('✓ Found question with alternative pattern');
                    currentQuestion = {
                        id: require('uuid').v4(),
                        questionText: altMatch[1].trim(),
                        answers: [],
                        correctAnswerIndex: 0 // Default to A if not found
                    };
                }
            }
        }
        // Check if line contains answer options - VERY FLEXIBLE REGEX
        else if (currentQuestion && line.match(/[A-D]\s*\./)) {
            console.log('✓ Found answer line');
            
            // SUPER flexible answer pattern that handles:
            // A.text, A. text, A .text, A . text
            // Fixed to properly handle cases where answer text contains A, B, C, D letters
            const answerPattern = /([A-D])\s*\.\s*(.*?)(?=\s+[A-D]\s*\.|$)/g;
            let answerMatch;
            const tempAnswers = [];
            
            // Reset regex lastIndex to ensure it starts from beginning
            answerPattern.lastIndex = 0;
            
            console.log('  Trying to extract answers from:', line);
            
            while ((answerMatch = answerPattern.exec(line)) !== null) {
                const answerLetter = answerMatch[1];
                const answerText = answerMatch[2].trim();
                console.log(`  Found answer: ${answerLetter} = "${answerText}"`);
                if (answerText) {
                    tempAnswers.push(answerText);
                }
            }
            
            // If regex didn't work, try manual parsing
            if (tempAnswers.length === 0) {
                console.log('  Regex failed, trying manual parsing...');
                // Split by spaces and look for A. B. C. D. patterns
                const parts = line.split(/\s+/);
                let currentAnswer = '';
                let currentLetter = '';
                
                for (let j = 0; j < parts.length; j++) {
                    const part = parts[j];
                    // Check if this part starts with A. B. C. or D.
                    const letterMatch = part.match(/^([A-D])\s*\.(.*)$/);
                    if (letterMatch) {
                        // Save previous answer if exists
                        if (currentLetter && currentAnswer.trim()) {
                            tempAnswers.push(currentAnswer.trim());
                            console.log(`  Manual: ${currentLetter} = "${currentAnswer.trim()}"`);
                        }
                        // Start new answer
                        currentLetter = letterMatch[1];
                        currentAnswer = letterMatch[2] || '';
                    } else if (currentLetter) {
                        // Continue building current answer
                        currentAnswer += ' ' + part;
                    }
                }
                // Don't forget the last answer
                if (currentLetter && currentAnswer.trim()) {
                    tempAnswers.push(currentAnswer.trim());
                    console.log(`  Manual: ${currentLetter} = "${currentAnswer.trim()}"`);
                }
            }
            
            if (tempAnswers.length > 0) {
                currentQuestion.answers = tempAnswers;
                console.log('✓ Added answers:', tempAnswers);
            } else {
                console.log('✗ No answers extracted from line');
            }
        }
        else {
            console.log('✗ Line not recognized as question or answer');
        }
    }
    
    // Don't forget the last question
    if (currentQuestion && currentQuestion.answers && currentQuestion.answers.length > 0) {
        console.log('✓ Saving last question:', currentQuestion.questionText);
        questions.push(currentQuestion);
    }
    
    console.log(`\n=== FINAL RESULT: Found ${questions.length} questions ===`);
    questions.forEach((q, index) => {
        console.log(`Question ${index + 1}: "${q.questionText}"`);
        console.log(`  Answers: ${q.answers.join(', ')}`);
        console.log(`  Correct: ${q.correctAnswerIndex} (${String.fromCharCode(65 + q.correctAnswerIndex)})`);
    });
    
    return questions;
}

/**
 * Validate parsed questions
 */
function validateQuestions(questions) {
    const errors = [];
    const validQuestions = [];
    
    questions.forEach((question, index) => {
        const questionErrors = [];
        
        if (!question.questionText || question.questionText.trim().length === 0) {
            questionErrors.push('Missing question text');
        }
        
        if (!question.answers || question.answers.length === 0) {
            questionErrors.push('No answers found');
        } else if (question.answers.length !== 4) {
            questionErrors.push(`Expected 4 answers, found ${question.answers.length}`);
        }
        
        if (question.correctAnswerIndex < 0 || question.correctAnswerIndex >= (question.answers ? question.answers.length : 0)) {
            questionErrors.push('Invalid correct answer index');
        }
        
        if (questionErrors.length > 0) {
            errors.push({
                questionNumber: index + 1,
                errors: questionErrors
            });
        } else {
            validQuestions.push(question);
        }
    });
    
    return { errors, validQuestions };
}

// Main API endpoint for parsing quiz documents
app.post('/api/parse-quiz', upload.single('document'), async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({
                success: false,
                message: 'No document file uploaded',
                questions: []
            });
        }

        console.log('Processing file:', req.file.originalname);

        // Extract text from Word document
        const result = await mammoth.extractRawText({ buffer: req.file.buffer });
        const documentText = result.value;

        console.log('Document text length:', documentText.length);

        // Parse questions from text
        const parsedQuestions = parseQuizContent(documentText);
        
        if (parsedQuestions.length === 0) {
            return res.status(400).json({
                success: false,
                message: 'No valid questions found in the document. Please check the format.',
                questions: [],
                formatExample: {
                    line1: "Câu 1: What is 1+1? (đáp án A)",
                    line2: "A.2\tB.3\tC.4\tD.5"
                }
            });
        }

        // Validate questions and separate valid from invalid ones
        const { errors, validQuestions } = validateQuestions(parsedQuestions);

        // Return response with questions array (matching your Android List<Question>)
        const response = {
            success: true,
            message: `Found ${validQuestions.length} valid questions`,
            totalFound: parsedQuestions.length,
            validCount: validQuestions.length,
            questions: validQuestions // This is your List<Question> format
        };

        // Include validation errors if any exist, but still return valid questions
        if (errors.length > 0) {
            response.warnings = errors;
            response.message += ` (${errors.length} questions had issues)`;
        }

        res.json(response);

    } catch (error) {
        console.error('Error processing document:', error);
        res.status(500).json({
            success: false,
            message: 'Error processing document: ' + error.message,
            questions: []
        });
    }
});

// Test endpoint
app.get('/api/test', (req, res) => {
    res.json({
        success: true,
        message: 'Quiz parser API is running',
        timestamp: new Date().toISOString(),
        endpoints: {
            parse: 'POST /api/parse-quiz (upload document file)',
            test: 'GET /api/test'
        }
    });
});

// Health check endpoint
app.get('/health', (req, res) => {
    res.json({
        status: 'healthy',
        timestamp: new Date().toISOString()
    });
});

// Error handling middleware
app.use((error, req, res, next) => {
    if (error instanceof multer.MulterError) {
        if (error.code === 'LIMIT_FILE_SIZE') {
            return res.status(400).json({
                success: false,
                message: 'File too large. Maximum size is 10MB.',
                questions: []
            });
        }
    }
    
    res.status(500).json({
        success: false,
    });
});

// 404 handler
app.use('*', (req, res) => {
    res.status(404).json({
        success: false,
        message: 'Endpoint not found',
        availableEndpoints: [
            'POST /api/parse-quiz',
            'GET /api/test',
            'GET /health'
        ]
    });
});

app.listen(PORT, () => {
    console.log(`\n🚀 Quiz Parser API Server Started`);
    console.log(`📍 Server running on: http://localhost:${PORT}`);
    console.log(`🔍 Test endpoint: http://localhost:${PORT}/api/test`);
    console.log(`📄 Parse endpoint: http://localhost:${PORT}/api/parse-quiz`);
    console.log(`💚 Health check: http://localhost:${PORT}/health\n`);
});