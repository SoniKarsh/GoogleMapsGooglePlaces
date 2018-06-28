package com.example.karshsoni.googlemapsgoogleplaces

data class StepsItem(val duration: Duration,
                     val startLocation: StartLocation,
                     val distance: Distance,
                     val travelMode: String = "",
                     val htmlInstructions: String = "",
                     val endLocation: EndLocation,
                     val polyline: Polyline)