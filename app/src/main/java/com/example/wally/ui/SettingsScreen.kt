package com.example.wally.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wally.data.SettingsDataStore
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onConnectGoogleDrive: () -> Unit,
    onDisconnectGoogleDrive: () -> Unit
) {
    val context = LocalContext.current

    val settingsDataStore = remember {
        SettingsDataStore(context.applicationContext)
    }

    val googleEmail by settingsDataStore.googleEmail
        .collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("SETTINGS")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text("GOOGLE DRIVE")

            if (googleEmail == null) {

                Text("Not connected")

                Button(
                    onClick = onConnectGoogleDrive,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CONNECT GOOGLE DRIVE")
                }

            } else {

                Text("Connected as")

                Text(
                    text = googleEmail!!
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Button(
                    onClick = onDisconnectGoogleDrive,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("DISCONNECT GOOGLE DRIVE")
                }
            }
        }
    }
}