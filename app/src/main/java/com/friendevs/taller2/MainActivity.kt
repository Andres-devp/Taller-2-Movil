package com.friendevs.taller2

import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Geocoder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import com.friendevs.taller2.navigation.Navigation
import com.friendevs.taller2.ui.theme.Taller2Theme
import com.friendevs.taller2.utils.geocoder
import com.friendevs.taller2.utils.lightSensor
import com.friendevs.taller2.utils.sensorManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        geocoder = Geocoder(this)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(
            Sensor.TYPE_LIGHT)

        enableEdgeToEdge()
        setContent {
            Taller2Theme {
                Scaffold() {paddingValues ->
                    Navigation(paddingValues)
                }

            }
        }
    }
}
