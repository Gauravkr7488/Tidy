package com.example.tidy.ui.component.taskComponents

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tidy.Task

@Suppress("AssignedValueIsNeverRead")
@Composable
fun TaskCardNew(
    task: Task,
    onClick: (Task) -> Unit,
    modifier: Modifier = Modifier,
    icons: List<TaskIconAction> = emptyList(),
    contextMenuOptions: List<TaskContextAction> = emptyList(),
) {
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    var showMenu by remember { mutableStateOf(false) }
    var parentOffset by remember { mutableStateOf(Offset.Zero) }
    val correctedOffset = parentOffset + tapOffset
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .onGloballyPositioned {
                parentOffset = it.localToWindow(Offset.Zero)
            },
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick(task) },
                        onLongPress = { offset ->
                            if (contextMenuOptions.isNotEmpty()) {
                                tapOffset = offset
                                showMenu = true
                            }
                        }
                    )
                }
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp, bottom = 16.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None,
                )
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            icons.forEach { (icon, description, onClickAction, modifier) ->
                IconButton(onClick = { onClickAction(task) }) {
                    Icon(icon, contentDescription = description, modifier = modifier)
                }
            }

        }
    }

    TaskContextMenu(
        showMenu = showMenu,
        tapOffset = correctedOffset,
        onDismiss = { showMenu = !showMenu },
        options = contextMenuOptions
    )
}