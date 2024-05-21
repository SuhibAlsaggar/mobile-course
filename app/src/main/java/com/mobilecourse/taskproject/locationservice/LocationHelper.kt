package com.mobilecourse.taskproject.locationservice

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

public class LocationHelper {
    companion object {
        fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 6371e3

            val phi1 = lat1 * (PI / 180)
            val phi2 = lat2 * (PI / 180)
            val deltaPhi = (lat2 - lat1) * (PI / 180)
            val deltaLambda = (lon2 - lon1) * (PI / 180)

            val a = sin(deltaPhi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(deltaLambda / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))

            return r * c
        }
    }
}