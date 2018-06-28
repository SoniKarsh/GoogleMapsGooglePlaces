package com.example.karshsoni.googlemapsgoogleplaces

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RoutingGoogleMaps {
    @GET("/maps/api/directions/json")
    fun sendRequestForRouting(@Query("origin") origin: String, @Query("destination") destination: String
                              , @Query("key") key: String) : Call<MapsDirectionModelClass>
}