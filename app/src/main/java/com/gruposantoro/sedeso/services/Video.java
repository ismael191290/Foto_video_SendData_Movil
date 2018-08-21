package com.gruposantoro.sedeso.services;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface Video {

    @Multipart
    @POST("ServletVideo")
    Call<ResponseBody> uploadVideo(
            @Part("nombre") RequestBody nombre,
            @Part("domicilio") RequestBody domicilio,
            @Part("latitud") RequestBody lat,
            @Part("longitud") RequestBody lang,
            @Part("metros") RequestBody metros,
            @Part("observa") RequestBody observa,
            @Part("fecha") RequestBody fecha,
            @Part MultipartBody.Part video
    );
}
