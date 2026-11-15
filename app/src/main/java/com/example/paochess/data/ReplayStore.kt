package com.example.paochess.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class SavedReplay(val id: String, val date: Long, val result: String, val movesJson: String)

object ReplayStore {
    private const val FILE = "saved_replays.json"
    fun save(context: Context, id: String, result: String, movesJson: String) {
        val list = load(context).toMutableList()
        list.add(0, SavedReplay(id, System.currentTimeMillis(), result, movesJson))
        write(context, list.take(50))
    }
    fun load(context: Context): List<SavedReplay> {
        val file = File(context.filesDir, FILE)
        if (!file.exists()) return emptyList()
        return try {
            val arr = JSONArray(file.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                SavedReplay(o.getString("id"), o.getLong("date"), o.getString("result"), o.getString("movesJson"))
            }
        } catch (e: Exception) { emptyList() }
    }
    fun delete(context: Context, id: String) { write(context, load(context).filter { it.id != id }) }
    private fun write(context: Context, list: List<SavedReplay>) {
        val arr = JSONArray()
        list.forEach { r ->
            val o = JSONObject()
            o.put("id", r.id); o.put("date", r.date); o.put("result", r.result); o.put("movesJson", r.movesJson)
            arr.put(o)
        }
        File(context.filesDir, FILE).writeText(arr.toString())
    }
}
