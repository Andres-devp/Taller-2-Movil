package com.friendevs.taller2.utils

import android.util.Log
import com.friendevs.taller2.model.MyLocation
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import java.io.BufferedWriter
import java.util.Date
import android.content.Context
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import java.io.OutputStreamWriter

const val RADIUS_OF_EARTH_KM = 6371.01


fun createLocationRequest() : LocationRequest {
    return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
        .setWaitForAccurateLocation(true)
        .setMinUpdateIntervalMillis(5000)
        .setMinUpdateDistanceMeters(30f)
        .build()
}

fun createLocationCallback(onLocationChange: (LocationResult) -> Unit): LocationCallback {
    val callback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            onLocationChange(locationResult)
        }
    }
    return callback
}

fun distance(lat1 : Double, long1: Double, lat2:Double, long2:Double) : Double{
    val latDistance = Math.toRadians(lat1 - lat2)
    val lngDistance = Math.toRadians(long1 - long2)
    val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)+
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    val result = RADIUS_OF_EARTH_KM * c;
    return Math.round(result*100.0)/100.0;
}



fun writeLocationToInternalStorage(context: Context, latitude: Double, longitude: Double) {
    try {
        val myLocation = MyLocation(Date(), latitude, longitude)
        val jsonObject = myLocation.toJSON()
        val fileOutputStream = context.openFileOutput("ubicaciones.json", Context.MODE_APPEND)
        val writer = BufferedWriter(OutputStreamWriter(fileOutputStream))
        writer.write(jsonObject.toString() + "\n")
        writer.close()
        Log.i("LOCATION", "Registro guardado en internal storage: ${jsonObject}")
    } catch (e: Exception) {
        Log.e("LOCATION", "Error escribiendo el archivo: ${e.message}")
    }
}


fun readLocationHistory(context: Context): List<LatLng> {
    val history = mutableListOf<LatLng>()
    try {
        val fileInputStream = context.openFileInput("ubicaciones.json")
        val reader = fileInputStream.bufferedReader()
        reader.forEachLine { line ->
            val json = JSONObject(line)
            val lat = json.getDouble("latitude")
            val lon = json.getDouble("longitude")
            history.add(LatLng(lat, lon))
        }
        reader.close()
    } catch (e: Exception) {
        Log.e("HISTORY", "Aún no hay historial o error al leer: ${e.message}")
    }
    return history
}