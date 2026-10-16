package com.androidhive.myapplication.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.androidhive.myapplication.R

@Composable
fun PageColumn(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp), content = content)
}
@Composable
fun AppCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(23.dp)
    if (onClick == null) Card(modifier.fillMaxWidth(), shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    } else Card(onClick, modifier.fillMaxWidth(), shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}
@Composable
fun SectionTitle(text: String) = Text(text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })

@Composable
fun SupportingText(text: String, modifier: Modifier = Modifier) = Text(text, modifier, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
fun InfoCard(text: String) {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
fun ChoiceRow(title: String, selectedLabel: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ListItem(headlineContent = { Text(title) }, supportingContent = { Text(selectedLabel) },
        trailingContent = { Icon(Icons.Default.ArrowDropDown, null) },
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = 64.dp),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface))
}

@Composable
fun <T> ChoiceDialog(title: String, options: List<T>, selected: T, label: @Composable (T) -> String, onChoose: (T) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                options.forEach { option ->
                    Row(Modifier.fillMaxWidth().selectable(option == selected, role = Role.RadioButton) { onChoose(option); onDismiss() }
                        .padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RadioButton(option == selected, onClick = null)
                        Text(label(option), Modifier.weight(1f).padding(top = 3.dp))
                    }
                }
            }
        }, confirmButton = { TextButton(onDismiss) { Text(stringResource(R.string.ui_close)) } })
}
