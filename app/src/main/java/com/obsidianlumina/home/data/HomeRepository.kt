package com.obsidianlumina.home.data

import android.content.Context
import com.obsidianlumina.home.model.AutomationRule
import com.obsidianlumina.home.model.BridgeConfig
import com.obsidianlumina.home.model.BridgeMode
import com.obsidianlumina.home.model.DeviceType
import com.obsidianlumina.home.model.DispatchEvent
import com.obsidianlumina.home.model.HomeDevice
import com.obsidianlumina.home.model.InitialHomeData
import com.obsidianlumina.home.model.RoomId
import com.obsidianlumina.home.model.SignalTone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeRepository(context: Context) {
    private val prefs = context.getSharedPreferences("obsidian_lumina_prefs", Context.MODE_PRIVATE)
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun loadDevices(): List<HomeDevice> {
        val raw = prefs.getString("devices_json", null) ?: return InitialHomeData.devices
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val o = arr.getJSONObject(idx)
                HomeDevice(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    roomId = RoomId.fromId(o.getString("roomId")),
                    type = DeviceType.valueOf(o.getString("type")),
                    isOn = o.getBoolean("isOn"),
                    signalTone = SignalTone.valueOf(o.getString("signalTone")),
                    brightness = o.optInt("brightness", 75),
                    kelvin = o.optInt("kelvin", 3200),
                    fanSpeed = o.optInt("fanSpeed", 2),
                    targetTemp = o.optInt("targetTemp", 22),
                    currentTemp = o.optInt("currentTemp", 23),
                    position = o.optInt("position", 80),
                    watts = o.optDouble("watts", 18.0),
                    standbyWatts = o.optDouble("standbyWatts", 0.4),
                    ip = o.optString("ip", "192.168.1.40"),
                    mac = o.optString("mac", "A4:C1:38:09:2B:01"),
                    channel = o.optString("channel", "CH-01"),
                    latencyMs = o.optInt("latencyMs", 5),
                    haEntityId = o.optString("haEntityId", ""),
                    webhookOnUrl = o.optString("webhookOnUrl", ""),
                    webhookOffUrl = o.optString("webhookOffUrl", "")
                )
            }
        } catch (_: Exception) {
            InitialHomeData.devices
        }
    }

    fun saveDevices(devices: List<HomeDevice>) {
        val arr = JSONArray()
        devices.forEach { d ->
            arr.put(JSONObject().apply {
                put("id", d.id)
                put("name", d.name)
                put("roomId", d.roomId.id)
                put("type", d.type.name)
                put("isOn", d.isOn)
                put("signalTone", d.signalTone.name)
                put("brightness", d.brightness)
                put("kelvin", d.kelvin)
                put("fanSpeed", d.fanSpeed)
                put("targetTemp", d.targetTemp)
                put("currentTemp", d.currentTemp)
                put("position", d.position)
                put("watts", d.watts)
                put("standbyWatts", d.standbyWatts)
                put("ip", d.ip)
                put("mac", d.mac)
                put("channel", d.channel)
                put("latencyMs", d.latencyMs)
                put("haEntityId", d.haEntityId)
                put("webhookOnUrl", d.webhookOnUrl)
                put("webhookOffUrl", d.webhookOffUrl)
            })
        }
        prefs.edit().putString("devices_json", arr.toString()).apply()
    }

    fun loadAutomations(): List<AutomationRule> {
        val raw = prefs.getString("automations_json", null) ?: return InitialHomeData.automations
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val o = arr.getJSONObject(idx)
                AutomationRule(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    triggerType = o.optString("triggerType", "time"),
                    triggerValue = o.getString("triggerValue"),
                    targetSummary = o.getString("targetSummary"),
                    enabled = o.getBoolean("enabled"),
                    sceneId = o.optString("sceneId").takeIf { it.isNotBlank() },
                    lastRun = o.optString("lastRun").takeIf { it.isNotBlank() }
                )
            }
        } catch (_: Exception) {
            InitialHomeData.automations
        }
    }

    fun saveAutomations(rules: List<AutomationRule>) {
        val arr = JSONArray()
        rules.forEach { r ->
            arr.put(JSONObject().apply {
                put("id", r.id)
                put("name", r.name)
                put("triggerType", r.triggerType)
                put("triggerValue", r.triggerValue)
                put("targetSummary", r.targetSummary)
                put("enabled", r.enabled)
                put("sceneId", r.sceneId ?: "")
                put("lastRun", r.lastRun ?: "")
            })
        }
        prefs.edit().putString("automations_json", arr.toString()).apply()
    }

    fun loadBridgeConfig(): BridgeConfig {
        val raw = prefs.getString("bridge_json", null) ?: return BridgeConfig()
        return try {
            val o = JSONObject(raw)
            BridgeConfig(
                homeName = o.optString("homeName", "Obsidian Residence"),
                mode = BridgeMode.valueOf(o.optString("mode", BridgeMode.LOCAL.name)),
                haUrl = o.optString("haUrl", "http://homeassistant.local:8123"),
                haToken = o.optString("haToken", ""),
                gridVoltage = o.optInt("gridVoltage", 230),
                currency = o.optString("currency", "$"),
                ratePerKwh = o.optDouble("ratePerKwh", 0.16)
            )
        } catch (_: Exception) {
            BridgeConfig()
        }
    }

    fun saveBridgeConfig(config: BridgeConfig) {
        val o = JSONObject().apply {
            put("homeName", config.homeName)
            put("mode", config.mode.name)
            put("haUrl", config.haUrl)
            put("haToken", config.haToken)
            put("gridVoltage", config.gridVoltage)
            put("currency", config.currency)
            put("ratePerKwh", config.ratePerKwh)
        }
        prefs.edit().putString("bridge_json", o.toString()).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    suspend fun dispatchCommand(
        device: HomeDevice,
        nextState: Boolean,
        bridge: BridgeConfig,
        brightnessPct: Int? = null
    ): DispatchEvent = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        val timestamp = timeFormat.format(Date())
        val actionLabel = when {
            device.type == DeviceType.LOCK -> if (nextState) "LOCK" else "UNLOCK"
            brightnessPct != null -> "DIM $brightnessPct%"
            nextState -> "RELAY ON"
            else -> "RELAY OFF"
        }

        if (bridge.mode == BridgeMode.HOME_ASSISTANT &&
            bridge.haUrl.isNotBlank() &&
            bridge.haToken.isNotBlank() &&
            device.haEntityId.isNotBlank()
        ) {
            val domain = device.haEntityId.substringBefore('.', "homeassistant")
            val service = when {
                device.type == DeviceType.LOCK -> if (nextState) "lock" else "unlock"
                nextState -> "turn_on"
                else -> "turn_off"
            }
            val cleanUrl = bridge.haUrl.trimEnd('/')
            return@withContext try {
                val conn = (URL("$cleanUrl/api/services/$domain/$service").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 3000
                    readTimeout = 3000
                    doOutput = true
                    setRequestProperty("Authorization", "Bearer ${bridge.haToken}")
                    setRequestProperty("Content-Type", "application/json")
                }
                val payload = JSONObject().apply {
                    put("entity_id", device.haEntityId)
                    if (nextState && brightnessPct != null) {
                        put("brightness_pct", brightnessPct)
                    }
                }
                OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
                val code = conn.responseCode
                conn.disconnect()
                val latency = (System.currentTimeMillis() - start).toInt().coerceAtLeast(1)
                DispatchEvent(
                    id = "${System.nanoTime()}",
                    timestamp = timestamp,
                    deviceName = device.name,
                    action = actionLabel,
                    transport = "HA REST",
                    latencyMs = latency,
                    ok = code in 200..299
                )
            } catch (_: Exception) {
                val latency = (System.currentTimeMillis() - start).toInt().coerceAtLeast(1)
                DispatchEvent(
                    id = "${System.nanoTime()}",
                    timestamp = timestamp,
                    deviceName = device.name,
                    action = "$actionLabel (Local)",
                    transport = "HA Offline",
                    latencyMs = latency,
                    ok = false
                )
            }
        }

        val targetUrl = if (nextState) device.webhookOnUrl else device.webhookOffUrl
        if ((bridge.mode == BridgeMode.WEBHOOK || targetUrl.isNotBlank()) && targetUrl.isNotBlank()) {
            return@withContext try {
                val conn = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 3000
                    readTimeout = 3000
                }
                val code = conn.responseCode
                conn.disconnect()
                val latency = (System.currentTimeMillis() - start).toInt().coerceAtLeast(1)
                DispatchEvent(
                    id = "${System.nanoTime()}",
                    timestamp = timestamp,
                    deviceName = device.name,
                    action = actionLabel,
                    transport = "LAN HTTP",
                    latencyMs = latency,
                    ok = code in 200..299
                )
            } catch (_: Exception) {
                val latency = (System.currentTimeMillis() - start).toInt().coerceAtLeast(1)
                DispatchEvent(
                    id = "${System.nanoTime()}",
                    timestamp = timestamp,
                    deviceName = device.name,
                    action = actionLabel,
                    transport = "LAN Error",
                    latencyMs = latency,
                    ok = false
                )
            }
        }

        DispatchEvent(
            id = "${System.nanoTime()}",
            timestamp = timestamp,
            deviceName = device.name,
            action = actionLabel,
            transport = "Local Bus",
            latencyMs = device.latencyMs,
            ok = true
        )
    }

    suspend fun testHomeAssistant(haUrl: String, haToken: String): String = withContext(Dispatchers.IO) {
        if (haUrl.isBlank() || haToken.isBlank()) {
            return@withContext "Enter your Home Assistant URL and Long-Lived Access Token."
        }
        return@withContext try {
            val cleanUrl = haUrl.trimEnd('/')
            val conn = (URL("$cleanUrl/api/").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3500
                readTimeout = 3500
                setRequestProperty("Authorization", "Bearer $haToken")
            }
            val code = conn.responseCode
            conn.disconnect()
            if (code in 200..299) {
                "Connected to Home Assistant REST API."
            } else {
                "Home Assistant returned HTTP $code. Check token."
            }
        } catch (e: Exception) {
            "Connection failed: ${e.localizedMessage ?: "Host unreachable"}"
        }
    }
}
