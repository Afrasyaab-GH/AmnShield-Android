package com.alhaq.amnshield.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alhaq.amnshield.utils.NotificationHelper

class SmartNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val nudge = SmartBehavioralBrain.checkDoomscrollSpike(context)
                ?: SmartBehavioralBrain.checkPreBlockWarning(context)

            if (nudge != null) {
                val notificationHelper = NotificationHelper.getInstance(context)
                notificationHelper.showFocusReminder(nudge.title, nudge.message)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
