package com.example.tidy.ui.component.taskComponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.tidy.Task

@Composable
fun TaskDeleteDialog(
    task: Task,
    onDismiss: () -> Unit,
    onDeleteClick: (Long, Boolean) -> Unit,
) {
    var deleteChildren by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Task") },
        text = {
            Column {
                Text(
                    text = buildAnnotatedString {
                        append("Are you sure you want to delete '")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(task.title)
                        }
                        append("'?")
                    }
                )
                if (task.children.isNotEmpty()){
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { deleteChildren = !deleteChildren }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = deleteChildren,
                            onCheckedChange = { deleteChildren = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Also delete subtasks")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDeleteClick(task.id, deleteChildren)
                onDismiss()
            }) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

