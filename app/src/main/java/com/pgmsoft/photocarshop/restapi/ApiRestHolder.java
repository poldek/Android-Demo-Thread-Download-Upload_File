package com.pgmsoft.photocarshop.restapi;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface ApiRestHolder {
    @Headers("Accept: application/json")
    @GET("image/thumbs")
    Call<ImageRestHandler> getImagePacjent();
}

