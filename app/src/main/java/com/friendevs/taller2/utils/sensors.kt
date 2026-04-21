package com.friendevs.taller2.utils

import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.model.LatLng

lateinit var geocoder: Geocoder
lateinit var sensorManager: SensorManager
var lightSensor: Sensor? = null

fun findAddress(location: LatLng): String? {
    try {
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 2)
        if (!addresses.isNullOrEmpty()) {
            return addresses[0].getAddressLine(0)
        }
    } catch (e: Exception) {
        Log.e("GEOCODER", "Error buscando dirección: ${e.message}")
    }
    return null
}

fun findLocation(address: String): LatLng? {
    try {
        val addresses = geocoder.getFromLocationName(address, 2)
        if (!addresses.isNullOrEmpty()) {
            val addr = addresses[0]
            return LatLng(addr.latitude, addr.longitude)
        }
    } catch (e: Exception) {
        Log.e("GEOCODER", "Error buscando ubicación: ${e.message}")
    }
    return null
}