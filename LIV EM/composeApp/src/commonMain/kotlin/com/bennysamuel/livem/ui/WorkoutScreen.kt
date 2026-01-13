package com.bennysamuel.livem.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDateTime


data class WorkoutSet(
    val weight: String = "",
    val reps: String = ""
)

data class Exercise(
    val id: String = (0..999).random().toString(),
    val name: String,
    val muscles: List<String>,
    val recommended: String = "3 x 12",
    val sets: List<WorkoutSet> = emptyList(),
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null
)


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkoutScreen() {
    var exercises by remember { mutableStateOf(sampleWorkout) }
    var exerciseToLog by remember { mutableStateOf<Exercise?>(null) }
    var setEntries by remember { mutableStateOf(listOf<WorkoutSet>()) }
    var showHistory by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            WorkoutHeader(title = "Shoulder Day", onHistory = { showHistory = true })
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("TODAY'S PLAN", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
            }

            items(exercises) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onLogClick = {
                        exerciseToLog = exercise
                        setEntries = if(exercise.sets.isEmpty()) List(3) { WorkoutSet() } else exercise.sets
                    }
                )
            }
        }
    }

    if (exerciseToLog != null) {
        ModalBottomSheet(onDismissRequest = { exerciseToLog = null }) {
            SetEntrySheet(
                exercise = exerciseToLog!!,
                entries = setEntries,
                onEntriesChanged = { setEntries = it },
                onSave = {
                    exercises = exercises.map {
                        if (it.id == exerciseToLog!!.id) it.copy(isCompleted = true, sets = setEntries) else it
                    }
                    exerciseToLog = null
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseCard(exercise: Exercise, onLogClick: () -> Unit) {
    Surface(
        onClick = onLogClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = if (exercise.isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.1f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(exercise.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)

                    FlowRow(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        exercise.muscles.forEach { muscle ->
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(0.1f)
                            ) {
                                Text(
                                    muscle.uppercase(),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (exercise.isCompleted) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth().background(Color.Black.copy(0.05f), RoundedCornerShape(16.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("GOAL", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(exercise.recommended, fontWeight = FontWeight.Bold)
                }

                if (!exercise.isCompleted) {
                    Text("TAP TO LOG", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                } else {
                    Text("${exercise.sets.size} SETS DONE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SetEntrySheet(
    exercise: Exercise,
    entries: List<WorkoutSet>,
    onEntriesChanged: (List<WorkoutSet>) -> Unit,
    onSave: () -> Unit
) {
    Column(Modifier.padding(24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
        Text("Log ${exercise.name}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text("Target: ${exercise.recommended}", color = Color.Gray)

        Spacer(Modifier.height(24.dp))

        entries.forEachIndexed { index, set ->
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("${index + 1}", modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary.copy(0.1f), CircleShape).wrapContentHeight(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = set.weight,
                    onValueChange = { w -> onEntriesChanged(entries.toMutableList().apply { this[index] = set.copy(weight = w) }) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("kg") },
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = set.reps,
                    onValueChange = { r -> onEntriesChanged(entries.toMutableList().apply { this[index] = set.copy(reps = r) }) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("reps") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        TextButton(
            onClick = { onEntriesChanged(entries + WorkoutSet()) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(Icons.Rounded.Add, null)
            Text("Add Set")
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Save Progress", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WorkoutHeader(title: String, onHistory: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp), shadowElevation = 2.dp) {
        Row(Modifier.padding(24.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column {
                Text("MONDAY SESSION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                Text(title, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            }
            IconButton(onClick = onHistory, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                Icon(Icons.Rounded.History, null)
            }
        }
    }
}

val sampleWorkout = listOf(
    Exercise(name = "Military Press", muscles = listOf("Shoulders", "Triceps", "Upper Chest"), recommended = "4 x 8"),
    Exercise(name = "Lateral Raise", muscles = listOf("Mid Delts"), recommended = "3 x 15"),
    Exercise(name = "Rear Delt Fly", muscles = listOf("Rear Delts", "Traps"), recommended = "3 x 12")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistorySheet(history: List<Exercise>, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.History, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text("Workout Log", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            }
            Text("Review your past performance", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            Spacer(Modifier.height(24.dp))

            if (history.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No exercises completed yet.", color = Color.Gray)
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(history) { item ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.1f))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(item.completedAt.toString() ?: "", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }

                            Spacer(Modifier.height(12.dp))

                            item.sets.forEachIndexed { index, set ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Set ${index + 1}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    Text("${set.weight}kg x ${set.reps} reps", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
