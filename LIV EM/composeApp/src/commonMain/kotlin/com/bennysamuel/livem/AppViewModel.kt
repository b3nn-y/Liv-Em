package com.bennysamuel.livem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bennysamuel.liveem.db.DailyTask
import com.bennysamuel.liveem.db.JournalBlock
import com.bennysamuel.liveem.db.JournalEntry
import com.bennysamuel.liveem.db.ReviewReport
import com.bennysamuel.livem.ai.AiReflectionService
import com.bennysamuel.livem.db.LiveEmDbUtil
import com.bennysamuel.livem.user.SessionManager
import com.mohamedrejeb.richeditor.model.RichTextState
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant


class AppViewModel : ViewModel() {

    private val _feedItems = MutableStateFlow<List<Pair<JournalEntry, String>>>(emptyList())
    val feedItems: StateFlow<List<Pair<JournalEntry, String>>> = _feedItems.asStateFlow()

    private val _hasWrittenToday = MutableStateFlow(false)
    val hasWrittenToday = _hasWrittenToday.asStateFlow()

    var blocks = mutableStateListOf<com.bennysamuel.livem.ui.JournalBlock>()
    var title by mutableStateOf("")
    var tags = mutableStateListOf<String>()
    var isFav by mutableStateOf(false)
    var currentEntryId by mutableStateOf<String?>(null)

    var searchQuery by mutableStateOf("")
    var selectedFilter by mutableStateOf("All Time")

    var groupEntries by mutableStateOf<List<Pair<JournalEntry, String>>?>(null)

    init {
        refreshHomeData()
        initializeEditor()
        refreshAllTaskData()

    }

    init {
        viewModelScope.launch {
            snapshotFlow { Pair(searchQuery, selectedFilter) }
                .collect { (query, filter) ->
                    loadData(query, filter)
                }
        }
    }



    private fun loadData(query: String, filter: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = if (query.isNotEmpty()) {
                LiveEmDbUtil.searchEverything(query)
            } else {
                when (filter) {
                    "Favorites" -> LiveEmDbUtil.getFavoritesWithPreviews()
                    "This Week" -> LiveEmDbUtil.getThisWeekWithPreviews()
                    else -> LiveEmDbUtil.getFeedWithPreviews()
                }
            }
            withContext(Dispatchers.Main) { _feedItems.value = items }
        }
    }

    fun loadFolder(groupKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val all = _feedItems.value
            val filtered = when (selectedFilter) {
                "Months" -> all.filter { getMonthYear(it.first.createdAt) == groupKey }
                "Years" -> all.filter { getYear(it.first.createdAt) == groupKey }
                "Tagged" -> all.filter { it.first.tags?.contains(groupKey) == true }
                else -> all
            }
            withContext(Dispatchers.Main) { groupEntries = filtered }
        }
    }

    private fun getMonthYear(ts: Long): String {
        val dt = Instant.fromEpochMilliseconds(ts).toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dt.month.name} ${dt.year}"
    }

    private fun getYear(ts: Long): String =
        Instant.fromEpochMilliseconds(ts).toLocalDateTime(TimeZone.currentSystemDefault()).year.toString()


    fun addTag(tag: String) {
        if (tag.isNotBlank() && !tags.contains(tag)) {
            tags.add(tag)
        }
    }

    fun removeTag(tag: String) {
        tags.remove(tag)
    }
    fun refreshHomeData() {
        viewModelScope.launch(Dispatchers.Default) {
            val items = LiveEmDbUtil.getFeedWithPreviews()
            val writtenToday = LiveEmDbUtil.isJournalEnteredToday()

            withContext(Dispatchers.Main) {
                _feedItems.value = items
                _hasWrittenToday.value = writtenToday
            }
        }
    }


    fun initializeEditor() {
        viewModelScope.launch(Dispatchers.Default) {
            val todayId = LiveEmDbUtil.getTodayEntryId()

            if (todayId != null) {
                val header = LiveEmDbUtil.getEntryById(todayId)
                val existingBlocks = LiveEmDbUtil.loadJournalEntry(todayId)

                withContext(Dispatchers.Main) {
                    currentEntryId = todayId
                    title = header?.title ?: ""
                    isFav = header?.isFavorite ?: false
                    tags.clear()
                    tags.addAll(header?.tags?.split(",")?.filter { it.isNotBlank() } ?: emptyList())
                    blocks.clear()
                    blocks.addAll(existingBlocks)
                }
            } else {
                startNewEntry()
            }
        }
    }

    fun startNewEntry() {
        viewModelScope.launch(Dispatchers.Main) {
            currentEntryId = null
            title = ""
            isFav = false
            tags.clear()
            blocks.clear()
            blocks.add(com.bennysamuel.livem.ui.JournalBlock.Text(RichTextState()))
        }
    }


    fun saveJournal() {
        viewModelScope.launch(Dispatchers.Default) {
            LiveEmDbUtil.saveJournal(
                title = title,
                blocks = blocks.toList(),
                tags = tags.toList(),
                isFavourite = isFav,
                id = currentEntryId
            )
            refreshHomeData()
        }
    }

    fun toggleFavorite(entryId: String, currentStatus: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            LiveEmDbUtil.toggleFavourite(entryId, !currentStatus)
            refreshHomeData()
        }
    }

    var selectedPreview by mutableStateOf<Pair<JournalEntry, List<com.bennysamuel.livem.ui.JournalBlock>>?>(null)
        private set

    fun loadPreview(entryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val entry = LiveEmDbUtil.getEntryById(entryId)
            val blocks = LiveEmDbUtil.loadJournalEntry(entryId)
            if (entry != null) {
                withContext(Dispatchers.Main) {
                    selectedPreview = entry to blocks
                }
            }
        }
    }

    fun groupItems(items: List<Pair<JournalEntry, String>>, filter: String): Map<String, List<Pair<JournalEntry, String>>> {
        return when (filter) {
            "Months" -> items.groupBy { getMonthYear(it.first.createdAt) }
            "Years" -> items.groupBy { getYear(it.first.createdAt) }
            "Tagged" -> {
                val tagMap = mutableMapOf<String, MutableList<Pair<JournalEntry, String>>>()
                items.forEach { pair ->
                    pair.first.tags?.split(",")?.forEach { tag ->
                        if (tag.isNotBlank()) tagMap.getOrPut(tag) { mutableListOf() }.add(pair)
                    }
                }
                tagMap
            }
            else -> emptyMap()
        }
    }

    fun clearPreview() {
        selectedPreview = null
    }
    var taskHistory = mutableStateListOf<com.bennysamuel.livem.ui.DailyTask>()
        private set

    var todaysTasks = mutableStateListOf<com.bennysamuel.livem.ui.DailyTask>()
        private set

    fun refreshTodaysTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            val tasks = LiveEmDbUtil.getTodaysTasks()
            withContext(Dispatchers.Main) {
                todaysTasks.clear()
                todaysTasks.addAll(tasks)
            }
        }
    }

    fun refreshAllTaskData() {
        viewModelScope.launch(Dispatchers.IO) {
            val today = LiveEmDbUtil.getTodaysTasks()
            val history = LiveEmDbUtil.getAllTasks()

            withContext(Dispatchers.Main) {
                todaysTasks.clear()
                todaysTasks.addAll(today)

                taskHistory.clear()
                taskHistory.addAll(history)
            }
        }
    }

    fun addTask(title: String, time: LocalTime?) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = com.bennysamuel.livem.ui.DailyTask(title = title, targetTime = time)
            LiveEmDbUtil.insertTask(task)
            refreshAllTaskData()
        }
    }

    fun toggleTask(task: com.bennysamuel.livem.ui.DailyTask) {
        viewModelScope.launch(Dispatchers.IO) {
            LiveEmDbUtil.updateTaskStatus(task.id, !task.isCompleted)
            refreshAllTaskData()
        }
    }




    var reports = mutableStateListOf<ReviewReport>()
    var nextWeeklyDate by mutableStateOf<Long?>(null)
    var isGenerating by mutableStateOf(false)

    fun refreshReports() {
        viewModelScope.launch(Dispatchers.IO) {
            val dbReports = LiveEmDbUtil.getAllReports()
            val settings: Settings = Settings()
            val sessionManager = SessionManager(settings)
            val joinDate = sessionManager.getJoinDate()

            withContext(Dispatchers.Main) {
                reports.clear()
                reports.addAll(dbReports)
                calculateNextReport(joinDate, dbReports)
            }
        }
    }

    private fun calculateNextReport(joinDate: Long, existing: List<ReviewReport>) {
        val lastReportEnd = existing.find { it.reportType == "WEEKLY" }?.endDate ?: joinDate
        val nextDate = lastReportEnd + (7 * 24 * 60 * 60 * 1000L)
        nextWeeklyDate = nextDate
    }

    var _report = MutableStateFlow("")
    val reportState = _report.asStateFlow()

    fun generateWeeklyReport() {
        viewModelScope.launch {
            isGenerating = true
            val ai = AiReflectionService().generateReview()
            _report.value = if (ai != null){
                ai
            } else{
                "Failed to generate report"
            }
            isGenerating = false
        }
    }


}