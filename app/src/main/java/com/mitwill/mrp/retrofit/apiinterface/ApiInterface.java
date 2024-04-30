package com.mitwill.mrp.retrofit.apiinterface;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiInterface {

//    @Multipart
//    @POST("upload-image")
//    Call<String> uploadImage(@Part MultipartBody.Part[] surveyImage,
//                             @Part("work_order") RequestBody workOrderId,
//                             @Part("user_name") RequestBody username,
//                             @Part("password") RequestBody password);


    @Multipart
    @POST("upload-image")
    Call<Object> uploadImage(@Part MultipartBody.Part[] imageAttachment,
                                   @Part("work_order") RequestBody batchOrderId,
                                   @Part("uid") RequestBody userId);
    @Multipart
    @POST("upload-image")
    Call<Object> uploadImageBatch(@Part MultipartBody.Part[] imageAttachment,
                             @Part("batch_order") RequestBody batchOrderId,
                             @Part("uid") RequestBody userId);
    @Multipart
    @POST("upload-image")
    Call<Object> uploadImageManufacture(@Part MultipartBody.Part[] imageAttachment,
                                  @Part("manufacturing_order") RequestBody batchOrderId,
                                  @Part("uid") RequestBody userId);


}
