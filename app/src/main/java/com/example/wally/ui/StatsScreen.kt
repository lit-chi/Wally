package com.example.wally.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    todayPayments: Int,
    todayDues: Int,
    peopleDue: Int,
    onBack: () -> Unit,
    onConnectGoogleDrive: () -> Unit
) {
    val total = todayPayments + todayDues
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "TODAY",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSettings = true }
                    ) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Spacer(Modifier.height(8.dp))

            Text(
                text = "₹$total",
                style = MaterialTheme.typography.displayMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "TOTAL SPENT TODAY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(6.dp))

            StatCard(
                label = "EXPENSES",
                value = "₹$todayPayments"
            )

            StatCard(
                label = "DUES",
                value = "₹$todayDues"
            )

            StatCard(
                label = "PEOPLE DUE",
                value = peopleDue.toString()
            )
        }
    }
    if (showSettings) {
        AlertDialog(
            onDismissRequest = {
                showSettings = false
            },
            title = {
                Text("SETTINGS")
            },
            text = {
                Column {
                    Text("Google Drive")

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Button(
                        onClick = {
                            showSettings = false
                            onConnectGoogleDrive()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CONNECT GOOGLE DRIVE")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSettings = false
                    }
                ) {
                    Text("CLOSE")
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 18.dp,
                    vertical = 20.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}