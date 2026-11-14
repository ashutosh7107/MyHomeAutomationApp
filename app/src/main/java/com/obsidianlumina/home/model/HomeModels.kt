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
    val offUrl: String = "",
    val irCode: String = ""
)

data class SwitchboardState(
    val switches: List<HomeSwitch> = DefaultBoard.switches,
    val level: Int = 3,
    val plusUrl: String = "",
    val minusUrl: String = "",
    val plusIrCode: String = "0x00FF09F6",
    val minusIrCode: String = "0x00FF0AF5",
    val allOnIrCode: String = "0x00FF0BF4",
    val allOffIrCode: String = "0x00FF0CF3"
)

object DefaultBoard {
    val switches: List<HomeSwitch> = listOf(
        HomeSwitch(1, "Main Light", SwitchType.LIGHT, true, irCode = "0x00FF01FE"),
        HomeSwitch(2, "Spot Light", SwitchType.LIGHT, false, irCode = "0x00FF02FD"),
        HomeSwitch(3, "Ceiling Fan", SwitchType.FAN, true, irCode = "0x00FF03FC"),
        HomeSwitch(4, "Cove LED", SwitchType.LIGHT, false, irCode = "0x00FF04FB"),
        HomeSwitch(5, "Wall Lamp", SwitchType.LIGHT, false, irCode = "0x00FF05FA"),
        HomeSwitch(6, "Chandelier", SwitchType.LIGHT, true, irCode = "0x00FF06F9"),
        HomeSwitch(7, "TV Outlet", SwitchType.PLUG, false, irCode = "0x00FF07F8"),
        HomeSwitch(8, "Living AC", SwitchType.AC, false, irCode = "0x00FF08F7")
    )
}
