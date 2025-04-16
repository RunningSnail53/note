package edu.hebut.retrofittest.chat.service;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ChatApi {

    /*@FormUrlEncoded*/
    @POST("/api/chat/stream")
    Call<ResponseBody> sendMessageStream(@Body Map<String ,String> params);

    /*@FormUrlEncoded*/
    @POST("/api/mind-analysis/stream")
    Call<ResponseBody> getSummaryStream(@Body Map<String,Long> params);

}