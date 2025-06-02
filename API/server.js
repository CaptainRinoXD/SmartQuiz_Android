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
 * Parse Vietnamese quiz document content
 * Expected format: Câu X: question text (đáp án Y)
 * A.answer1    B.answer2    C.answer3    D.answer4
 */
function parseQuizContent(text) {
    const questions = [];
    const lines = text.split('\n').map(line => line.trim()).filter(line => line.length > 0);
    
    let currentQuestion = null;
    
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        
        // Check if line starts with "Câu" (Question)
        if (line.match(/^Câu\s+\d+:/)) {
            // Save previous question if exists
            if (currentQuestion && currentQuestion.answers && currentQuestion.answers.length > 0) {
                questions.push(currentQuestion);
            }
            
            // Extract question text and correct answer
            const questionMatch = line.match(/^Câu\s+\d+:\s*(.+?)\s*\(đáp án\s+([A-D])\)/i);
            if (questionMatch) {
                const questionText = questionMatch[1].trim();
                const correctAnswerLetter = questionMatch[2].toUpperCase();
                
                // Convert letter to index (A=0, B=1, C=2, D=3)
                const correctAnswerIndex = correctAnswerLetter.charCodeAt(0) - 'A'.charCodeAt(0);
                
                currentQuestion = {
                    id: uuidv4(),
                    questionText: questionText,
                    answers: [],
                    correctAnswerIndex: correctAnswerIndex
                };
            }
        }
        // Check if line contains answer options (A. B. C. D.)
        else if (currentQuestion && line.match(/[A-D]\./)) {
            // Parse answers from line like "A.answer1      B.answer2    C.answer3    D.answer4"
            const answerPattern = /([A-D])\.\s*([^A-D]*?)(?=\s*[A-D]\.|$)/g;
            let answerMatch;
            const tempAnswers = [];
            
            while ((answerMatch = answerPattern.exec(line)) !== null) {
                const answerText = answerMatch[2].trim();
                if (answerText) {
                    tempAnswers.push(answerText);
                }
            }
            
            if (tempAnswers.length > 0) {
                currentQuestion.answers = tempAnswers;
            }
        }
        // Handle case where answers might be on separate lines
        else if (currentQuestion && currentQuestion.answers.length < 4) {
            const singleAnswerMatch = line.match(/^([A-D])\.\s*(.+)/);
            if (singleAnswerMatch) {
                const answerText = singleAnswerMatch[2].trim();
                if (answerText) {
                    currentQuestion.answers.push(answerText);
                }
            }
        }
    }
    
    // Don't forget the last question
    if (currentQuestion && currentQuestion.answers && currentQuestion.answers.length > 0) {
        questions.push(currentQuestion);
    }
    
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
        message: 'Internal server error: ' + error.message,
        questions: []
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