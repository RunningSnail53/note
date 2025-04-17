package edu.hebut.retrofittest.chat.service;

import java.util.List;
import java.util.Map;

import edu.hebut.retrofittest.chat.model.GenerateResponse;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ChatApi {

    /*@FormUrlEncoded*/
    @POST("/api/chat/stream")
    Call<ResponseBody> sendMessageStream(@Body Map<String ,String> params);

    /*@FormUrlEncoded*/
    @POST("/api/mind-analysis/stream")
    Call<ResponseBody> getSummaryStream(@Body Map<String,Long> params);

    // 生成文案和
    @Multipart
    @POST("/api/image-caption/generate")
    Call<GenerateResponse> generateCaption(@Part List<MultipartBody.Part> partList);

    // 生成文案和图片
    @Multipart
    @POST("/api/image-caption/generateImageAndCaption")
    Call<GenerateResponse> generateImageAndCaption(@Part List<MultipartBody.Part> partList);


}