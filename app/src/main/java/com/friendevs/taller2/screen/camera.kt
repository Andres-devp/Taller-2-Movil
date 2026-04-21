package com.friendevs.taller2.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.friendevs.taller2.R
import java.io.File

@Composable
fun CamaraScreen( paddingValues: PaddingValues) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val requiredImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { result ->
        imageUri = result
    }

    val cameraUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        File(context.filesDir, "CameraPic.jpg")
    )
    val launchCamera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) {
            imageUri = null
            imageUri = Uri.parse(cameraUri.toString() + "?t=${System.currentTimeMillis()}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imageModifier = Modifier
                .fillMaxWidth()
                .height(400.dp)

            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Imagen Cargada",
                    contentScale = ContentScale.Fit,
                    modifier = imageModifier
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.camera),
                    contentDescription = "Icono defecto",
                    modifier = imageModifier,
                    contentScale = ContentScale.Fit
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { requiredImage.launch("image/*") },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text("Gallery")
            }
            Button(
                onClick = { launchCamera.launch(cameraUri) },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text("Camera")
            }
        }
    }

}




@Preview(showBackground = true)
@Composable
fun GalleryPreview() {
    CamaraScreen(PaddingValues())
}