package com.alhaq.amnshield.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

object DndHelper {
    private const val TAG = "DndHelper"
    private const val PREF_WAS_TURNED_ON_BY_US = "dnd_was_turned_on_by_us"

    fun hasDndAccess(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.isNotificationPolicyAccessGranted
    }

    fun openDndPermissionSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open DND settings: ${e.message}")
        }
    }

    fun setDndMode(context: Context, enable: Boolean) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) return

        val prefs = context.getSharedPreferences("dnd_prefs", Context.MODE_PRIVATE)

        try {
            if (enable) {
                val currentFilter = nm.currentInterruptionFilter
                if (currentFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
                    nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                    prefs.edit().putBoolean(PREF_WAS_TURNED_ON_BY_US, true).apply()
                }
            } else {
                val wasTurnedOnByUs = prefs.getBoolean(PREF_WAS_TURNED_ON_BY_US, false)
                val currentFilter = nm.currentInterruptionFilter
                if (wasTurnedOnByUs && currentFilter != NotificationManager.INTERRUPTION_FILTER_ALL) {
                    nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    prefs.edit().putBoolean(PREF_WAS_TURNED_ON_BY_US, false).apply()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling DND filter: ${e.message}")
        }
    }
}
