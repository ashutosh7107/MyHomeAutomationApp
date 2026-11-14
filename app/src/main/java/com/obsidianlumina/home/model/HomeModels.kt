package com.obsidianlumina.home.model

enum class RoomId(val id: String, val displayName: String, val shortName: String) {
    ALL("all", "All Zones", "All"),
    LIVING("living", "Living Room", "Living"),
    KITCHEN("kitchen", "Kitchen & Dining", "Kitchen"),
    BEDROOM("bedroom", "Master Suite", "Suite"),
    STUDIO("studio", "Home Studio", "Studio"),
    OUTDOOR("outdoor", "Entry & Terrace", "Outdoor");

    companion object {
        fun fromId(id: String): RoomId = entries.find { it.id == id } ?: LIVING
    }
}

enum class DeviceType {
    LIGHT, DIMMER, FAN, CLIMATE, PLUG, BLIND, LOCK
}

enum class SignalTone {
    CYAN, AMBER, EMERALD
}

enum class BridgeMode(val label: String) {
    LOCAL("Local Bus"),
    HOME_ASSISTANT("Home Assistant"),
    WEBHOOK("LAN Webhooks")
}

data class HomeDevice(
    val id: String,
    val name: String,
    val roomId: RoomId,
    val type: DeviceType,
    val isOn: Boolean,
    val signalTone: SignalTone,
    val brightness: Int = 75,
    val kelvin: Int = 3200,
    val fanSpeed: Int = 2,
    val targetTemp: Int = 22,
    val currentTemp: Int = 23,
    val position: Int = 80,
    val watts: Double = 18.0,
    val standbyWatts: Double = 0.4,
    val ip: String = "192.168.1.40",
    val mac: String = "A4:C1:38:09:2B:01",
    val channel: String = "CH-01",
    val latencyMs: Int = 5,
    val haEntityId: String = "",
    val webhookOnUrl: String = "",
    val webhookOffUrl: String = ""
) {
    fun currentWatts(): Double {
        if (!isOn) return standbyWatts
        return if (type == DeviceType.DIMMER) {
            watts * (brightness / 100.0)
        } else {
            watts
        }
    }

    fun secondaryLabel(): String {
        if (type == DeviceType.LOCK) {
            return if (isOn) "Locked" else "Unlocked"
        }
        if (!isOn) {
            return if (type == DeviceType.BLIND) "Closed · 0%" else "Standby"
        }
        return when (type) {
            DeviceType.DIMMER -> "$brightness% · ${kelvin}K"
            DeviceType.FAN -> "Speed $fanSpeed"
            DeviceType.CLIMATE -> "${targetTemp}°C target"
            DeviceType.BLIND -> "Open $position%"
            else -> "Active"
        }
    }
}

data class SceneAction(
    val deviceId: String,
    val isOn: Boolean,
    val brightness: Int? = null,
    val targetTemp: Int? = null,
    val position: Int? = null,
    val fanSpeed: Int? = null
)

data class HomeScene(
    val id: String,
    val name: String,
    val subtitle: String,
    val accent: SignalTone,
    val actions: List<SceneAction>
)

data class AutomationRule(
    val id: String,
    val name: String,
    val triggerType: String,
    val triggerValue: String,
    val targetSummary: String,
    val enabled: Boolean,
    val sceneId: String? = null,
    val lastRun: String? = null
)

data class BridgeConfig(
    val homeName: String = "Obsidian Residence",
    val mode: BridgeMode = BridgeMode.LOCAL,
    val haUrl: String = "http://homeassistant.local:8123",
    val haToken: String = "",
    val gridVoltage: Int = 230,
    val currency: String = "$",
    val ratePerKwh: Double = 0.16
)

data class DispatchEvent(
    val id: String,
    val timestamp: String,
    val deviceName: String,
    val action: String,
    val transport: String,
    val latencyMs: Int,
    val ok: Boolean
)

object InitialHomeData {
    val devices: List<HomeDevice> = listOf(
        HomeDevice(
            id = "dev-living-cove",
            name = "Ceiling Cove",
            roomId = RoomId.LIVING,
            type = DeviceType.DIMMER,
            isOn = true,
            signalTone = SignalTone.AMBER,
            brightness = 72,
            kelvin = 3200,
            watts = 38.4,
            ip = "192.168.1.41",
            mac = "A4:C1:38:09:2B:11",
            channel = "CH-01",
            latencyMs = 4,
            haEntityId = "light.living_ceiling_cove"
        ),
        HomeDevice(
            id = "dev-living-track",
            name = "Track Spots",
            roomId = RoomId.LIVING,
            type = DeviceType.LIGHT,
            isOn = true,
            signalTone = SignalTone.CYAN,
            watts = 24.0,
            ip = "192.168.1.42",
            mac = "A4:C1:38:09:2B:12",
            channel = "CH-02",
            latencyMs = 5,
            haEntityId = "light.living_track_spots"
        ),
        HomeDevice(
            id = "dev-living-fan",
            name = "Ceiling Fan",
            roomId = RoomId.LIVING,
            type = DeviceType.FAN,
            isOn = true,
            signalTone = SignalTone.CYAN,
            fanSpeed = 2,
            watts = 42.0,
            ip = "192.168.1.43",
            mac = "A4:C1:38:09:2B:13",
            channel = "CH-03",
            latencyMs = 6,
            haEntityId = "fan.living_ceiling"
        ),
        HomeDevice(
            id = "dev-living-ac",
            name = "Living Climate",
            roomId = RoomId.LIVING,
            type = DeviceType.CLIMATE,
            isOn = true,
            signalTone = SignalTone.CYAN,
            targetTemp = 22,
            currentTemp = 23,
            watts = 940.0,
            standbyWatts = 2.1,
            ip = "192.168.1.44",
            mac = "B8:27:EB:44:10:08",
            channel = "IR-01",
            latencyMs = 9,
            haEntityId = "climate.living_ac"
        ),
        HomeDevice(
            id = "dev-living-blinds",
            name = "Motorized Sheers",
            roomId = RoomId.LIVING,
            type = DeviceType.BLIND,
            isOn = true,
            signalTone = SignalTone.CYAN,
            position = 80,
            watts = 0.8,
            standbyWatts = 0.8,
            ip = "192.168.1.45",
            mac = "B8:27:EB:44:10:19",
            channel = "MTR-01",
            latencyMs = 7,
            haEntityId = "cover.living_sheers"
        ),
        HomeDevice(
            id = "dev-living-media",
            name = "Media Console",
            roomId = RoomId.LIVING,
            type = DeviceType.PLUG,
            isOn = false,
            signalTone = SignalTone.CYAN,
            watts = 115.0,
            standbyWatts = 1.1,
            ip = "192.168.1.46",
            mac = "DC:A6:32:77:8A:04",
            channel = "RLY-04",
            latencyMs = 4,
            haEntityId = "switch.living_media_console"
        ),
        HomeDevice(
            id = "dev-kitchen-island",
            name = "Island Pendants",
            roomId = RoomId.KITCHEN,
            type = DeviceType.DIMMER,
            isOn = true,
            signalTone = SignalTone.AMBER,
            brightness = 85,
            kelvin = 2900,
            watts = 31.2,
            ip = "192.168.1.51",
            mac = "A4:C1:38:12:4C:01",
            channel = "CH-05",
            latencyMs = 5,
            haEntityId = "light.kitchen_island"
        ),
        HomeDevice(
            id = "dev-kitchen-cabinet",
            name = "Under-Cabinet",
            roomId = RoomId.KITCHEN,
            type = DeviceType.LIGHT,
            isOn = true,
            signalTone = SignalTone.AMBER,
            watts = 14.5,
            ip = "192.168.1.52",
            mac = "A4:C1:38:12:4C:02",
            channel = "CH-06",
            latencyMs = 4,
            haEntityId = "light.kitchen_cabinet"
        ),
        HomeDevice(
            id = "dev-kitchen-espresso",
            name = "Espresso Bar",
            roomId = RoomId.KITCHEN,
            type = DeviceType.PLUG,
            isOn = false,
            signalTone = SignalTone.AMBER,
            watts = 1280.0,
            ip = "192.168.1.53",
            mac = "DC:A6:32:77:8A:19",
            channel = "RLY-07",
            latencyMs = 6,
            haEntityId = "switch.kitchen_espresso"
        ),
        HomeDevice(
            id = "dev-bedroom-sconces",
            name = "Bedside Sconces",
            roomId = RoomId.BEDROOM,
            type = DeviceType.DIMMER,
            isOn = false,
            signalTone = SignalTone.AMBER,
            brightness = 40,
            kelvin = 2700,
            watts = 18.0,
            ip = "192.168.1.61",
            mac = "A4:C1:38:55:9E:01",
            channel = "CH-08",
            latencyMs = 5,
            haEntityId = "light.master_sconces"
        ),
        HomeDevice(
            id = "dev-bedroom-climate",
            name = "Suite Climate",
            roomId = RoomId.BEDROOM,
            type = DeviceType.CLIMATE,
            isOn = true,
            signalTone = SignalTone.CYAN,
            targetTemp = 21,
            currentTemp = 22,
            watts = 780.0,
            standbyWatts = 1.8,
            ip = "192.168.1.62",
            mac = "B8:27:EB:44:21:02",
            channel = "IR-02",
            latencyMs = 8,
            haEntityId = "climate.master_ac"
        ),
        HomeDevice(
            id = "dev-bedroom-blinds",
            name = "Blackout Shades",
            roomId = RoomId.BEDROOM,
            type = DeviceType.BLIND,
            isOn = false,
            signalTone = SignalTone.CYAN,
            position = 0,
            watts = 0.7,
            standbyWatts = 0.7,
            ip = "192.168.1.63",
            mac = "B8:27:EB:44:21:03",
            channel = "MTR-02",
            latencyMs = 7,
            haEntityId = "cover.master_blackout"
        ),
        HomeDevice(
            id = "dev-studio-key",
            name = "Desk Key Light",
            roomId = RoomId.STUDIO,
            type = DeviceType.DIMMER,
            isOn = true,
            signalTone = SignalTone.CYAN,
            brightness = 90,
            kelvin = 5200,
            watts = 22.0,
            ip = "192.168.1.71",
            mac = "A4:C1:38:81:0D:01",
            channel = "CH-09",
            latencyMs = 3,
            haEntityId = "light.studio_key"
        ),
        HomeDevice(
            id = "dev-studio-desk",
            name = "Workstation",
            roomId = RoomId.STUDIO,
            type = DeviceType.PLUG,
            isOn = true,
            signalTone = SignalTone.CYAN,
            watts = 148.5,
            standbyWatts = 1.4,
            ip = "192.168.1.72",
            mac = "DC:A6:32:77:9B:11",
            channel = "RLY-10",
            latencyMs = 4,
            haEntityId = "switch.studio_workstation"
        ),
        HomeDevice(
            id = "dev-outdoor-porch",
            name = "Porch Lights",
            roomId = RoomId.OUTDOOR,
            type = DeviceType.LIGHT,
            isOn = true,
            signalTone = SignalTone.AMBER,
            watts = 18.0,
            ip = "192.168.1.81",
            mac = "A4:C1:38:99:1A:01",
            channel = "CH-11",
            latencyMs = 11,
            haEntityId = "light.outdoor_porch"
        ),
        HomeDevice(
            id = "dev-outdoor-lock",
            name = "Front Entry Lock",
            roomId = RoomId.OUTDOOR,
            type = DeviceType.LOCK,
            isOn = true,
            signalTone = SignalTone.EMERALD,
            watts = 1.8,
            standbyWatts = 1.8,
            ip = "192.168.1.82",
            mac = "E4:5F:01:22:7C:09",
            channel = "SEC-01",
            latencyMs = 6,
            haEntityId = "lock.front_entry"
        )
    )

    val scenes: List<HomeScene> = listOf(
        HomeScene(
            id = "scene-morning",
            name = "Morning",
            subtitle = "Open sheers · Kitchen & Entry",
            accent = SignalTone.AMBER,
            actions = listOf(
                SceneAction("dev-living-blinds", true, position = 100),
                SceneAction("dev-bedroom-blinds", true, position = 85),
                SceneAction("dev-kitchen-island", true, brightness = 90),
                SceneAction("dev-kitchen-espresso", true),
                SceneAction("dev-living-cove", false)
            )
        ),
        HomeScene(
            id = "scene-focus",
            name = "Focus",
            subtitle = "Studio 5200K · Workstation ON",
            accent = SignalTone.CYAN,
            actions = listOf(
                SceneAction("dev-studio-key", true, brightness = 95),
                SceneAction("dev-studio-desk", true),
                SceneAction("dev-living-track", false),
                SceneAction("dev-living-media", false)
            )
        ),
        HomeScene(
            id = "scene-cinema",
            name = "Cinema",
            subtitle = "Warm cove 25% · Media ON",
            accent = SignalTone.AMBER,
            actions = listOf(
                SceneAction("dev-living-cove", true, brightness = 25),
                SceneAction("dev-living-track", false),
                SceneAction("dev-living-blinds", false, position = 0),
                SceneAction("dev-living-media", true)
            )
        ),
        HomeScene(
            id = "scene-away",
            name = "Night Cutoff",
            subtitle = "Lights off · Lock secured",
            accent = SignalTone.EMERALD,
            actions = listOf(
                SceneAction("dev-living-cove", false),
                SceneAction("dev-living-track", false),
                SceneAction("dev-living-media", false),
                SceneAction("dev-kitchen-island", false),
                SceneAction("dev-kitchen-cabinet", false),
                SceneAction("dev-kitchen-espresso", false),
                SceneAction("dev-studio-key", false),
                SceneAction("dev-outdoor-lock", true)
            )
        )
    )

    val automations: List<AutomationRule> = listOf(
        AutomationRule(
            id = "auto-1",
            name = "Sunset Entry Illumination",
            triggerType = "time",
            triggerValue = "Daily · 18:15",
            targetSummary = "Porch Lights ON · Living Cove 65%",
            enabled = true,
            sceneId = "scene-cinema",
            lastRun = "Yesterday, 18:15"
        ),
        AutomationRule(
            id = "auto-2",
            name = "Weekday Studio Ready",
            triggerType = "time",
            triggerValue = "Mon–Fri · 08:45",
            targetSummary = "Focus Scene · Workstation Relay",
            enabled = true,
            sceneId = "scene-focus",
            lastRun = "Today, 08:45"
        ),
        AutomationRule(
            id = "auto-3",
            name = "Goodnight Master Cutoff",
            triggerType = "time",
            triggerValue = "Daily · 23:30",
            targetSummary = "All Living & Kitchen OFF · Lock Secured",
            enabled = true,
            sceneId = "scene-away",
            lastRun = "Yesterday, 23:30"
        ),
        AutomationRule(
            id = "auto-4",
            name = "High Load Shedding Guard",
            triggerType = "power",
            triggerValue = "Load > 3.5 kW",
            targetSummary = "Trim Secondary Plugs",
            enabled = false
        )
    )
}
