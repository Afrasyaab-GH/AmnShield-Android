package com.alhaq.deenshield.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Lightweight on-device store for in-app notifications shown by DeenShield
 * (daily reports, focus reminders, achievements, block alerts, etc.).
 *
 * The toolbar bell icon opens a screen backed by this store so the user sees
 * an actual history of notifications instead of the reminder settings screen.
 *
 * All data lives in SharedPreferences as a JSON list and never leaves the device,
 * matching the rest of the app's privacy-first architecture.
 */
object NotificationInboxStore {

    private const val PREFS = "notification_inbox"
    private const val KEY_ITEMS = "items"
    private const val MAX_ITEMS = 100

    enum class Category {
        DAILY_REPORT,
        REMINDER,
        ACHIEVEMENT,
        BLOCK_ALERT,
        SYSTEM
    }

    data class InboxNotification(
        val id: Long = System.currentTimeMillis(),
        val title: String,
        val message: String,
        val category: Category = Category.SYSTEM,
        val timestamp: Long = System.currentTimeMillis(),
        var read: Boolean = false
    )

    private val gson = Gson()
    private val listType = object : TypeToken<MutableList<InboxNotification>>() {}.type

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    @Synchronized
    fun add(context: Context, title: String, message: String, category: Category) {
        val list = getAll(context).toMutableList()
        list.add(0, InboxNotification(title = title, message = message, category = category))
        // Cap history so prefs can't grow unbounded.
        if (list.size > MAX_ITEMS) {
            list.subList(MAX_ITEMS, list.size).clear()
        }
        prefs(context).edit().putString(KEY_ITEMS, gson.toJson(list)).apply()
    }

    fun getAll(context: Context): List<InboxNotification> {
        val raw = prefs(context).getString(KEY_ITEMS, null) ?: return emptyList()
        return try {
            gson.fromJson<MutableList<InboxNotification>>(raw, listType) ?: emptyList()
        } catch (_: Throwable) {
            // Corrupt JSON shouldn't crash the inbox screen.
            emptyList()
        }
    }

    fun unreadCount(context: Context): Int = getAll(context).count { !it.read }

    @Synchronized
    fun markAllRead(context: Context) {
        val list = getAll(context).toMutableList()
        if (list.isEmpty()) return
        list.forEach { it.read = true }
        prefs(context).edit().putString(KEY_ITEMS, gson.toJson(list)).apply()
    }

    @Synchronized
    fun clearAll(context: Context) {
        prefs(context).edit().remove(KEY_ITEMS).apply()
    }
}
