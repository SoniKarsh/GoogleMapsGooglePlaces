package com.example.karshsoni.googlemapsgoogleplaces


import com.google.gson.annotations.SerializedName


data class Northeast(

	@field:SerializedName("lng")
	val lng: Double? = null,

	@field:SerializedName("lat")
	val lat: Double? = null
)