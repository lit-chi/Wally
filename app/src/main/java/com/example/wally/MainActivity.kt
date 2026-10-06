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
import java.time.DayOfWeek
import java.time.Duration
import java.time.ZonedDateTime
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit
import androidx.work.ExistingPeriodicWorkPolicy
import com.example.wally.data.SettingsDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
class MainActivity : ComponentActivity() {

    private fun delayUntilNextMonday(): Long {
        val now = ZonedDateTime.now()

        var nextMonday = now
            .with(DayOfWeek.MONDAY)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        if (!nextMonday.isAfter(now)) {
            nextMonday = nextMonday.plusWeeks(1)
        }

        return Duration.between(now, nextMonday).toMillis()
    }
    private lateinit var googleDriveAuth: GoogleDriveAuth

    private fun scheduleWeeklyExport() {
        val weeklyWorkRequest =
            PeriodicWorkRequestBuilder<WeeklyExportWorker>(
                7,
                TimeUnit.DAYS
            )
                .setInitialDelay(
                    delayUntilNextMonday(),
                    TimeUnit.MILLISECONDS
                )
                .build()

        WorkManager
            .getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                "WallyWeeklyExport",
                ExistingPeriodicWorkPolicy.KEEP,
                weeklyWorkRequest
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleDriveAuth = GoogleDriveAuth(this)

        val database = ExpenseDatabase.getDatabase(applicationContext)
        val settingsDataStore = SettingsDataStore(applicationContext)

        lifecycleScope.launch {
            settingsDataStore.googleDriveNeedsReconnect.collect { needsReconnect ->
                if (needsReconnect) {
                    Log.d(
                        "GoogleDrive",
                        "Google Drive needs to be reconnected"
                    )
                }
            }
        }

        lifecycleScope.launch {
            val fileId =
                settingsDataStore.googleDriveFileId.first()

            if (fileId != null) {
                scheduleWeeklyExport()
            }
        }


        setContent {
            WallyTheme {
                val googleDriveNeedsReconnect by settingsDataStore
                    .googleDriveNeedsReconnect
                    .collectAsStateWithLifecycle(initialValue = false)

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

                                        scheduleWeeklyExport()

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
                                        WorkManager
                                            .getInstance(applicationContext)
                                            .cancelUniqueWork("WallyWeeklyExport")

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
                    },
                    googleDriveNeedsReconnect = googleDriveNeedsReconnect
                )
            }
        }
    }
}