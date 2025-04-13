package edu.hebut.retrofittest.assistant.service;

import edu.hebut.retrofittest.assistant.entity.ChatRequest;
import edu.hebut.retrofittest.assistant.entity.ChatResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ChatApi {
    @Headers("Content-Type: application/json")
    @POST("/chat")
    Call<ChatResponse> sendMessage(@Body ChatRequest request);
}