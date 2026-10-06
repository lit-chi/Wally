package com.example.wally

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wally.data.ExpenseDatabase
import com.example.wally.data.SettingsDataStore
import kotlinx.coroutines.flow.first
import java.io.File
import android.util.Log

class WeeklyExportWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val database =
        ExpenseDatabase.getDatabase(appContext)

    private val settingsDataStore =
        SettingsDataStore(appContext)

    override suspend fun doWork(): Result {
        return try {
            // your existing Worker code here
            println("Wally weekly export worker started")

            // Database
            val expenseDao = database.expenseDao()
            val dueDao = database.dueDao()
            val tagDao = database.tagDao()



            // Settings
            val googleDriveFileId =
                settingsDataStore.googleDriveFileId.first()

            if (googleDriveFileId == null) {
                println("Google Drive is not connected. Skipping export.")
                return Result.success()
            }

            println("Google Drive file ID: $googleDriveFileId")
            val tokenProvider =
                GoogleDriveTokenProvider(applicationContext)

            val accessToken = try {
                tokenProvider.getAccessToken()
            } catch (e: Exception) {
                Log.e(
                    "WallyWeeklyExport",
                    "Google Drive authorization is no longer available. User needs to reconnect.",
                    e
                )
                settingsDataStore.setGoogleDriveNeedsReconnect()
                return Result.success()
            }

            Log.d(
                "WallyWeeklyExport",
                "Successfully obtained Google access token"
            )

            val driveClient = GoogleDriveClient(accessToken)

            Log.d(
                "WallyWeeklyExport",
                "GoogleDriveClient created"
            )
            val expenses = database.expenseDao()
                .getAllExpenses()
                .first()

            val excelFile = File(
                applicationContext.cacheDir,
                "Wally Expenses.xlsx"
            )

            exportExpensesToExcel(
                expenses = expenses,
                outputFile = excelFile
            )

            Log.d(
                "WallyWeeklyExport",
                "Excel file generated: ${excelFile.exists()}"
            )

            val updatedFileId = driveClient.uploadOrUpdate(
                excelFile
            )

            Log.d(
                "WallyWeeklyExport",
                "Drive file updated: $updatedFileId"
            )
            Result.success()
        } catch (e: Exception) {
            Log.e(
                "WallyWeeklyExport",
                "Weekly export failed",
                e
            )
            Result.retry()
        }
    }

}