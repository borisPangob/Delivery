package com.boris.delivery.deliveryInterface;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;
public interface OpenRouteServiceAPI {
    @GET
    Call<String> getRoute(@Url String url);
}
