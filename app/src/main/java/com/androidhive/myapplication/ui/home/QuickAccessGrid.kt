package com.androidhive.myapplication.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Width is the available content width, after page padding. */
fun quickAccessColumns(widthDp: Float, fontScale: Float): Int =
    if (widthDp >= 2 * 148f * fontScale.coerceAtLeast(1f) + 12f) 2 else 1

data class QuickAccessItem(val id: String, val title: String, val onClick: () -> Unit)

/** The page scrolls; this small finite grid never nests a scrolling lazy grid. */
@Composable
fun QuickAccessGrid(items: List<QuickAccessItem>, modifier: Modifier = Modifier,
    artwork: @Composable (String, Modifier) -> Unit) {
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val columns = quickAccessColumns(maxWidth.value, fontScale)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { item ->
                        Card(onClick = item.onClick, modifier = Modifier.weight(1f).heightIn(min = 82.dp)
                            .testTag("home_${item.id}").semantics(mergeDescendants = true) {
                                contentDescription = item.title; role = Role.Button
                            }, shape = MaterialTheme.shapes.large,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Row(Modifier.fillMaxWidth().heightIn(min = 82.dp).padding(horizontal = 10.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.size(48.dp).clip(CircleShape),
                                    contentAlignment = Alignment.Center) {
                                    artwork(item.id, Modifier.fillMaxSize())
                                }
                                Text(item.title, style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
