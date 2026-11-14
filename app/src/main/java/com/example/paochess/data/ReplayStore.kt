package com.example.paochess.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class SavedMove(val fromF: Int, val fromR: Int, val toF: Int, val toR: Int)

data class SavedGame(
    val id: String,
    val date: Long,
    val result: String,
    val moves: List<SavedMove>,
    val durationMs: Long
)

object ReplayStore {
    private const val FILE = "chess_replays.json"

    fun save(context: Context, game: SavedGame) {
        val list = load(context).toMutableList()
        list.add(0, game)
        write(context, list.take(100))
    }

    fun load(context: Context): List<SavedGame> {
        val file = File(context.filesDir, FILE)
        if (!file.exists()) return emptyList()
        return try {
            val arr = JSONArray(file.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val movesArr = o.getJSONArray("moves")
                val moves = (0 until movesArr.length()).map { j ->
                    val m = movesArr.getJSONObject(j)
                    SavedMove(m.getInt("fromF"), m.getInt("fromR"), m.getInt("toF"), m.getInt("toR"))
                }
                SavedGame(
                    o.getString("id"), o.getLong("date"), o.getString("result"),
                    moves, o.getLong("durationMs")
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    fun delete(context: Context, id: String) {
        val list = load(context).filter { it.id != id }
        write(context, list)
    }

    fun clearAll(context: Context) = write(context, emptyList())

    private fun write(context: Context, list: List<SavedGame>) {
        val arr = JSONArray()
        list.forEach { g ->
            val o = JSONObject()
            o.put("id", g.id)
            o.put("date", g.date)
            o.put("result", g.result)
            o.put("durationMs", g.durationMs)
            val movesArr = JSONArray()
            g.moves.forEach { m ->
                val mo = JSONObject()
                mo.put("fromF", m.fromF); mo.put("fromR", m.fromR)
                mo.put("toF", m.toF); mo.put("toR", m.toR)
                movesArr.put(mo)
            }
            o.put("moves", movesArr)
            arr.put(o)
        }
        File(context.filesDir, FILE).writeText(arr.toString())
    }
}
