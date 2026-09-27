package com.example.wally

import ai.botisan.xlsxwriter.XlsxWorkbook
import com.example.wally.data.Expense
import java.io.File
import java.time.Instant
import java.time.ZoneId

fun exportExpensesToExcel(
    expenses: List<Expense>,
    outputFile: File
) {
    XlsxWorkbook().use { workbook ->

        val sheet = workbook.addWorksheet("Expenses")

        // Header row
        workbook.writeString(
            sheet,
            row = 0,
            column = 0,
            value = "Date"
        )

        workbook.writeString(
            sheet,
            row = 0,
            column = 1,
            value = "Description"
        )

        workbook.writeString(
            sheet,
            row = 0,
            column = 2,
            value = "Amount"
        )

        workbook.writeString(
            sheet,
            row = 0,
            column = 3,
            value = "Tag"
        )

        // Expense rows
        expenses.forEachIndexed { index, expense ->

            val row = index + 1

            val date = Instant
                .ofEpochMilli(expense.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            workbook.writeDate(
                sheet,
                row = row,
                column = 0,
                value = date
            )

            workbook.writeString(
                sheet,
                row = row,
                column = 1,
                value = expense.description
            )

            workbook.writeInteger(
                sheet,
                row = row,
                column = 2,
                value = expense.amount.toLong()
            )

            workbook.writeString(
                sheet,
                row = row,
                column = 3,
                value = expense.tag
            )
        }

        // Column widths
        workbook.setColumnWidth(
            sheet,
            column = 0,
            width = 15.0
        )

        workbook.setColumnWidth(
            sheet,
            column = 1,
            width = 30.0
        )

        workbook.setColumnWidth(
            sheet,
            column = 2,
            width = 12.0
        )

        workbook.setColumnWidth(
            sheet,
            column = 3,
            width = 20.0
        )

        // Save the workbook
        workbook.save(outputFile)
    }
}