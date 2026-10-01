package com.example.wally

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.wally.data.ExpenseDatabase
import com.example.wally.ui.HomeScreen
import com.example.wally.ui.theme.WallyTheme

class MainActivity : ComponentActivity() {
    private lateinit var googleDriveAuth: GoogleDriveAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleDriveAuth = GoogleDriveAuth(this)
        val database = ExpenseDatabase.getDatabase(applicationContext)

        setContent {
            WallyTheme {
                HomeScreen(
                    expenseDao = database.expenseDao(),
                    dueDao = database.dueDao(),
                    tagDao = database.tagDao(),
                    onConnectGoogleDrive = {
                        googleDriveAuth.authorize(
                            onSuccess = { accessToken ->
                                println("Google Drive authorized")
                            },
                            onError = { error ->
                                println("Google Drive authorization failed: ${error.message}")
                            }
                        )
                    }
                )
            }
        }
    }
}