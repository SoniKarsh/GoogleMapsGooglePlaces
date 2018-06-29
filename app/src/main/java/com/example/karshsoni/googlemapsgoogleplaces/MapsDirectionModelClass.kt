package com.example.karshsoni.googlemapsgoogleplaces

data class MapsDirectionModelClass(val routes: List<RoutesItem>?,
                                   val geocodedWaypoints: List<GeocodedWaypointsItem>?,
                                   val status: String = "")