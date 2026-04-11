package com.example.tidy.ui.component.taskComponents

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun TaskContextMenu(
    showMenu: Boolean,
    tapOffset: Offset,
    onDismiss: () -> Unit,
    options: List<TaskContextAction>
) {
    val density = LocalDensity.current
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = onDismiss,
        offset = with(density) {
            DpOffset(tapOffset.x.toDp(), tapOffset.y.toDp())
        },
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = {
                    Text(
                        option.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = option.color
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        tint = option.color,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    onDismiss()
                    option.onClick()
                }
            )
        }
    }
}