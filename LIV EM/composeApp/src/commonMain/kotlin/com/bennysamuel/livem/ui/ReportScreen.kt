package com.bennysamuel.livem.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bennysamuel.liveem.db.ReviewReport
import com.bennysamuel.livem.AppViewModel
import io.ktor.util.collections.getValue
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(vm: AppViewModel) {
    val now = Clock.System.now().toEpochMilliseconds()
    val isReportReady = (vm.nextWeeklyDate ?: 0L) <= now
    val report by vm.reportState.collectAsState()
    var showResultSheet by remember { mutableStateOf(false) }

    LaunchedEffect(report) {
        if (report.isNotEmpty()) {
            showResultSheet = true
        }
    }

    Scaffold(
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(24.dp)) {


                    Button(
                        onClick = {
                            vm.generateWeeklyReport ()
                        },
                        enabled = isReportReady && !vm.isGenerating,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        if (vm.isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("AI is analyzing your journey...")
                        } else {
                            Icon(Icons.Rounded.AutoAwesome, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Generate Your life report", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(horizontal = 24.dp)
//        ) {
//            Spacer(Modifier.height(32.dp))
//            Text("Growth History", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
//
//            if (vm.reports.isEmpty()) {
//                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    Text("No reports generated yet.", color = Color.Gray)
//                }
//            } else {
//                LazyColumn(
//                    verticalArrangement = Arrangement.spacedBy(16.dp),
//                    contentPadding = PaddingValues(vertical = 24.dp)
//                ) {
//                    items(vm.reports) { report ->
//                        ReportItem(report)
//                    }
//                }
//            }
//        }
    }

    if (showResultSheet) {
        ReportDetailSheet(report = report) {
            showResultSheet = false
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailSheet(
    report: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            // --- ICON & CATEGORY ---
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "LIFE REVIEW",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Text(
                text = "Your Path to Growth",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))



            HorizontalDivider(
                modifier = Modifier.padding(vertical = 24.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )


            Text(
                text = report,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 28.sp,
                    letterSpacing = 0.25.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Close Insight", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatDate(ms: Long): String {
    val date = Instant.fromEpochMilliseconds(ms)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.month.name.lowercase().capitalize()} ${date.dayOfMonth}, ${date.year}"
}

//@Composable
//fun ReportsScreen(vm: AppViewModel) {
//    val now = Clock.System.now().toEpochMilliseconds()
//    val isReportReady = (vm.nextWeeklyDate ?: 0L) <= now
//
//    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
//        Text("Personal Growth", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
//
//        Spacer(Modifier.height(24.dp))
//
//        NextReportCard(
//            isReady = isReportReady,
//            nextDate = vm.nextWeeklyDate,
//            isGenerating = vm.isGenerating,
//            onGenerate = { vm.generateWeeklyReport() }
//        )
//
//        Spacer(Modifier.height(32.dp))
//
//        Text("HISTORY", style = MaterialTheme.typography.labelLarge, color = Color.Gray, letterSpacing = 2.sp)
//
//        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 16.dp)) {
//            items(vm.reports) { report ->
//                ReportItem(report)
//            }
//        }
//    }
//}
//
//@Composable
//fun NextReportCard(isReady: Boolean, nextDate: Long?, isGenerating: Boolean, onGenerate: () -> Unit) {
//    val containerColor = if (isReady) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
//    val contentColor = if (isReady) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
//
//    Surface(
//        color = containerColor,
//        shape = RoundedCornerShape(32.dp),
//        border = if (!isReady) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)) else null,
//        shadowElevation = if (isReady) 8.dp else 0.dp
//    ) {
//        Column(Modifier.padding(28.dp)) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.Top
//            ) {
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = if (isReady) "Insights Ready" else "Analysis in Progress",
//                        style = MaterialTheme.typography.headlineSmall,
//                        fontWeight = FontWeight.Bold,
//                        color = contentColor
//                    )
//                    Text(
//                        text = if (isReady) "Weekly Review" else "Building your story...",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = contentColor.copy(alpha = 0.7f)
//                    )
//                }
//
//                // Icon with a soft background glow
//                Box(
//                    modifier = Modifier
//                        .size(48.dp)
//                        .background(contentColor.copy(alpha = 0.1f), CircleShape),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        imageVector = if (isReady) Icons.Rounded.AutoAwesome else Icons.Rounded.Lock,
//                        contentDescription = null,
//                        tint = contentColor,
//                        modifier = Modifier.size(24.dp)
//                    )
//                }
//            }
//
//            Spacer(Modifier.height(24.dp))
//
//            if (isReady) {
//                Button(
//                    onClick = onGenerate,
//                    enabled = !isGenerating,
//                    modifier = Modifier.fillMaxWidth().height(56.dp),
//                    shape = RoundedCornerShape(16.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.primary,
//                        contentColor = MaterialTheme.colorScheme.onPrimary
//                    )
//                ) {
//                    if (isGenerating) {
//                        CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 3.dp)
//                    } else {
//                        Text("Reveal Today's Reflection", fontWeight = FontWeight.Bold)
//                    }
//                }
//            } else {
//                val daysLeft = ((nextDate ?: 0L) - Clock.System.now().toEpochMilliseconds()) / (24 * 60 * 60 * 1000L)
//                val progress = calculateReportProgress(nextDate)
//
//                Column {
//                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                        Text("$daysLeft days to go", style = MaterialTheme.typography.labelMedium, color = contentColor)
//                        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = contentColor)
//                    }
//                    Spacer(Modifier.height(8.dp))
//                    LinearProgressIndicator(
//                        progress = { progress },
//                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
//                        trackColor = contentColor.copy(alpha = 0.1f),
//                        color = contentColor,
//                        strokeCap = StrokeCap.Round
//                    )
//                }
//            }
//        }
//    }
//}
//
//fun calculateReportProgress(nextReportDate: Long?, isMonthly: Boolean = false): Float {
//    if (nextReportDate == null) return 0f
//
//    val now = Clock.System.now().toEpochMilliseconds()
//    val windowDuration = if (isMonthly) {
//        30 * 24 * 60 * 60 * 1000L
//    } else {
//        7 * 24 * 60 * 60 * 1000L
//    }
//
//    val windowStart = nextReportDate - windowDuration
//    val elapsed = now - windowStart
//
//    return (elapsed.toFloat() / windowDuration.toFloat()).coerceIn(0f, 1f)
//}
//
//@Composable
//fun ReportItem(report: ReviewReport) {
//    Surface(
//        onClick = { /* Open full report in BottomSheet */ },
//        shape = RoundedCornerShape(24.dp),
//        color = MaterialTheme.colorScheme.surface,
//        tonalElevation = 2.dp // Subtle depth
//    ) {
//        Row(
//            modifier = Modifier.padding(20.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            val badgeColor = if (report.reportType == "MONTHLY") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
//
//            Box(
//                modifier = Modifier
//                    .size(52.dp)
//                    .clip(RoundedCornerShape(16.dp))
//                    .background(badgeColor.copy(alpha = 0.15f)),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Rounded.Description,
//                    contentDescription = null,
//                    tint = badgeColor,
//                    modifier = Modifier.size(26.dp)
//                )
//            }
//
//            Column(
//                modifier = Modifier
//                    .padding(horizontal = 16.dp)
//                    .weight(1f)
//            ) {
//                Text(
//                    text = "${report.reportType} REVIEW",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = badgeColor,
//                    fontWeight = FontWeight.ExtraBold,
//                    letterSpacing = 1.sp
//                )
//                Text(
//                    text = report.content,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//
//                Text(
//                    text = "Generated on Jan 12, 2026",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
//                    modifier = Modifier.padding(top = 4.dp)
//                )
//            }
//
//            Icon(
//                imageVector = Icons.Rounded.ChevronRight,
//                contentDescription = null,
//                tint = MaterialTheme.colorScheme.outline,
//                modifier = Modifier.size(20.dp)
//            )
//        }
//    }
//}