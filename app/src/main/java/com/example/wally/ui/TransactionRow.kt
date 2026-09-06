package com.example.wally.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wally.data.Due
import com.example.wally.data.Expense
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember


@Composable
fun PaymentRow(
    expense: Expense,
    onLongClick: () -> Unit
) {
    TransactionRow(
        title = if(expense.description.isBlank()) {
            expense.tag
        } else {
            expense.description
        },
        subtitle = expense.tag,
        amount = expense.amount,
        onLongClick = onLongClick
    )
}

@Composable
fun DueRow(
    due: Due,
    onLongClick: () -> Unit
) {
    TransactionRow(
        title = due.name,
        subtitle = if (due.description.isBlank()) {
            due.tag
        } else {
            "${due.description} • ${due.tag}"
        },
        amount = due.amount,
        onLongClick = onLongClick
    )
}

@Composable
private fun TransactionRow(
    title: String,
    subtitle: String,
    amount: Int,
    onLongClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                    horizontal = 16.dp,
                    vertical = 15.dp
                )
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                )
            ,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = "₹$amount",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}