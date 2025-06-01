package com.example.smartquiz.ServiceModelAPI;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import okhttp3.MultipartBody;

public interface QuizParserService {
    @Multipart
    @POST("api/parse-quiz")
    Call<QuizParseResponse> parseQuizDocument(@Part MultipartBody.Part document);
}