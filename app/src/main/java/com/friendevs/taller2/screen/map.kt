package com.friendevs.taller2.screen

import com.friendevs.taller2.R
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.friendevs.taller2.utils.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION

@Composable
fun MapScreen(paddingValues: PaddingValues) {
    LocationScreen()
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationScreen() {
    val permission = rememberPermissionState(locationPermission)
    var showButton by remember { mutableStateOf(false) }

    SideEffect {
        if (!permission.status.isGranted) {
            if (permission.status.shouldShowRationale) {
                showButton = true
            } else {
                showButton = false
                permission.launchPermissionRequest()
            }
        }
    }

    if (permission.status.isGranted) {
        LocationWithRequest()
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (showButton) {
                Text("Access to GPS is Mandatory for this app.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { permission.launchPermissionRequest() }) {
                    Text("Request Location Permission")
                }
            } else {
                Text("No access to location")
            }
        }
    }
}

@Composable
fun LocationWithRequest() {
    val context = LocalContext.current
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }

    val locationCallback = remember {
        createLocationCallback { result ->
            result.lastLocation?.let {
                latitude = it.latitude
                longitude = it.longitude
                writeLocationToInternalStorage(context, it.latitude, it.longitude)
            }
        }
    }

    val locationRequest = remember { createLocationRequest() }

    DisposableEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, locationPermission) == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
        onDispose {
            locationClient.removeLocationUpdates(locationCallback)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMapsHome(LatLng(latitude, longitude))
    }
}

@Composable
fun GoogleMapsHome(currentLocation: LatLng) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Estilos de mapa
    val lightMapStyle = MapStyleOptions.loadRawResourceStyle(context, R.raw.lightmap)
    val darkMapStyle = MapStyleOptions.loadRawResourceStyle(context, R.raw.darkmap)
    var currentMapStyle by remember { mutableStateOf(lightMapStyle) }

    // Estados de marcadores y rutas
    var searchText by remember { mutableStateOf("") }
    var searchedLocation by remember { mutableStateOf<LatLng?>(null) }
    var longClickLocation by remember { mutableStateOf<LatLng?>(null) }
    var longClickTitle by remember { mutableStateOf("") }
    val longClickMarkerState = rememberMarkerState()

    var historyPoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    // Estado para centrar cámara una sola vez
    val cameraPositionState = rememberCameraPositionState()
    var hasCenteredCamera by remember { mutableStateOf(false) }

    // Centrar cámara al entrar
    LaunchedEffect(currentLocation) {
        if (currentLocation.latitude != 0.0 && !hasCenteredCamera) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
            hasCenteredCamera = true
        }
    }

    // Mostrar etiqueta de Long Click automáticamente
    LaunchedEffect(longClickTitle) {
        if (longClickTitle.isNotEmpty()) {
            longClickMarkerState.showInfoWindow()
        }
    }

    // Sensor de luminosidad
    val sensorListener = remember {
        object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                    val lux = event.values[0]
                    currentMapStyle = if (lux < 2000) darkMapStyle else lightMapStyle
                }
            }
        }
    }

    DisposableEffect(Unit) {
        lightSensor?.let { sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL) }
        onDispose { sensorManager.unregisterListener(sensorListener) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapStyleOptions = currentMapStyle),
            onMapLongClick = { position ->
                longClickLocation = position
                longClickMarkerState.position = position
                val address = findAddress(position)
                longClickTitle = address ?: "Marcador"

                // PUNTO 9: Dibujar ruta al punto tocado con Google Directions
                coroutineScope.launch {
                    routePoints = fetchRoute(currentLocation, position)
                }
            }
        ) {
            // Ubicación Actual
            if (currentLocation.latitude != 0.0) {
                Marker(state = rememberUpdatedMarkerState(position = currentLocation), title = "Tú")
            }

            // Marcador por busqueda
            searchedLocation?.let {
                Marker(state = rememberUpdatedMarkerState(position = it), title = searchText)
            }

            // Marcador por Long Click
            longClickLocation?.let {
                Marker(state = longClickMarkerState, title = longClickTitle)
            }

            // Trazar la ruta calculada con google
            if (routePoints.isNotEmpty()) {
                Polyline(points = routePoints, color = Color.Blue, width = 12f)
            }

            // PUNTO 10: Pintar historial
            if (historyPoints.isNotEmpty()) {
                Polyline(points = historyPoints, color = Color.Magenta, width = 8f, jointType = JointType.ROUND)
            }
        }

        // Interfaz de Búsqueda y Botones
        Column(modifier = Modifier.padding(16.dp).padding(top = 35.dp)) {
            Row(modifier = Modifier.fillMaxWidth().background(Color.White), verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Universidad Javeriana...") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    val loc = findLocation(searchText)
                    if (loc != null) {
                        searchedLocation = loc
                        coroutineScope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(loc, 15f))
                            // PUNTO 9: Dibujar ruta al lugar buscado con Google Directions
                            routePoints = fetchRoute(currentLocation, loc)
                        }
                    } else {
                        Toast.makeText(context, "Lugar no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                val destino = searchedLocation ?: longClickLocation
                if (destino != null && currentLocation.latitude != 0.0) {
                    val dist = distance(currentLocation.latitude, currentLocation.longitude, destino.latitude, destino.longitude)
                    Toast.makeText(context, "Distancia: $dist km", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Calcular Distancia")
            }
        }

        // Botón de historial
        IconButton(
            onClick = { historyPoints = readLocationHistory(context) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Refresh, "Ver Historial", tint = Color.Blue, modifier = Modifier.size(48.dp))
        }
    }
}

suspend fun fetchRoute(start: LatLng, end: LatLng): List<LatLng> {
    return withContext(Dispatchers.IO) {
        try {
            val apiKey = "AIzaSyCn0tHbVZOBrZfoXhzKyVc5ERKUHHZJ_QQ"

            val urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=${start.latitude},${start.longitude}&destination=${end.latitude},${end.longitude}&key=$apiKey"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            val inputStream = connection.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val response = bufferedReader.use { it.readText() }

            val jsonObject = JSONObject(response)
            val routes = jsonObject.getJSONArray("routes")

            if (routes.length() > 0) {
                val route = routes.getJSONObject(0)
                val polyline = route.getJSONObject("overview_polyline").getString("points")
                return@withContext decodePolyline(polyline)
            }
        } catch (e: Exception) {
            Log.e("GoogleDirections", "Error obteniendo la ruta: ${e.message}")
        }
        emptyList()
    }
}

private fun decodePolyline(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0
    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat
        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng
        val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
        poly.add(p)
    }
    return poly
}