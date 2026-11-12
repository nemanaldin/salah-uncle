package com.androidhive.myapplication.ui.lessons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.content.*

@Composable
fun ArticleContents(steps: List<LessonStep>, onSection: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        val stateLabel = stringResource(if (expanded) R.string.ui_contents_expanded else R.string.ui_contents_collapsed)
        TextButton({ expanded = !expanded }, Modifier.testTag("article_contents").semantics { stateDescription = stateLabel }) { Text(stringResource(R.string.ui_article_contents)) }
        if (expanded) Column(Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())) { steps.forEachIndexed { index, step ->
            TextButton({ expanded = false; onSection(step.id) }, Modifier.fillMaxWidth().testTag("contents_${step.id}")) {
                Text("${index + 1}. ${sectionHeading(step)}")
            }
        } }
    }
}

@Composable
fun SectionBookmarkDialog(value: SectionBookmark, existing: SectionBookmark?, onSave: (SectionBookmark) -> Unit,
    onRemove: (SectionBookmark) -> Unit, onDismiss: () -> Unit) {
    var note by remember(value) { mutableStateOf(existing?.note.orEmpty()) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.ui_section_bookmark)) },
        text = { OutlinedTextField(note, { note = boundedNote(it) }, label = { Text(stringResource(R.string.ui_section_note)) },
            supportingText = { Text("${note.codePointCount(0, note.length)}/200") }, modifier = Modifier.testTag("section_note")) },
        confirmButton = { TextButton({ onSave(value.copy(note = note)); onDismiss() }) { Text(stringResource(R.string.ui_save)) } },
        dismissButton = { Row {
            if (existing != null) TextButton({ onRemove(value); onDismiss() }) { Text(stringResource(R.string.ui_remove_section_bookmark)) }
            TextButton(onDismiss) { Text(stringResource(R.string.ui_cancel)) }
        } })
}
