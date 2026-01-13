package com.bennysamuel.livem.db

import com.bennysamuel.liveem.db.DailyTask
import com.bennysamuel.liveem.db.JournalEntry
import com.bennysamuel.liveem.db.LiveEmDB
import com.bennysamuel.liveem.db.ReviewReport
import com.bennysamuel.livem.ui.JournalBlock
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlinx.datetime.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Clock

object LiveEmDbUtil : KoinComponent {

    private val dbDriver: DriverFactory by inject()

    val db: LiveEmDB by lazy { LiveEmDB(dbDriver.createDriver()) }
    private val journalQueries = db.journalsQueries

    fun saveJournal(
        id: String?,
        title: String,
        blocks: List<JournalBlock>,
        tags: List<String>,
        isFavourite: Boolean
    ) {
        val now = Clock.System.now().toEpochMilliseconds()

        val finalId = id ?: "entry_${now}_${(100..999).random()}"
        val tagsString = tags.joinToString(",")

        db.transaction {
            if (id != null) {
                journalQueries.updateEntry(
                    title = title,
                    tags = tagsString,
                    isFavorite = isFavourite,
                    id = id
                )

                journalQueries.deleteBlocksForEntry(id)
            } else {
                journalQueries.insertEntry(
                    id = finalId,
                    title = title,
                    tags = tagsString,
                    isFavorite = isFavourite,
                    createdAt = now
                )
            }

            blocks.forEachIndexed { index, block ->
                val blockId = block.id

                when (block) {
                    is JournalBlock.Text -> {
                        journalQueries.insertBlock(
                            id = blockId,
                            entryId = finalId,
                            blockType = "TEXT",
                            content = block.state.toHtml(),
                            sortOrder = index.toLong()
                        )
                    }

                    is JournalBlock.Gallery -> {
                        journalQueries.insertBlock(
                            id = blockId,
                            entryId = finalId,
                            blockType = "IMAGE",
                            content = null,
                            sortOrder = index.toLong()
                        )

                        block.images.forEachIndexed { imgIdx, bytes ->
                            journalQueries.insertImage(
                                id = "img_${blockId}_$imgIdx",
                                blockId = blockId,
                                imageData = bytes
                            )
                        }
                    }
                }
            }
        }
    }
    fun getAllEntries() = journalQueries.getEntries().executeAsList()

    fun loadJournalEntry(entryId: String): List<JournalBlock> {
        val dbBlocks = journalQueries.getBlocks(entryId).executeAsList()

        return dbBlocks.map { dbBlock ->
            when (dbBlock.blockType) {
                "TEXT" -> {
                    val richState = RichTextState()
                    richState.setHtml(dbBlock.content ?: "")
                    JournalBlock.Text(richState)
                }
                "IMAGE" -> {
                    val images = journalQueries.getImagesForBlock(dbBlock.id)
                        .executeAsList()
                        .map { it.imageData }
                    JournalBlock.Gallery(images)
                }
                else -> throw IllegalStateException("Unknown block type")
            }
        }
    }

    fun getEntryById(entryId: String): JournalEntry? {
        return journalQueries.getEntryById(entryId).executeAsOneOrNull()
    }

    fun deleteJournal(entryId: String) {
        db.transaction {
            journalQueries.deleteEntry(entryId)
        }
    }

    fun isJournalEnteredToday(): Boolean {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now().toLocalDateTime(tz)

        val startOfDay = LocalDateTime(now.year, now.month, now.dayOfMonth, 0, 0, 0)
            .toInstant(tz).toEpochMilliseconds()

        val endOfDay = LocalDateTime(now.year, now.month, now.dayOfMonth, 23, 59, 59)
            .toInstant(tz).toEpochMilliseconds()

        return journalQueries.countEntriesInRange(startOfDay, endOfDay).executeAsOne() > 0
    }

    fun toggleFavourite(entryId: String, isFav: Boolean) {
        journalQueries.updateFavorite(isFav, entryId)
    }

    fun searchEverything(query: String): List<Pair<JournalEntry, String>> {
        return journalQueries.searchEverything(query).executeAsList().map { row ->
            val entry = JournalEntry(row.id, row.title, row.tags, row.isFavorite, row.createdAt)
            val snippet = row.previewContent?.replace(Regex("<[^>]*>"), "")?.trim() ?: ""
            entry to snippet
        }
    }

    fun getThisWeekWithPreviews(): List<Pair<JournalEntry, String>> {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now().toLocalDateTime(tz)
        val startOfWeek = Clock.System.now().minus(7, DateTimeUnit.DAY, tz).toEpochMilliseconds()

        return getFeedWithPreviews().filter { it.first.createdAt >= startOfWeek }
    }

    fun getFavoritesWithPreviews(): List<Pair<JournalEntry, String>> {
        return journalQueries.getFavorites().executeAsList().map { row ->
            val entry = JournalEntry(row.id, row.title, row.tags, row.isFavorite, row.createdAt)
            entry to "Favorite Entry"
        }
    }

    fun getFeedWithPreviews(): List<Pair<JournalEntry, String>> {
        return journalQueries.getEntriesWithPreview().executeAsList().map { row ->
            val entry = JournalEntry(
                id = row.id,
                title = row.title,
                tags = row.tags,
                isFavorite = row.isFavorite,
                createdAt = row.createdAt
            )

            val snippet = row.previewContent
                ?.replace(Regex("<[^>]*>"), "")
                ?.replace("&nbsp;", " ")
                ?.trim() ?: ""

            entry to snippet
        }
    }

    fun saveTask(task: DailyTask) {
        journalQueries.insertTask(
            id = task.id,
            title = task.title,
            isCompleted = task.isCompleted,
            targetTime = task.targetTime?.toString(),
            createdAt = task.createdAt
        )
    }

    fun getAllTasks(): List<com.bennysamuel.livem.ui.DailyTask> {
        return journalQueries.getAllTasks().executeAsList().map { row ->
            DailyTask(
                id = row.id,
                title = row.title,
                isCompleted = row.isCompleted,
                targetTime = row.targetTime?.toString(),
                createdAt = row.createdAt
            ).toUIModel()
        }
    }


    fun getTodaysTasks(): List<com.bennysamuel.livem.ui.DailyTask> {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now().toLocalDateTime(tz)

        val startOfDay = LocalDateTime(now.year, now.month, now.dayOfMonth, 0, 0, 0)
            .toInstant(tz).toEpochMilliseconds()

        val endOfDay = LocalDateTime(now.year, now.month, now.dayOfMonth, 23, 59, 59)
            .toInstant(tz).toEpochMilliseconds()

        return journalQueries.getTasksByDateRange(startOfDay, endOfDay).executeAsList().map { row ->
            DailyTask(
                id = row.id,
                title = row.title,
                isCompleted = row.isCompleted,
                targetTime = row.targetTime,
                createdAt = row.createdAt
            ).toUIModel()
        }
    }

    fun insertTask(task: com.bennysamuel.livem.ui.DailyTask) {
        journalQueries.insertTask(
            id = task.id,
            title = task.title,
            isCompleted = task.isCompleted,
            targetTime = task.targetTime?.toString(),
            createdAt = task.createdAt
        )
    }

    fun updateTaskStatus(id: String, isCompleted: Boolean) {
        journalQueries.updateTaskStatus(isCompleted, id)
    }

    fun getTaskHistory(): List<com.bennysamuel.livem.ui.DailyTask> {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now().toLocalDateTime(tz)

        val startOfToday = LocalDateTime(now.year, now.month, now.dayOfMonth, 0, 0, 0)
            .toInstant(tz).toEpochMilliseconds()

        return journalQueries.getAllTasks().executeAsList()
            .filter { it.isCompleted }
            .filter { it.createdAt < startOfToday }
            .map { it.toUIModel() }
    }


    fun getTodayEntryId(): String? {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now().toLocalDateTime(tz)

        val startOfDay = LocalDateTime(now.year, now.month, now.dayOfMonth, 0, 0, 0)
            .toInstant(tz).toEpochMilliseconds()

        val endOfDay = LocalDateTime(now.year, now.month, now.dayOfMonth, 23, 59, 59)
            .toInstant(tz).toEpochMilliseconds()

        return journalQueries.getTodayEntryId(startOfDay, endOfDay).executeAsOneOrNull()
    }



    fun saveReport(report: ReviewReport) {
        journalQueries.insertReport(
            id = report.id,
            reportType = report.reportType,
            content = report.content,
            startDate = report.startDate,
            endDate = report.endDate,
            createdAt = report.createdAt
        )
    }

    fun getAllReports(): List<ReviewReport> {
        return journalQueries.getAllReports().executeAsList()
    }

    fun getLastReportEndDate(type: String): Long? {
        return journalQueries.getLatestReportByType(type).executeAsOneOrNull()?.endDate
    }

    fun getAllDataForAi(): String {
        val tz = TimeZone.currentSystemDefault()

        val allJournals = journalQueries.getEntriesWithPreview().executeAsList()
            .map {
                it.createdAt to "DATE: ${formatDate(it.createdAt, tz)} | TYPE: JOURNAL | TITLE: ${it.title}\nCONTENT: ${it.previewContent?.take(500) ?: "No text"}"
            }

        val allTasks = journalQueries.getAllTasks().executeAsList()
            .map {
                it.createdAt to "DATE: ${formatDate(it.createdAt, tz)} | TYPE: MISSION | TASK: ${it.title} (Status: ${if (it.isCompleted) "Completed" else "Incomplete"})"
            }

        return (allJournals + allTasks)
            .sortedBy { it.first }
            .joinToString("\n\n---\n\n") { it.second }
    }

    private fun formatDate(ms: Long, tz: TimeZone): String {
        val dt = Instant.fromEpochMilliseconds(ms).toLocalDateTime(tz)
        return "${dt.year}-${dt.monthNumber.toString().padStart(2, '0')}-${dt.dayOfMonth.toString().padStart(2, '0')}"
    }

}

fun com.bennysamuel.liveem.db.DailyTask.toUIModel(): com.bennysamuel.livem.ui.DailyTask {
    return com.bennysamuel.livem.ui.DailyTask(
        id = this.id,
        title = this.title,
        isCompleted = this.isCompleted,
        targetTime = this.targetTime?.let { LocalTime.parse(it) },
        createdAt = this.createdAt
    )
}