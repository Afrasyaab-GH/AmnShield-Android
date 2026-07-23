package com.alhaq.amnshield.notifications

import android.content.Context
import com.alhaq.amnshield.utils.ReelsStatsManager
import com.alhaq.amnshield.utils.ReportGenerator
import com.alhaq.amnshield.utils.SavedPreferencesLoader
import com.alhaq.amnshield.utils.TimeTools
import java.time.LocalDate
import java.util.Calendar

object SmartBehavioralBrain {

    data class SmartNudge(
        val title: String,
        val message: String,
        val actionType: ActionType,
        val targetPackage: String? = null
    )

    enum class ActionType {
        START_FOCUS,
        SNOOZE_BLOCK,
        QUICK_BLOCK_APP
    }

    /**
     * Checks if user has been doomscrolling Reels/Shorts excessively in the recent window
     */
    fun checkDoomscrollSpike(context: Context): SmartNudge? {
        val today = TimeTools.getCurrentDate()
        val record = ReelsStatsManager.getInstance(context).loadDailyRecord(today)
        val count = record.totalScrolled
        val timeSeconds = record.totalWatchTimeSeconds

        // Threshold: 35+ Reels or 20+ minutes of scrolling
        if (count >= 35 || timeSeconds >= 1200) {
            val minutes = timeSeconds / 60
            return SmartNudge(
                title = "🚨 Doomscroll Spike Detected!",
                message = "You've scrolled $count Reels ($minutes mins today). Take a 15-min Focus Break?",
                actionType = ActionType.START_FOCUS
            )
        }
        return null
    }

    /**
     * Checks if an App Blocker schedule rule starts within the next 5 minutes
     */
    fun checkPreBlockWarning(context: Context): SmartNudge? {
        val loader = SavedPreferencesLoader(context)
        val rules = loader.loadAppBlockerScheduleRules()
        if (rules.isEmpty()) return null

        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_WEEK)
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val nowMinuteOfDay = currentHour * 60 + currentMinute

        for (rule in rules) {
            if (!rule.isRuleEnabled) continue

            if (rule.selectedDays.isEmpty() || rule.selectedDays.contains(currentDay)) {
                val startMinuteOfDay = rule.startMinute
                val diff = startMinuteOfDay - nowMinuteOfDay
                if (diff in 1..5) {
                    val appName = rule.title.ifEmpty { rule.packageName }
                    return SmartNudge(
                        title = "⏳ App Blocker Starts in $diff Mins",
                        message = "Scheduled blocking for $appName starts soon.",
                        actionType = ActionType.SNOOZE_BLOCK
                    )
                }
            }
        }
        return null
    }

    /**
     * Computes daily productivity insights & streak highlights
     */
    fun generateDailyInsight(context: Context, date: LocalDate = LocalDate.now()): String {
        val reportGenerator = ReportGenerator(context)
        val summary = reportGenerator.generateDailySummaryText(date)
        val record = ReelsStatsManager.getInstance(context).loadDailyRecord(date.toString())

        return buildString {
            append(summary)
            if (record.totalScrolled > 0) {
                append("\n• Reels/Shorts: ${record.totalScrolled} scrolled today")
            }
        }
    }
}
