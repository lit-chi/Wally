package com.example.wally

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.wally.data.ExpenseDatabase
import com.example.wally.ui.HomeScreen
import com.example.wally.ui.theme.WallyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = ExpenseDatabase.getDatabase(applicationContext)

        setContent {
            WallyTheme {
                HomeScreen(
                    expenseDao = database.expenseDao(),
                    dueDao = database.dueDao(),
                    tagDao = database.tagDao()
                )
            }
        }
    }
}