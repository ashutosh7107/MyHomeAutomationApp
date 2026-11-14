package com.obsidianlumina.home.model

enum class SwitchType(val label: String) {
    LIGHT("Light"),
    FAN("Fan"),
    AC("AC"),
    PLUG("Outlet")
}

data class HomeSwitch(
    val number: Int,
    val name: String,
    val type: SwitchType,
    val isOn: Boolean,
    val onUrl: String = "",
    val offUrl: String = ""
)

data class SwitchboardState(
    val switches: List<HomeSwitch> = DefaultBoard.switches,
    val level: Int = 3,
    val plusUrl: String = "",
    val minusUrl: String = ""
)

object DefaultBoard {
    val switches: List<HomeSwitch> = listOf(
        HomeSwitch(1, "Main Light", SwitchType.LIGHT, true),
        HomeSwitch(2, "Spot Light", SwitchType.LIGHT, false),
        HomeSwitch(3, "Ceiling Fan", SwitchType.FAN, true),
        HomeSwitch(4, "Cove LED", SwitchType.LIGHT, false),
        HomeSwitch(5, "Wall Lamp", SwitchType.LIGHT, false),
        HomeSwitch(6, "Chandelier", SwitchType.LIGHT, true),
        HomeSwitch(7, "TV Outlet", SwitchType.PLUG, false),
        HomeSwitch(8, "Living AC", SwitchType.AC, false)
    )
}
