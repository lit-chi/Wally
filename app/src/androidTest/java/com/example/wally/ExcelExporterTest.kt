package com.example.wally

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.wally.data.Expense
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import android.util.Log

@RunWith(AndroidJUnit4::class)
class ExcelExporterTest {

    @Test
    fun generateExcelFile() {

        val context =
            InstrumentationRegistry.getInstrumentation().targetContext

        val expenses = listOf(
            Expense(
                description = "Lunch",
                amount = 150,
                tag = "Food"
            ),
            Expense(
                description = "Uber",
                amount = 320,
                tag = "Travel"
            )
        )

        val file = File(
            context.getExternalFilesDir(null),
            "Wally Expenses.xlsx"
        )

        exportExpensesToExcel(
            expenses = expenses,
            outputFile = file
        )

        println("Excel file: ${file.absolutePath}")
        println("Excel file exists: ${file.exists()}")
        println("Excel file size: ${file.length()} bytes")
        Log.e("ExcelExporterTest", "Excel file: ${file.absolutePath}")
        Log.e("ExcelExporterTest", "Excel file exists: ${file.exists()}")
        Log.e("ExcelExporterTest", "Excel file size: ${file.length()} bytes")
        check(file.exists())
        check(file.length() > 0)
    }
}