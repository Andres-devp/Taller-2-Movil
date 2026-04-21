package com.friendevs.taller2.screen


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.friendevs.taller2.R
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.friendevs.taller2.utils.loadContacts
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionContactsScreen( paddingValues: PaddingValues) {

    val contactPermission = android.Manifest.permission.READ_CONTACTS
    val permission = rememberPermissionState(contactPermission)
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (permission.status.isGranted) {
            ContactsScreen(paddingValues)
        } else {
            var message = "Requiered Permission"
            if (permission.status.shouldShowRationale) {
                message = "Requiered Permissio neeed contacts"
            }
            Text(message, color = Color.Red)
        }
        Button(onClick = { permission.launchPermissionRequest() }) {
            Text("Permission")
        }
    }
}

@Composable
fun ContactsScreen(paddingValues: PaddingValues) {

    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val contacts = loadContacts(contentResolver)
    LazyColumn(modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize()) {
        items(contacts) { contact ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_contact_mail_24),
                        contentDescription = "Icon",
                        modifier = Modifier
                            .size(56.dp)
                            .padding(end = 10.dp)
                    )
                    Text(
                        text = contact.id,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Text(
                        text = contact.name,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

    }

}

@Composable
@Preview(showBackground = true)
fun ContactsScreenPreview() {
    PermissionContactsScreen( PaddingValues())
}


