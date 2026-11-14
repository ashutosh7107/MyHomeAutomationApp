package com.obsidianlumina.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.obsidianlumina.home.data.HomeRepository
import com.obsidianlumina.home.model.AutomationRule
import com.obsidianlumina.home.model.BridgeConfig
import com.obsidianlumina.home.model.DeviceType
import com.obsidianlumina.home.model.DispatchEvent
import com.obsidianlumina.home.model.HomeDevice
import com.obsidianlumina.home.model.HomeScene
import com.obsidianlumina.home.model.InitialHomeData
import com.obsidianlumina.home.model.RoomId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val selectedRoom: RoomId = RoomId.ALL,
    val devices: List<HomeDevice> = emptyList(),
    val automations: List<AutomationRule> = emptyList(),
    val bridge: BridgeConfig = BridgeConfig(),
    val activeSceneId: String? = null,
    val events: List<DispatchEvent> = emptyList(),
    val detailDeviceId: String? = null,
    val haTestStatus: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = HomeRepository(application)

    private val _uiState = MutableStateFlow(
        HomeUiState(
            devices = repo.loadDevices(),
            automations = repo.loadAutomations(),
            bridge = repo.loadBridgeConfig()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun selectRoom(room: RoomId) {
        _uiState.update { it.copy(selectedRoom = room) }
    }

    fun openDetail(deviceId: String?) {
        _uiState.update { it.copy(detailDeviceId = deviceId) }
    }

    fun toggleDevice(deviceId: String) {
        val target = _uiState.value.devices.find { it.id == deviceId } ?: return
        val nextOn = !target.isOn
        _uiState.update { state ->
            val updated = state.devices.map { d ->
                if (d.id == deviceId) d.copy(isOn = nextOn) else d
            }
            repo.saveDevices(updated)
            state.copy(devices = updated, activeSceneId = null)
        }
        viewModelScope.launch {
            val ev = repo.dispatchCommand(target, nextOn, _uiState.value.bridge)
            pushEvent(ev)
        }
    }

    fun updateDevice(updatedDevice: HomeDevice, dispatch: Boolean = true) {
        _uiState.update { state ->
            val list = state.devices.map { if (it.id == updatedDevice.id) updatedDevice else it }
            repo.saveDevices(list)
            state.copy(devices = list)
        }
        if (dispatch) {
            viewModelScope.launch {
                val ev = repo.dispatchCommand(
                    updatedDevice,
                    updatedDevice.isOn,
                    _uiState.value.bridge,
                    if (updatedDevice.type == DeviceType.DIMMER) updatedDevice.brightness else null
                )
                pushEvent(ev)
            }
        }
    }

    fun addDevice(device: HomeDevice) {
        _uiState.update { state ->
            val list = state.devices + device
            repo.saveDevices(list)
            state.copy(devices = list, selectedRoom = device.roomId)
        }
    }

    fun deleteDevice(deviceId: String) {
        _uiState.update { state ->
            val list = state.devices.filterNot { it.id == deviceId }
            repo.saveDevices(list)
            state.copy(
                devices = list,
                detailDeviceId = if (state.detailDeviceId == deviceId) null else state.detailDeviceId
            )
        }
    }

    fun runScene(scene: HomeScene) {
        val actionMap = scene.actions.associateBy { it.deviceId }
        val beforeDevices = _uiState.value.devices
        _uiState.update { state ->
            val updated = state.devices.map { d ->
                val act = actionMap[d.id] ?: return@map d
                d.copy(
                    isOn = act.isOn,
                    brightness = act.brightness ?: d.brightness,
                    targetTemp = act.targetTemp ?: d.targetTemp,
                    position = act.position ?: d.position,
                    fanSpeed = act.fanSpeed ?: d.fanSpeed
                )
            }
            repo.saveDevices(updated)
            state.copy(devices = updated, activeSceneId = scene.id)
        }

        viewModelScope.launch {
            scene.actions.forEach { act ->
                val target = beforeDevices.find { it.id == act.deviceId }
                if (target != null) {
                    val ev = repo.dispatchCommand(target, act.isOn, _uiState.value.bridge, act.brightness)
                    pushEvent(ev)
                }
            }
        }
    }

    fun masterCutoff() {
        val activeNonLocks = _uiState.value.devices.filter { it.type != DeviceType.LOCK && it.isOn }
        _uiState.update { state ->
            val updated = state.devices.map { d ->
                if (d.type == DeviceType.LOCK) d else d.copy(isOn = false)
            }
            repo.saveDevices(updated)
            state.copy(devices = updated, activeSceneId = null)
        }
        viewModelScope.launch {
            activeNonLocks.forEach { dev ->
                val ev = repo.dispatchCommand(dev, false, _uiState.value.bridge)
                pushEvent(ev)
            }
        }
    }

    fun toggleRule(ruleId: String) {
        _uiState.update { state ->
            val updated = state.automations.map { r ->
                if (r.id == ruleId) r.copy(enabled = !r.enabled) else r
            }
            repo.saveAutomations(updated)
            state.copy(automations = updated)
        }
    }

    fun addRule(rule: AutomationRule) {
        _uiState.update { state ->
            val updated = listOf(rule) + state.automations
            repo.saveAutomations(updated)
            state.copy(automations = updated)
        }
    }

    fun updateBridge(bridge: BridgeConfig) {
        repo.saveBridgeConfig(bridge)
        _uiState.update { it.copy(bridge = bridge) }
    }

    fun testHomeAssistant() {
        _uiState.update { it.copy(haTestStatus = "Testing connection...") }
        viewModelScope.launch {
            val msg = repo.testHomeAssistant(_uiState.value.bridge.haUrl, _uiState.value.bridge.haToken)
            _uiState.update { it.copy(haTestStatus = msg) }
        }
    }

    fun resetDefaults() {
        repo.clearAll()
        _uiState.value = HomeUiState(
            devices = InitialHomeData.devices,
            automations = InitialHomeData.automations,
            bridge = BridgeConfig()
        )
    }

    private fun pushEvent(ev: DispatchEvent) {
        _uiState.update { state ->
            state.copy(events = (listOf(ev) + state.events).take(25))
        }
    }
}
