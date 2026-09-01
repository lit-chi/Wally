package com.example.wally.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EntryCard(
    isDue: Boolean,
    description: String,
    personName: String,
    amount: String,
    tags: List<String>,
    selectedTag: String?,
    onDescriptionChange: (String) -> Unit,
    onPersonNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onTagSelected: (String) -> Unit,
    onAddTag: () -> Unit,
    onSubmit: () -> Unit
) {
    val amountValid = amount.toIntOrNull() != null
    val nameValid = !isDue || personName.isNotBlank()
    val tagValid = selectedTag != null

    val canSubmit = amountValid && nameValid && tagValid

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isDue) "NEW DUE" else "NEW EXPENSE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (isDue) {
                            "Money you owe"
                        } else {
                            "Record something you spent"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.weight(1f))

                FilledIconButton(
                    onClick = onSubmit,
                    enabled = canSubmit,
                    shape = RoundedCornerShape(14.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "Add"
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isDue) {
                OutlinedTextField(
                    value = personName,
                    onValueChange = onPersonNameChange,
                    label = { Text("Person *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Description") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("Amount *") },
                prefix = { Text("₹ ") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "TAG *",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tags.take(6).forEach { tag ->
                    TagChip(
                        text = tag,
                        selected = selectedTag == tag,
                        onClick = {
                            onTagSelected(tag)
                        }
                    )
                }

                OutlinedButton(
                    onClick = onAddTag,
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(
                        horizontal = 14.dp,
                        vertical = 8.dp
                    )
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "Add tag",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}