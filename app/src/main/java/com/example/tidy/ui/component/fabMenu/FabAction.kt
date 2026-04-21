package com.example.tidy.ui.component.fabMenu

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class FabAction(
    val icon: ImageVector,
    val description: String,
    val onClick: () -> Unit,
    val label: String,
    val modifier: Modifier = Modifier,
)
