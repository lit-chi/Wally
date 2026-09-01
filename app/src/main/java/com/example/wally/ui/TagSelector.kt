package com.example.wally.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wally.data.Tag

@Composable
fun TagSelector(
    tags: List<Tag>,
    selectedTag: String?,
    onTagSelected: (String) -> Unit,
    onAddTag: (String) -> Unit,
    onDeleteTag: (Tag) -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    var newTag by remember { mutableStateOf("") }
    var showNewTag by remember { mutableStateOf(false) }
    var tagToDelete by remember { mutableStateOf<Tag?>(null) }

    val filteredTags = tags.filter {
        it.name.contains(search, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("SELECT TAG")
        },

        text = {
            Column {

                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Search") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.padding(4.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        filteredTags,
                        key = { it.id }
                    ) { tag ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        onTagSelected(tag.name)
                                        onDismiss()
                                    },
                                    onLongClick = {
                                        tagToDelete = tag
                                    }
                                )
                                .padding(12.dp)
                        ) {
                            Text(tag.name)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            showNewTag = true
                        }
                    ) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = "Add tag"
                        )
                    }
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("CLOSE")
            }
        }
    )

    if (showNewTag) {
        AlertDialog(
            onDismissRequest = {
                showNewTag = false
                newTag = ""
            },

            title = {
                Text("NEW TAG")
            },

            text = {
                OutlinedTextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    label = { Text("Tag name") },
                    singleLine = true
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        val cleanName = newTag.trim()

                        if (cleanName.isNotEmpty()) {
                            onAddTag(cleanName)
                            onTagSelected(cleanName)
                        }

                        newTag = ""
                        showNewTag = false
                        onDismiss()
                    }
                ) {
                    Text("ADD")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showNewTag = false
                        newTag = ""
                    }
                ) {
                    Text("CANCEL")
                }
            }
        )
    }

    tagToDelete?.let { tag ->

        AlertDialog(
            onDismissRequest = {
                tagToDelete = null
            },

            title = {
                Text("DELETE TAG?")
            },

            text = {
                Text(
                    "Delete \"${tag.name}\"? Existing transactions using this tag will not be changed."
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTag(tag)
                        tagToDelete = null
                    }
                ) {
                    Text("DELETE")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        tagToDelete = null
                    }
                ) {
                    Text("CANCEL")
                }
            }
        )
    }
}