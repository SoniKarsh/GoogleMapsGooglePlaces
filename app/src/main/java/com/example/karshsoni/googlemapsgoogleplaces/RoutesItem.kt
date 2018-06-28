package com.example.karshsoni.googlemapsgoogleplaces

data class RoutesItem(val summary: String = "",
                      val copyrights: String = "",
                      val legs: List<LegsItem>?,
                      val bounds: Bounds,
                      val overviewPolyline: OverviewPolyline)