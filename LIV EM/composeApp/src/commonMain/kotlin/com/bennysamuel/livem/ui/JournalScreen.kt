package com.bennysamuel.livem.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FolderCopy
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bennysamuel.liveem.db.JournalEntry
import com.bennysamuel.livem.AppViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Composable
fun JournalScreen(
    hasWrittenToday: Boolean,
    enterJournalEntry: () -> Unit,
    feedItems: List<Pair<JournalEntry, String>>,
    appViewModel: AppViewModel,

    ) {
    val searchQuery = appViewModel.searchQuery
    val selectedFilter = appViewModel.selectedFilter
    val previewData = appViewModel.selectedPreview

    if (previewData != null) {
        JournalPreviewSheet(
            previewData = previewData,
            onDismiss = { appViewModel.clearPreview() },
            onEdit = { entryId ->
//                appViewModel.initializeEditor()
//                enterJournalEntry()
            }
        )
    }

    if (appViewModel.groupEntries != null) {
        FolderEntriesSheet(
            entries = appViewModel.groupEntries!!,
            onDismiss = { appViewModel.groupEntries = null },
            onEntryClick = { appViewModel.loadPreview(it) }
        )
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            JournalSearchBar(searchQuery) { appViewModel.searchQuery = it }
            if (searchQuery.isNotEmpty()){
                Spacer(Modifier.height(24.dp))

                LazyColumn (
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ){
                    items(feedItems) { (entry, snippet) ->
                        JournalEntryCard(entry, snippet, onClick = { appViewModel.loadPreview(entry.id) })
                    }
                }
            }else{
                Spacer(Modifier.height(24.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    item {
                        DailyPromptCard(hasWrittenToday, enterJournalEntry)
                    }


                    item {
                        SectionHeader("Past Self Conversations", Icons.Rounded.AutoAwesome)
                    }

                    item {
                        PastSelfCard()
                    }

                    item {
                        SectionHeader("Recent Entries", Icons.Rounded.History)
                    }

                    item {
                        FilterCarousel(
                            selectedFilter = appViewModel.selectedFilter,
                            onFilterSelected = { appViewModel.selectedFilter = it }
                        )
                    }
                    if (appViewModel.selectedFilter in listOf("Months", "Years", "Tagged")) {
                        val groups = appViewModel.groupItems(feedItems, appViewModel.selectedFilter)
                        items(groups.keys.toList()) { key ->
                            JournalFolderCard(
                                title = key,
                                dateRange = "Entries from $key",
                                entryCount = groups[key]?.size ?: 0,
                                onClick = { appViewModel.loadFolder(key) }
                            )
                        }
                    } else {
                        items(feedItems) { (entry, snippet) ->
                            JournalEntryCard(
                                entry = entry,
                                previewText = snippet,
                                onFavoriteToggle = {
                                    appViewModel.toggleFavorite(entry.id, entry.isFavorite)
                                },
                                onClick = {
                                    appViewModel.loadPreview(entry.id)
                                }
                            )
                        }
                    }

//                    items(feedItems) { (entry, snippet) ->
//                        JournalEntryCard(
//                            entry = entry,
//                            previewText = snippet,
//                            onFavoriteToggle = {
//                                appViewModel.toggleFavorite(entry.id, entry.isFavorite)
//                            },
//                            onClick = {
//                                appViewModel.loadPreview(entry.id)
//                            }
//                        )
//                    }
                }
            }
        }
    }
}

@Composable
fun DailyPromptCard(written: Boolean, enterJournalEntry: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (written) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = if (written) "Make more changes" else "Your page is empty",
                style = MaterialTheme.typography.headlineSmall,
                color = if (written) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (written) "You've already made an entry today. Want to add more?" else "What's on your mind? Take a moment to reflect on your day.",
                style = MaterialTheme.typography.bodyMedium,
                color = if (written) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary.copy(
                    alpha = 0.8f
                )
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { enterJournalEntry.invoke()},
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (written) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                    contentColor = if (written) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (written) "Edit Entry" else "Write Now")
            }
        }
    }
}

@Composable
fun PastSelfCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Message,
                    tint = MaterialTheme.colorScheme.tertiary,
                    contentDescription = null
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "Talk to your past self",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Ask questions about you from your past",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun JournalEntryCard(
    entry: JournalEntry,
    previewText: String,
    onFavoriteToggle: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val dateDisplay = remember(entry.createdAt) {
        val instant = Instant.fromEpochMilliseconds(entry.createdAt)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${dateTime.month.name.take(3)} ${dateTime.dayOfMonth}, ${dateTime.year}"
            .lowercase()
            .replaceFirstChar { it.uppercase() }
    }


    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateDisplay,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (entry.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        contentDescription = "Favorite",
                        modifier = Modifier.size(20.dp),
                        tint = if (entry.isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            Text(
                text = entry.title.ifEmpty { "Untitled" },
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = previewText.ifEmpty { "No additional text..." },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entry.tags?.split(",")?:listOf()) {
                    JournalTag(it)
                }
            }
        }
    }
}
@Composable
fun JournalTag(name: String) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderEntriesSheet(
    entries: List<Pair<JournalEntry, String>>,
    onDismiss: () -> Unit,
    onEntryClick: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(entries) { (entry, snippet) ->
                JournalEntryCard(
                    entry = entry,
                    previewText = snippet,
                    onClick = { onEntryClick(entry.id) }
                )
            }
        }
    }
}


@Composable
fun JournalSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp)),
        placeholder = {
            Text(
                "Search memories, tags, or moods...",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        },
        leadingIcon = {
            Icon(
                Icons.Rounded.Search, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        singleLine = true
    )
}

@Composable
fun FilterCarousel(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("All Time", "This Week", "Months", "Years", "Favorites", "Tagged")

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(filters) { filter ->
            val isSelected = selectedFilter == filter

            Surface(
                modifier = Modifier.clickable { onFilterSelected(filter) },
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
                border = if (isSelected) null
                else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (filter == "Favorites") {
                        Icon(
                            Icons.Rounded.Star,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isSelected) Color.White else Color(0xFFFFD700)
                        )
                        Spacer(Modifier.width(6.dp))
                    }

                    Text(
                        text = filter,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun JournalFolderCard(
    title: String,
    dateRange: String,
    entryCount: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.FolderCopy,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            }

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateRange,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = entryCount.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "entries",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun FolderEntriesSheet(
//    folderTitle: String,
//    onDismiss: () -> Unit
//) {
//    ModalBottomSheet(
//        onDismissRequest = onDismiss,
//        containerColor = MaterialTheme.colorScheme.surface,
//        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline) }
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 24.dp)
//        ) {
//            Text(
//                text = folderTitle,
//                style = MaterialTheme.typography.headlineSmall,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//            Spacer(Modifier.height(16.dp))
//
//            LazyColumn(
//                verticalArrangement = Arrangement.spacedBy(16.dp),
//                contentPadding = PaddingValues(bottom = 40.dp)
//            ) {
//                items(4) {
//                    JournalEntryCard()
//                }
//            }
//        }
//    }
//}