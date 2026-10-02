package com.example.wally

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.wally.data.ExpenseDatabase
import com.example.wally.ui.HomeScreen
import com.example.wally.ui.theme.WallyTheme
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

import com.example.wally.data.SettingsDataStore
class MainActivity : ComponentActivity() {
    private lateinit var googleDriveAuth: GoogleDriveAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleDriveAuth = GoogleDriveAuth(this)

        val database = ExpenseDatabase.getDatabase(applicationContext)
        val settingsDataStore = SettingsDataStore(applicationContext)

        setContent {
            WallyTheme {
                HomeScreen(
                    expenseDao = database.expenseDao(),
                    dueDao = database.dueDao(),
                    tagDao = database.tagDao(),
                    onConnectGoogleDrive = {
                        googleDriveAuth.authorize(
                            onSuccess = { accessToken ->

                                lifecycleScope.launch(Dispatchers.IO) {

                                    try {
                                        val expenses =
                                            database.expenseDao()
                                                .getAllExpenses()
                                                .first()

                                        val excelFile = File(
                                            cacheDir,
                                            "Wally Expenses.xlsx"
                                        )

                                        exportExpensesToExcel(
                                            expenses = expenses,
                                            outputFile = excelFile
                                        )

                                        val driveClient =
                                            GoogleDriveClient(accessToken)

                                        val email = driveClient.getUserEmail()


                                        val fileId =
                                            driveClient.uploadOrUpdate(excelFile)

                                            settingsDataStore.saveGoogleAccount(
                                            email = email,
                                            fileId = fileId
                                        )
                                        Log.d(
                                            "GoogleDrive",
                                            "Excel uploaded. File ID: $fileId"
                                        )

                                    } catch (e: Exception) {

                                        Log.e(
                                            "GoogleDrive",
                                            "Upload failed",
                                            e
                                        )
                                    }
                                }
                            },
                            onError = { error ->
                                Log.e(
                                    "GoogleDriveAuth",
                                    "Google Drive authorization failed",
                                    error
                                )
                            }
                        )
                    },
                    onDisconnectGoogleDrive = {

                        lifecycleScope.launch {

                            val email = settingsDataStore.googleEmail.first()

                            if (email == null) {
                                return@launch
                            }

                            googleDriveAuth.revokeAccess(
                                email = email,

                                onSuccess = {
                                    lifecycleScope.launch {
                                        settingsDataStore.clearGoogleAccount()

                                        Log.d(
                                            "GoogleDriveAuth",
                                            "Google Drive disconnected"
                                        )
                                    }
                                },

                                onError = { error ->
                                    Log.e(
                                        "GoogleDriveAuth",
                                        "Failed to disconnect Google Drive",
                                        error
                                    )
                                }
                            )
                        }
                    }                )
            }
        }
    }
}