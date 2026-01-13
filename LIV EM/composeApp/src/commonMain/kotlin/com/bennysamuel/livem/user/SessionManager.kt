package com.bennysamuel.livem.user

import com.russhwolf.settings.Settings
import com.russhwolf.settings.contains

import kotlinx.datetime.*
import kotlin.time.Clock

class SessionManager(
    private val settings: Settings
) {
    init {
        updateStreak()

    }
    companion object {
        private const val KEY_NAME = "user_name"
        private const val KEY_DOB = "user_dob"
        private const val KEY_LAST_OPEN = "last_open_date"
        private const val KEY_STREAK = "current_streak"
        private const val KEY_JOIN_DATE = "user_join_date"
    }

    fun getJoinDate(): Long {
        return settings.getLong(KEY_JOIN_DATE, Clock.System.now().toEpochMilliseconds())
    }

    fun isSignedIn(): Boolean = settings.contains(KEY_NAME)

    fun signIn(name: String, dob: String) {
        settings.putString(KEY_NAME, name)
        settings.putString(KEY_DOB, dob)

        if (!settings.contains(KEY_JOIN_DATE)) {
            val now = Clock.System.now().toEpochMilliseconds()
            settings.putLong(KEY_JOIN_DATE, now)
        }

        updateStreak()
    }

    fun getUser(): UserProfile? {

        val name = settings.getStringOrNull(KEY_NAME)

        val dob = settings.getStringOrNull(KEY_DOB)

        return if (name != null && dob != null) UserProfile(name, dob, getStreak()) else null

    }
    fun updateStreak() {
        if (!isSignedIn()) return

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val lastOpenStr = settings.getStringOrNull(KEY_LAST_OPEN)
        val currentStreak = settings.getInt(KEY_STREAK, 0)

        if (lastOpenStr == null) {
            saveStreak(now, 1)
            return
        }

        val lastDate = LocalDate.parse(lastOpenStr)
        val daysBetween = now.toEpochDays() - lastDate.toEpochDays()

        when {
            daysBetween <= 0L -> {

            }
            daysBetween == 1L -> {
                saveStreak(now, currentStreak + 1)
            }
            else -> {
                saveStreak(now, 1)
            }
        }
    }

    private fun saveStreak(date: LocalDate, count: Int) {
        settings.putString(KEY_LAST_OPEN, date.toString())
        settings.putInt(KEY_STREAK, count)
    }

    fun getStreak(): Int = settings.getInt(KEY_STREAK, 0)

    fun signOut() {
        settings.clear()
    }
}