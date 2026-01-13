package com.bennysamuel.livem.ui


import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bennysamuel.livem.AppViewModel
import kotlinx.datetime.*
import kotlinx.coroutines.delay
import kotlin.time.Clock

data class DailyTask(
    val id: String = (0..999999).random().toString(),
    val title: String,
    val isCompleted: Boolean = false,
    val targetTime: LocalTime? = null,
    val isRecurring: Boolean = false,
    val recurringDays: Set<Int> = emptySet(),
    val createdAt: Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyTodoScreen(appViewModel: AppViewModel) {
    val tasks = appViewModel.todaysTasks
    val historyTasks = appViewModel.taskHistory

    var showCreateSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }

    val activeTasks = tasks.filter { !it.isCompleted }.sortedBy { it.targetTime }
    val completedToday = tasks.filter { it.isCompleted }
    val progress = if (tasks.isNotEmpty()) completedToday.size.toFloat() / tasks.size else 0f

    val dateText = remember {
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        "${now.dayOfWeek.name}, ${now.month.name.take(3)} ${now.dayOfMonth}"
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) { Icon(Icons.Rounded.Add, "Add Task", Modifier.size(28.dp)) }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {

            DailyHeader(
                date = dateText,
                progress = progress,
                completedCount = completedToday.size
            ) {
                appViewModel.refreshAllTaskData()
                showHistorySheet = true
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "MISSIONS",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary.copy(0.6f),
                        letterSpacing = 2.sp
                    )
                }

                items(activeTasks, key = { it.id }) { task ->
                    DailyTaskCard(
                        task = task,
                        onToggle = { appViewModel.toggleTask(task) },
                        onDelete = { }
                    )
                }

                if (activeTasks.isEmpty()) {
                    item { EmptyPrompt(tasks.isNotEmpty()) }
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateDailyTaskSheet(
            onDismiss = { showCreateSheet = false },
            onSave = { title, time ->
                appViewModel.addTask(title, time)
            }
        )
    }

    if (showHistorySheet) {
        LaunchedEffect(Unit){
            appViewModel.refreshAllTaskData()
        }
        HistorySheet(
            completedTasks = historyTasks,
            onDismiss = { showHistorySheet = false },
            onRestore = { task -> appViewModel.toggleTask(task) }
        )
    }
}

@Composable
fun DailyTaskCard(task: DailyTask, onToggle: () -> Unit, onDelete: () -> Unit) {
    var countdownText by remember { mutableStateOf("") }
    var isUrgent by remember { mutableStateOf(false) }

    LaunchedEffect(task.targetTime, task.isCompleted) {
        while (task.targetTime != null && !task.isCompleted) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
            val diffSeconds = task.targetTime!!.toSecondOfDay() - now.toSecondOfDay()

            if (diffSeconds <= 0) {
                countdownText = "Deadline Passed"
                isUrgent = true
            } else {
                val h = diffSeconds / 3600
                val m = (diffSeconds % 3600) / 60
                countdownText = if (h > 0) "${h}h ${m}m remaining" else "${m}m remaining"
                isUrgent = h < 1
            }
            delay(30000)
        }
    }

    Surface(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = if (isUrgent) MaterialTheme.colorScheme.errorContainer.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
        border = BorderStroke(1.dp, if (isUrgent) MaterialTheme.colorScheme.error.copy(0.2f) else MaterialTheme.colorScheme.outline.copy(0.05f))
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(28.dp)
                    .border(2.dp, if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, CircleShape)
            )

            Column(Modifier.padding(horizontal = 16.dp).weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                if (task.targetTime != null) {
                    Text(
                        text = countdownText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUrgent) MaterialTheme.colorScheme.error else Color.Gray
                    )
                }
            }

//            IconButton(onClick = onDelete) {
//                Icon(Icons.Rounded.DeleteOutline, null, tint = Color.Gray.copy(0.5f))
//            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDailyTaskSheet(onDismiss: () -> Unit, onSave: (String, LocalTime?) -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(24.dp).padding(bottom = 32.dp)) {
            Text("New Mission", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        text = "What needs to be done?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(Modifier.height(20.dp))

            Surface(
                onClick = { showTimePicker = true },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Schedule, null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = selectedTime?.let { "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}" } ?: "Set Deadline Time",
                        modifier = Modifier.padding(start = 12.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, selectedTime)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = title.isNotBlank()
            ) { Text("Confirm Mission", fontWeight = FontWeight.Bold) }
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = LocalTime(state.hour, state.minute)
                    showTimePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state) }
        )
    }
}

@Composable
fun DailyHeader(date: String, progress: Float, completedCount: Int, onViewHistory: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
        shadowElevation = 2.dp
    ) {
        Row(Modifier.fillMaxWidth().padding(24.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column {
                Text(date, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("Daily Tasks", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(12.dp))
                AssistChip(
                    onClick = onViewHistory,
                    label = { Text("Logbook ($completedCount)") },
                    leadingIcon = { Icon(Icons.Rounded.History, null, Modifier.size(16.dp)) }
                )
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyPrompt(hasTasks: Boolean) {
    Column(
        Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.FactCheck,
            null,
            Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(0.2f)
        )
        Text(
            text = if(hasTasks) "Mission Complete!" else "No tasks for today",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(completedTasks: List<DailyTask>, onDismiss: () -> Unit, onRestore: (DailyTask) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text("Logbook", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("Completed tasks from previous days", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Spacer(Modifier.height(16.dp))

            if (completedTasks.isEmpty()) {
                Text("No historical data yet.", Modifier.padding(vertical = 20.dp), color = Color.Gray)
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(completedTasks) { task ->
                    Row(
                        Modifier.fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.3f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.padding(horizontal = 12.dp).weight(1f)) {
                            Text(
                                task.title,
                                style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough)
                            )
                            val date = Instant.fromEpochMilliseconds(task.createdAt).toLocalDateTime(TimeZone.currentSystemDefault()).date
                            Text(date.toString(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                        IconButton(onClick = { onRestore(task) }) { Icon(Icons.Rounded.Undo, null) }
                    }
                }
            }
        }
    }
}





//
//import androidx.compose.animation.*
//import androidx.compose.foundation.*
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.rounded.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextDecoration
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import kotlinx.datetime.*
//import kotlinx.coroutines.delay
//import kotlin.time.Clock
//
//data class DailyTask(
//    val id: String = (0..999999).random().toString(),
//    val title: String,
//    val isCompleted: Boolean = false,
//    val targetTime: LocalTime? = null,
//    val isRecurring: Boolean = false,
//    val recurringDays: Set<Int> = emptySet(),
//    val createdAt: Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
//)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DailyTodoScreen() {
//    var tasks by remember { mutableStateOf(mutableListOf<DailyTask>()) }
//    var showCreateSheet by remember { mutableStateOf(false) }
//    var showHistorySheet by remember { mutableStateOf(false) }
//
//    val activeTasks = tasks.filter { !it.isCompleted }.sortedBy { it.targetTime }
//    val completedTasks = tasks.filter { it.isCompleted }
//    val progress = if (tasks.isNotEmpty()) completedTasks.size.toFloat() / tasks.size else 0f
//
//    Scaffold(
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = { showCreateSheet = true },
//                containerColor = MaterialTheme.colorScheme.primary,
//                shape = RoundedCornerShape(16.dp)
//            ) { Icon(Icons.Rounded.Add, "Add Task", Modifier.size(28.dp)) }
//        }
//    ) { padding ->
//        Column(Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
//
//            DailyHeader(progress, completedTasks.size) { showHistorySheet = true }
//
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//                contentPadding = PaddingValues(24.dp),
//                verticalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                item {
//                    Text("MISSIONS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary.copy(0.6f), letterSpacing = 2.sp)
//                }
//
//                items(activeTasks, key = { it.id }) { task ->
//                    DailyTaskCard(
//                        task = task,
//                        onToggle = { tasks = tasks.map { if (it.id == task.id) it.copy(isCompleted = true) else it }.toMutableList() },
//                        onDelete = { tasks = tasks.filter { it.id != task.id }.toMutableList() }
//                    )
//                }
//
//                if (activeTasks.isEmpty()) {
//                    item { EmptyPrompt(tasks.isNotEmpty()) }
//                }
//            }
//        }
//    }
//
//    if (showCreateSheet) {
//        CreateDailyTaskSheet(
//            onDismiss = { showCreateSheet = false },
//            onSave = { tasks = (tasks + it).toMutableList() }
//        )
//    }
//
//    if (showHistorySheet) {
//        HistorySheet(completedTasks, { showHistorySheet = false }) { task ->
//            tasks = tasks.map { if (it.id == task.id) it.copy(isCompleted = false) else it }.toMutableList()
//        }
//    }
//}
//
//@Composable
//fun DailyTaskCard(task: DailyTask, onToggle: () -> Unit, onDelete: () -> Unit) {
//    var countdownText by remember { mutableStateOf("") }
//    var isUrgent by remember { mutableStateOf(false) }
//
//    LaunchedEffect(task.targetTime) {
//        while (task.targetTime != null && !task.isCompleted) {
//            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
//            val diffSeconds = task.targetTime.toSecondOfDay() - now.toSecondOfDay()
//
//            if (diffSeconds <= 0) {
//                countdownText = "Missed"
//                isUrgent = true
//            } else {
//                val h = diffSeconds / 3600
//                val m = (diffSeconds % 3600) / 60
//                countdownText = "${h}h ${m}m remaining"
//                isUrgent = h < 2
//            }
//            delay(1000)
//        }
//    }
//
//    Surface(
//        onClick = onToggle,
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(24.dp),
//        color = if (isUrgent) MaterialTheme.colorScheme.errorContainer.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
//        border = BorderStroke(1.dp, if (isUrgent) MaterialTheme.colorScheme.error.copy(0.2f) else MaterialTheme.colorScheme.outline.copy(0.05f))
//    ) {
//        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
//            Box(Modifier.size(28.dp).border(2.dp, if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, CircleShape))
//
//            Column(Modifier.padding(horizontal = 16.dp).weight(1f)) {
//                Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    if (task.isRecurring) {
//                        Icon(Icons.Rounded.Repeat, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
//                    }
//                    if (task.targetTime != null) {
//                        Text(
//                            text = if (task.isRecurring) " • $countdownText" else countdownText,
//                            style = MaterialTheme.typography.labelSmall,
//                            color = if (isUrgent) MaterialTheme.colorScheme.error else Color.Gray
//                        )
//                    }
//                }
//            }
//            IconButton(onClick = onDelete) { Icon(Icons.Rounded.DeleteOutline, null, tint = Color.Gray.copy(0.5f)) }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CreateDailyTaskSheet(onDismiss: () -> Unit, onSave: (DailyTask) -> Unit) {
//    var title by remember { mutableStateOf("") }
//    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
//    var isRecurring by remember { mutableStateOf(false) }
//    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
//    var showTimePicker by remember { mutableStateOf(false) }
//
//    ModalBottomSheet(onDismissRequest = onDismiss) {
//        Column(Modifier.padding(24.dp).padding(bottom = 32.dp)) {
//            Text("Add Mission", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
//
//            Spacer(Modifier.height(16.dp))
//            OutlinedTextField(
//                value = title, onValueChange = { title = it },
//                placeholder = { Text("Task name...") },
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(16.dp)
//            )
//
//            Spacer(Modifier.height(24.dp))
//
//            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(Icons.Rounded.Cached, null, tint = MaterialTheme.colorScheme.primary)
//                    Text(" Repeat Weekly", Modifier.padding(start = 12.dp), fontWeight = FontWeight.Bold)
//                }
//                Switch(checked = isRecurring, onCheckedChange = { isRecurring = it })
//            }
//
//            // CONDITIONAL DAY SELECTOR
//            AnimatedVisibility(visible = isRecurring, enter = expandVertically(), exit = shrinkVertically()) {
//                Column {
//                    Spacer(Modifier.height(16.dp))
//                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
//                        val days = listOf("M", "T", "W", "T", "F", "S", "S")
//                        days.forEachIndexed { i, day ->
//                            val dayNum = i + 1
//                            val active = selectedDays.contains(dayNum)
//                            Box(
//                                Modifier.size(40.dp).clip(CircleShape)
//                                    .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
//                                    .clickable { selectedDays = if (active) selectedDays - dayNum else selectedDays + dayNum },
//                                contentAlignment = Alignment.Center
//                            ) { Text(day, color = if (active) Color.White else Color.Gray, fontWeight = FontWeight.Bold) }
//                        }
//                    }
//                }
//            }
//
//            Spacer(Modifier.height(20.dp))
//
//            Surface(
//                onClick = { showTimePicker = true },
//                shape = RoundedCornerShape(16.dp),
//                color = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)
//            ) {
//                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
//                    Icon(Icons.Rounded.Schedule, null, tint = MaterialTheme.colorScheme.primary)
//                    Text(
//                        text = selectedTime?.toString()?.take(5) ?: "Set Deadline Time",
//                        modifier = Modifier.padding(start = 12.dp),
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//            }
//
//            Spacer(Modifier.height(32.dp))
//            Button(
//                onClick = {
//                    if (title.isNotBlank()) {
//                        onSave(DailyTask(title = title, targetTime = selectedTime, isRecurring = isRecurring, recurringDays = if(isRecurring) selectedDays else emptySet()))
//                        onDismiss()
//                    }
//                },
//                modifier = Modifier.fillMaxWidth().height(56.dp),
//                shape = RoundedCornerShape(16.dp)
//            ) { Text("Confirm Mission", fontWeight = FontWeight.Bold) }
//        }
//    }
//
//    if (showTimePicker) {
//        val state = rememberTimePickerState()
//        AlertDialog(
//            onDismissRequest = { showTimePicker = false },
//            confirmButton = {
//                TextButton(onClick = {
//                    selectedTime = LocalTime(state.hour, state.minute)
//                    showTimePicker = false
//                }) { Text("Confirm") }
//            },
//            text = { TimePicker(state) }
//        )
//    }
//}
//
//@Composable
//fun DailyHeader(progress: Float, completedCount: Int, onViewHistory: () -> Unit) {
//    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp), shadowElevation = 2.dp) {
//        Row(Modifier.fillMaxWidth().padding(24.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
//            Column {
//                Text("MONDAY, JAN 12", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
//                Text("Daily Tasks", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
//                Spacer(Modifier.height(12.dp))
//                AssistChip(
//                    onClick = onViewHistory,
//                    label = { Text("Logbook ($completedCount)") },
//                    leadingIcon = { Icon(Icons.Rounded.History, null, Modifier.size(16.dp)) }
//                )
//            }
//            Box(contentAlignment = Alignment.Center) {
//                CircularProgressIndicator(progress = progress, modifier = Modifier.size(80.dp), strokeWidth = 8.dp, strokeCap = StrokeCap.Round)
//                Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold)
//            }
//        }
//    }
//}
//
//@Composable
//fun EmptyPrompt(hasTasks: Boolean) {
//    Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
//        Icon(Icons.Rounded.FactCheck, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(0.2f))
//        Text(if(hasTasks) "Mission Complete!" else "No tasks for today", color = Color.Gray, textAlign = TextAlign.Center)
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HistorySheet(completedTasks: List<DailyTask>, onDismiss: () -> Unit, onRestore: (DailyTask) -> Unit) {
//    ModalBottomSheet(onDismissRequest = onDismiss) {
//        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
//            Text("Logbook", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
//            Spacer(Modifier.height(16.dp))
//            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                items(completedTasks) { task ->
//                    Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(0.3f), RoundedCornerShape(16.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
//                        Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
//                        Text(task.title, Modifier.padding(horizontal = 12.dp).weight(1f), style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough))
//                        IconButton(onClick = { onRestore(task) }) { Icon(Icons.Rounded.Undo, null) }
//                    }
//                }
//            }
//        }
//    }
//}