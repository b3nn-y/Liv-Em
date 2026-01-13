package com.bennysamuel.livem.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.FormatUnderlined
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.bennysamuel.liveem.db.JournalEntry
import com.bennysamuel.livem.AppViewModel
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import io.ktor.util.collections.getValue
import io.ktor.util.collections.setValue
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

sealed class JournalBlock(val id: String = "jb${Clock.System.now().toEpochMilliseconds()}-${(0..9999).random().toString()}") {
    data class Text(val state: RichTextState) : JournalBlock()
    data class Gallery(val images: List<ByteArray>) : JournalBlock()
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun JournalEditorScreen(
    onBack: () -> Unit,
    appViewModel: AppViewModel
) {
    val title = appViewModel.title
    val isFav = appViewModel.isFav
    val tags = appViewModel.tags
    var showTagInput by remember { mutableStateOf(false) }

    val tagFocusRequester = remember { FocusRequester() }
    var tagInputFieldState by remember {
        mutableStateOf(TextFieldValue("Tag Name", selection = TextRange(0, 8)))
    }

    val blocks = appViewModel.blocks
    var activeIndex by remember { mutableStateOf(0) }
    var previewImage by remember { mutableStateOf<ByteArray?>(null) }

    val picker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Multiple(5),
        onResult = { bytes ->
            if (bytes.isNotEmpty()) {
                blocks.add(activeIndex + 1, JournalBlock.Gallery(bytes))
                blocks.add(activeIndex + 2, JournalBlock.Text(RichTextState()))
            }
        },
        scope = rememberCoroutineScope(),

        )

    Scaffold(
        topBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.background)) {
                EditorHeader(onBack, onSave = { appViewModel.saveJournal()
                onBack.invoke()})
                val activeState = (blocks.getOrNull(activeIndex) as? JournalBlock.Text)?.state
                if (activeState != null) FormattingToolBar(activeState)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { picker.launch() },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            ) { Icon(Icons.Rounded.AddPhotoAlternate, null, Modifier.size(24.dp)) }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                val todayDisplay = remember {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val dayName = now.dayOfWeek.name
                    val monthName = now.month.name
                    val day = now.dayOfMonth
                    val year = now.year

                    "$dayName, $monthName $day, $year"
                }
                Text(
                    todayDisplay,
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = MaterialTheme.colorScheme.primary.copy(0.4f)
                )
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    BasicTextField(
                        value = title,
                        onValueChange = { appViewModel.title = it },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = {
                            if (title.isEmpty()) Text(
                                "Title...",
                                style = MaterialTheme.typography.displayMedium,
                                color = Color.Gray
                            ); it()
                        }
                    )
                    IconButton(onClick = { appViewModel.isFav = !isFav }) {
                        Icon(
                            if (isFav) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            null,
                            tint = if (isFav) Color(0xFFFFD700) else Color.Gray
                        )
                    }
                }

                var newTag by remember { mutableStateOf("Tag Name") }
                FlowRow(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    tags.forEach { tag ->
                        TagChip(tag) { appViewModel.removeTag(tag) }
                    }
                    if (showTagInput) {
                        TagInputField(
                            value = tagInputFieldState,
                            focusRequester = tagFocusRequester,
                            onValueChange = { tagInputFieldState = it },
                            onComplete = {
                                val finalTag = tagInputFieldState.text.trim()
                                if (finalTag.isNotEmpty() && finalTag != "Tag Name") {
                                    appViewModel.addTag(finalTag)
                                }
                                tagInputFieldState =
                                    TextFieldValue("Tag Name", selection = TextRange(0, 8))
                                showTagInput = false
                            }
                        )
                    } else {
                        ActionChip("+ Tag") { showTagInput = true }
                    }
                }
            }

            itemsIndexed(blocks) { index, block ->
                when (block) {
                    is JournalBlock.Text -> {
                        RichTextEditor(
                            state = block.state,
                            modifier = Modifier.fillMaxWidth()
                                .onFocusChanged { if (it.isFocused) activeIndex = index },
                            placeholder = { Text("How was your day....", color = Color.Gray) },
                            colors = RichTextEditorDefaults.richTextEditorColors(
                                containerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 30.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                    is JournalBlock.Gallery -> {
                        PremiumGallery(
                            block.images,
                            { previewImage = it },
                            { blocks.removeAt(index) })
                    }
                }
            }
            item { Spacer(Modifier.height(150.dp)) }
        }
    }

    if (previewImage != null) {
        FullscreenPreview(previewImage!!) { previewImage = null }
    }
}

@Composable
fun TagChip(label: String, onRemove: () -> Unit) {
    Surface(
        onClick = onRemove,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(0.1f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.2f))
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "#$label",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Rounded.Close,
                null,
                Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ActionChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)
    ) {
        Text(
            label,
            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun TagInputField(
    value: TextFieldValue,
    focusRequester: FocusRequester,
    onValueChange: (TextFieldValue) -> Unit,
    onComplete: () -> Unit
) {
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .focusRequester(focusRequester)
            .background(MaterialTheme.colorScheme.primary.copy(0.1f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onComplete() }),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        textStyle = MaterialTheme.typography.labelLarge.copy(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
fun PremiumGallery(images: List<ByteArray>, onPop: (ByteArray) -> Unit, onDelete: () -> Unit) {

    Box(Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(24.dp))) {
        Row(Modifier.fillMaxSize()) {
            if (images.size == 1) {
                AsyncImage(
                    model = images[0],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clickable { onPop(images[0]) },
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = images[0],
                    contentDescription = null,
                    modifier = Modifier.weight(1.5f).fillMaxHeight().clickable { onPop(images[0]) },
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(4.dp))
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    AsyncImage(
                        model = images[1],
                        contentDescription = null,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                            .clickable { onPop(images[1]) },
                        contentScale = ContentScale.Crop
                    )
                    if (images.size > 2) {
                        Spacer(Modifier.height(4.dp))
                        Box(Modifier.weight(1f).fillMaxWidth()) {
                            AsyncImage(
                                model = images[2],
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clickable { onPop(images[2]) },
                                contentScale = ContentScale.Crop
                            )
                            if (images.size > 3) {
                                Box(
                                    Modifier.fillMaxSize().background(Color.Black.copy(0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "+${images.size - 2}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                .background(Color.Black.copy(0.3f), CircleShape)
        ) {
            Icon(Icons.Rounded.Delete, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun FormattingToolBar(state: RichTextState) {
    Surface(
        Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(0.1f))
    ) {
        Row(
            Modifier.padding(8.dp).horizontalScroll(rememberScrollState()),
            Arrangement.spacedBy(8.dp)
        ) {
            FormatBtn(
                Icons.Rounded.FormatBold,
                state.currentSpanStyle.fontWeight == FontWeight.Bold
            ) { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) }
            FormatBtn(
                Icons.Rounded.FormatItalic,
                state.currentSpanStyle.fontStyle == FontStyle.Italic
            ) { state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) }
            FormatBtn(
                Icons.Rounded.FormatUnderlined,
                state.currentSpanStyle.textDecoration == TextDecoration.Underline
            ) { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) }
            VerticalDivider(Modifier.height(24.dp).align(Alignment.CenterVertically))
            FormatBtn(
                Icons.Rounded.TextFields,
                state.currentSpanStyle.fontSize.isSp && state.currentSpanStyle.fontSize > 20.sp
            ) {
                val size =
                    if (state.currentSpanStyle.fontSize.isSp && state.currentSpanStyle.fontSize > 20.sp) 18.sp else 26.sp
                state.toggleSpanStyle(SpanStyle(fontSize = size))
            }
        }
    }
}

@Composable
fun FormatBtn(icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.background(
            if (active) MaterialTheme.colorScheme.primary.copy(0.15f) else Color.Transparent,
            RoundedCornerShape(8.dp)
        )
    ) {
        Icon(
            icon,
            null,
            tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun FullscreenPreview(data: ByteArray, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier.fillMaxSize().background(Color.Black).clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(model = data, contentDescription = null, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun EditorHeader(onBack: () -> Unit, onSave: () -> Unit) {
    Box(modifier = Modifier.padding(top = 20.dp)){
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            IconButton(onClick = onSave) { Icon(Icons.Rounded.Close, null) }
            Text(
                "Journal Entry",
                style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 2.sp)
            )
            TextButton(onClick = onSave) { Text("SAVE", fontWeight = FontWeight.Bold) }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalPreviewSheet(
    previewData: Pair<JournalEntry, List<JournalBlock>>,
    onDismiss: () -> Unit,
    onEdit: (String) -> Unit
) {
    val (entry, blocks) = previewData
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Info
            Text(
                text = entry.title.ifEmpty { "Untitled" },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CREATED ON ${entry.createdAt}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(20.dp))

            blocks.forEach { block ->
                when (block) {
                    is JournalBlock.Text -> {
                        Text(
                            text = block.state.annotatedString,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    is JournalBlock.Gallery -> {
                        PremiumGalleryPreview(block.images)
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

        }
    }
}

@Composable
fun PremiumGalleryPreview(images: List<ByteArray>, onImageClick: (ByteArray) -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Row(Modifier.fillMaxSize()) {
            when {
                images.isEmpty() -> {}

                images.size == 1 -> {
                    AsyncImage(
                        model = images[0],
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onImageClick(images[0]) },
                        contentScale = ContentScale.Crop
                    )
                }

                else -> {
                    AsyncImage(
                        model = images[0],
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight()
                            .clickable { onImageClick(images[0]) },
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.width(4.dp))

                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        AsyncImage(
                            model = images[1],
                            contentDescription = null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clickable { onImageClick(images[1]) },
                            contentScale = ContentScale.Crop
                        )

                        if (images.size > 2) {
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.weight(1f).fillMaxWidth()) {
                                AsyncImage(
                                    model = images[2],
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { onImageClick(images[2]) },
                                    contentScale = ContentScale.Crop
                                )

                                if (images.size > 3) {
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(0.4f))
                                            .clickable { onImageClick(images[2]) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+${images.size - 2}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}