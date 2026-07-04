/*
 * Copyright (C) 2026  Gaurav Kumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.example.tidy.ui.component.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun FadingLazyRow(
    content: LazyListScope.() -> Unit
) {
    val bg = MaterialTheme.colorScheme.surface
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, bg),
                        startX = size.width * 0.91f,
                        endX = size.width
                    ),
                )
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, bg),
                        startX = size.width * 0.09f,
                        endX = size.width * 0f
                    ),
                )
            },
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        content()
    }
}
