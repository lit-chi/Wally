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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit
import androidx.work.ExistingPeriodicWorkPolicy
import com.example.wally.data.SettingsDataStore
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleDriveAuth = GoogleDriveAuth(this)

        val database = ExpenseDatabase.getDatabase(applicationContext)
        val settingsDataStore = SettingsDataStore(applicationContext)

//        val weeklyWorkRequest =
//            PeriodicWorkRequestBuilder<WeeklyExportWorker>(
//                7,
//                TimeUnit.DAYS
//            )
//                .setInitialDelay(
//                    10,
//                    TimeUnit.SECONDS
//                )
//                .build()
//        Log.d(
//            "WallyWork",
//            "Creating periodic work: ${weeklyWorkRequest.id}"
//        )
//
//        WorkManager
//            .getInstance(applicationContext)
//            .enqueueUniquePeriodicWork(
//                "WallyWeeklyExport",
//                ExistingPeriodicWorkPolicy.KEEP,
//                weeklyWorkRequest
//            )
//
//        Log.d(
//            "WallyWork",
//            "Enqueued periodic work: ${weeklyWorkRequest.id}"
//
        val workManager = WorkManager.getInstance(applicationContext)

        workManager.cancelUniqueWork("WallyWeeklyExport")

        val weeklyWorkRequest =
            PeriodicWorkRequestBuilder<WeeklyExportWorker>(
                7,
                TimeUnit.DAYS
            )
                .setInitialDelay(
                    10,
                    TimeUnit.SECONDS
                )
                .build()

        workManager.enqueueUniquePeriodicWork(
            "WallyWeeklyExport",
            ExistingPeriodicWorkPolicy.KEEP,
            weeklyWorkRequest
        )

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