package com.example.tidy.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tidy.constants.Routes

@Composable
fun BottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.HOME,
            onClick = {
                navController.navigate(Routes.HOME) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(
                    Icons.Default.Home,
                    "Home Button",
                    modifier = Modifier.size(30.dp)
                )
            },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute == Routes.SETTINGS,
            onClick = {
                navController.navigate(Routes.SETTINGS) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(
                    Icons.Default.Settings,
                    null,
                    modifier = Modifier.size(30.dp)
                )
            },
            label = { Text("Settings") }
        )
    }
}