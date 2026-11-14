package com.obsidianlumina.home.data

import android.content.Context
import com.obsidianlumina.home.model.DefaultBoard
import com.obsidianlumina.home.model.HomeSwitch
import com.obsidianlumina.home.model.SwitchType
import com.obsidianlumina.home.model.SwitchboardState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SwitchRepository(context: Context) {
    private val prefs = context.getSharedPreferences("lumina_living_board_v2", Context.MODE_PRIVATE)

    fun load(): SwitchboardState {
        val raw = prefs.getString("board_json", null) ?: return SwitchboardState()
        return try {
            val root = JSONObject(raw)
            val arr = root.getJSONArray("switches")
            val list = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val num = o.getInt("number")
                val defaultIr = DefaultBoard.switches.firstOrNull { it.number == num }?.irCode ?: ""
                HomeSwitch(
                    number = num,
                    name = o.getString("name"),
                    type = SwitchType.valueOf(o.getString("type")),
                    isOn = o.getBoolean("isOn"),
                    onUrl = o.optString("onUrl", ""),
                    offUrl = o.optString("offUrl", ""),
                    irCode = o.optString("irCode", defaultIr)
                )
            }
            SwitchboardState(
                switches = if (list.size == 8) list else DefaultBoard.switches,
                level = root.optInt("level", 3).coerceIn(1, 5),
                plusUrl = root.optString("plusUrl", ""),
                minusUrl = root.optString("minusUrl", ""),
                plusIrCode = root.optString("plusIrCode", "0x00FF09F6"),
                minusIrCode = root.optString("minusIrCode", "0x00FF0AF5"),
                allOnIrCode = root.optString("allOnIrCode", "0x00FF0BF4"),
                allOffIrCode = root.optString("allOffIrCode", "0x00FF0CF3")
            )
        } catch (_: Exception) {
            SwitchboardState()
        }
    }

    fun save(state: SwitchboardState) {
        val arr = JSONArray()
        state.switches.forEach { s ->
            arr.put(JSONObject().apply {
                put("number", s.number)
                put("name", s.name)
                put("type", s.type.name)
                put("isOn", s.isOn)
                put("onUrl", s.onUrl)
                put("offUrl", s.offUrl)
                put("irCode", s.irCode)
            })
        }
        val root = JSONObject().apply {
            put("switches", arr)
            put("level", state.level)
            put("plusUrl", state.plusUrl)
            put("minusUrl", state.minusUrl)
            put("plusIrCode", state.plusIrCode)
            put("minusIrCode", state.minusIrCode)
            put("allOnIrCode", state.allOnIrCode)
            put("allOffIrCode", state.allOffIrCode)
        }
        prefs.edit().putString("board_json", root.toString()).apply()
    }

    suspend fun triggerUrl(url: String) = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext
        try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 2500
                readTimeout = 2500
            }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
            // Keep local board responsive if device is offline
        }
    }
}
