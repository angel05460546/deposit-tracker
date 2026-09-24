package com.example.deposittracker

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Entry(val date: String, val amount: Long, val type: String, val source: String)

object Store {
    private const val PREFS = "deposit_tracker_prefs"
    private const val KEY = "entries_json"
    private const val GOAL_KEY = "goal_amount"
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
            val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                .format(java.util.Date())
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

    fun getGoal(context: Context): Long? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val value = prefs.getLong(GOAL_KEY, -1L)
        return if (value < 0) null else value
    }

    fun setGoal(context: Context, amount: Long?) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (amount == null) {
            prefs.edit().remove(GOAL_KEY).apply()
        } else {
            prefs.edit().putLong(GOAL_KEY, amount).apply()
        }
    }
}
