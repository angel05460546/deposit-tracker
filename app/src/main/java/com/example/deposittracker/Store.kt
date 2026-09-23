package com.example.deposittracker

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Entry(val date: String, val amount: Long, val type: String, val source: String)

object Store {
    private const val PREFS = "deposit_tracker_prefs"
    private const val KEY = "entries_json"

    fun getEntries(context: Context): List<Entry> {
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

    fun addEntry(context: Context, amount: Long, type: String, source: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString(KEY, "[]") ?: "[]")
        val entry = JSONObject()
        val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
            .format(java.util.Date())
        entry.put("date", date)
        entry.put("amount", amount)
        entry.put("type", type)
        entry.put("source", source)
        arr.put(entry)
        prefs.edit().putString(KEY, arr.toString()).apply()
    }
}
