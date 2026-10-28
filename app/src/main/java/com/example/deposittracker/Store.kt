package com.example.deposittracker

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Entry(val date: String, val amount: Long, val type: String, val source: String)

data class Goal(val amount: Long, val days: Int, val start: String)

object Store {
    private const val PREFS = "deposit_tracker_prefs"
    private const val KEY = "entries_json"
    private const val GOAL_AMOUNT = "goal2_amount"
    private const val GOAL_DAYS = "goal2_days"
    private const val GOAL_START = "goal2_start"
    private val lock = Any()

    fun getEntries(context: Context): List<Entry> {
        synchronized(lock) {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY, "[]") ?: "[]"
            val arr = JSONArray(raw)
            val list = mutableListOf<Entry>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    Entry(
                        o.getString("date"),
                        o.getLong("amount"),
                        o.optString("type", "واریز"),
                        o.optString("source", "پیامک")
                    )
                )
            }
            return list
        }
    }

    fun addEntry(context: Context, amount: Long, type: String, source: String) {
        synchronized(lock) {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val arr = JSONArray(prefs.getString(KEY, "[]") ?: "[]")
            val entry = JSONObject()
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            entry.put("date", date)
            entry.put("amount", amount)
            entry.put("type", type)
            entry.put("source", source)
            arr.put(entry)
            prefs.edit().putString(KEY, arr.toString()).commit()
        }
    }

    fun clearAll(context: Context) {
        synchronized(lock) {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY, "[]").commit()
        }
    }

    fun getGoal(context: Context): Goal? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val amount = prefs.getLong(GOAL_AMOUNT, -1L)
        val days = prefs.getInt(GOAL_DAYS, -1)
        val start = prefs.getString(GOAL_START, null)
        return if (amount > 0 && days > 0 && start != null) Goal(amount, days, start) else null
    }

    // شروع برنامه = ساعت ۰۰:۰۰ امروز
    fun setGoal(context: Context, amount: Long, days: Int) {
        val start = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) + " 00:00:00"
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putLong(GOAL_AMOUNT, amount)
            .putInt(GOAL_DAYS, days)
            .putString(GOAL_START, start)
            .apply()
    }

    fun clearGoal(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().remove(GOAL_AMOUNT).remove(GOAL_DAYS).remove(GOAL_START).apply()
    }
}
