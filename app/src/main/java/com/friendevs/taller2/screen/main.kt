package com.friendevs.taller2.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.friendevs.taller2.R
import com.friendevs.taller2.navigation.AppScreens

@Composable
fun MainScreen(navController: NavHostController, paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.contacts),
            contentDescription = "Contactos",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable(onClick = {navController.navigate(AppScreens.contact.name)})
                .padding(32.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.camera),
            contentDescription = "Camara",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable(onClick = {navController.navigate(AppScreens.camara.name)})
                .padding(32.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.osmap),
            contentDescription = "Mapa",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable(onClick = {navController.navigate(AppScreens.map.name)})
                .padding(32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview(){
    val c = rememberNavController()
    MainScreen(c, PaddingValues())
}