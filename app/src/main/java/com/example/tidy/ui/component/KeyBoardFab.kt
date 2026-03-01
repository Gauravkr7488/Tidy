package com.example.tidy.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Save
@Composable
fun KeyboardAwareFAB(
    onClick: () -> Unit
) {
    val imePadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.padding(end = 16.dp, bottom = imePadding + 16.dp).size(80.dp) // 16dp above keyboard
        ) {
            Icon(Icons.Filled.Save, contentDescription = "Save Task")
        }
    }
}