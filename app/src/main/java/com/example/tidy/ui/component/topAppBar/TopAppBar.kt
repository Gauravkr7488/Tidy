package com.example.tidy.ui.component.topAppBar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.tidy.ui.font.KnewaveFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(title: String, modifier: Modifier = Modifier) {
    LargeTopAppBar(
        title = { Text(title, fontFamily = KnewaveFontFamily) },
        modifier = modifier,
//        actions = TODO(),
//        expandedHeight = TODO(),
//        windowInsets = TODO(),
//        colors = TODO(),
//        scrollBehavior = TODO()
    )
}