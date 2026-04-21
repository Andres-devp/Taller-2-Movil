package com.friendevs.taller2.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.friendevs.taller2.screen.CamaraScreen
import com.friendevs.taller2.screen.MainScreen
import com.friendevs.taller2.screen.MapScreen
import com.friendevs.taller2.screen.PermissionContactsScreen

enum class AppScreens {
    main,
    camara,
    contact,
    map
}

@Composable
fun  Navigation(paddingValues: PaddingValues) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = AppScreens.main.name){

        composable(route = AppScreens.main.name){
            MainScreen( navController, paddingValues)

        }
        composable(route = AppScreens.contact.name){
            PermissionContactsScreen(paddingValues)

        }
        composable(route = AppScreens.camara.name){
            CamaraScreen(paddingValues)

        }
        composable(route = AppScreens.map.name){
            MapScreen(paddingValues)

        }

    }


}