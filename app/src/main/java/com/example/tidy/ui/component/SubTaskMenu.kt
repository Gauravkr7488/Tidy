package com.example.tidy.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.example.tidy.toggleValue

@Composable
fun SubTaskMenu(
    label: String,
    addNewTask: () -> Unit,
    addExistingTask: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "iconRotation"
    )
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = label, style = MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = { expanded = toggleValue(expanded) }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,   // change icon if needed
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation)
                )
            }

        }
        if (expanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick = { addNewTask() },
                    modifier = Modifier.weight(1f)
                ) {
                    Spacer(Modifier.width(6.dp))
                    Text("Add a New Task")
                }

                OutlinedButton(
                    onClick = { addExistingTask() },
                    modifier = Modifier.weight(1f)
                ) {
                    Spacer(Modifier.width(6.dp))
                    Text("Add From Existing")
                }
            }
        }
    }
}