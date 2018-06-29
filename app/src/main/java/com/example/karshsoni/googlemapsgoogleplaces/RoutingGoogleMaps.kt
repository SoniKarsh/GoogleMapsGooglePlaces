package com.example.karshsoni.googlemapsgoogleplaces

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface RoutingGoogleMaps {
    @GET("/maps/api/directions/json")
    fun sendRequestForRouting(@Query("origin") origin: String, @Query("destination") destination: String
                              , @Query("key") key: String) : Call<GoogleApiResponseModel>

    @GET
    fun sendRequest(@Url url: String) : Call<GoogleApiResponseModel>
}