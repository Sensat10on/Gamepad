package com.example.bluetoothgamepad.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.example.bluetoothgamepad.domain.GamepadState
import com.example.bluetoothgamepad.domain.HidReportEncoder
import com.example.bluetoothgamepad.domain.MouseProfile
import com.example.bluetoothgamepad.domain.KeyboardReport
import java.util.concurrent.Executor

sealed interface ConnectionState {
    data object BluetoothOff : ConnectionState
    data object Ready : ConnectionState
    data object Registering : ConnectionState
    data object Discoverable : ConnectionState
    data class Connecting(val device: BluetoothDevice) : ConnectionState
    data class Connected(val device: BluetoothDevice) : ConnectionState
    data object Disconnecting : ConnectionState
    data class Error(val message: String) : ConnectionState
}

@SuppressLint("MissingPermission")
class HidConnectionManager(
    private val context: Context,
    private val adapter: BluetoothAdapter,
    private val executor: Executor,
    private val onState: (ConnectionState) -> Unit
) : BluetoothProfile.ServiceListener {
    private var hid: BluetoothHidDevice? = null
    private var host: BluetoothDevice? = null
    private var registered = false

    private val callback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, isRegistered: Boolean) {
            registered = isRegistered
            if (pluggedDevice != null) host = pluggedDevice
            onState(if (isRegistered) ConnectionState.Ready else ConnectionState.Error("HID registration lost"))
        }
        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            when (state) {
                BluetoothProfile.STATE_CONNECTING -> onState(ConnectionState.Connecting(device))
                BluetoothProfile.STATE_CONNECTED -> { host = device; onState(ConnectionState.Connected(device)) }
                BluetoothProfile.STATE_DISCONNECTING -> onState(ConnectionState.Disconnecting)
                BluetoothProfile.STATE_DISCONNECTED -> { if (host == device) host = null; onState(ConnectionState.Ready) }
            }
        }
        override fun onGetReport(device: BluetoothDevice, type: Byte, id: Byte, bufferSize: Int) {
            when(id.toInt()){
                1->hid?.replyReport(device,type,id,HidReportEncoder.encode(GamepadState()))
                2->hid?.replyReport(device,type,id,MouseProfile.encode(GamepadState()))
                3->hid?.replyReport(device,type,id,KeyboardReport.encode(0))
                else->hid?.reportError(device,BluetoothHidDevice.ERROR_RSP_INVALID_RPT_ID)
            }
        }
        override fun onSetProtocol(device: BluetoothDevice, protocol: Byte) = Unit
        override fun onInterruptData(device: BluetoothDevice, reportId: Byte, data: ByteArray) = Unit
        override fun onVirtualCableUnplug(device: BluetoothDevice) { host = null; onState(ConnectionState.Ready) }
    }

    fun start() {
        if (!adapter.isEnabled) { onState(ConnectionState.BluetoothOff); return }
        onState(ConnectionState.Registering)
        if (!adapter.getProfileProxy(context, this, BluetoothProfile.HID_DEVICE))
            onState(ConnectionState.Error("Unable to request HID Device profile"))
    }
    override fun onServiceConnected(profileId: Int, proxy: BluetoothProfile) {
        if (profileId != BluetoothProfile.HID_DEVICE) return
        hid = proxy as BluetoothHidDevice
        val ok = hid?.registerApp(HidDescriptor.sdp(), null, null, executor, callback) == true
        if (!ok) onState(ConnectionState.Error("registerApp() was rejected"))
    }
    override fun onServiceDisconnected(profileId: Int) {
        hid = null; registered = false; host = null
        onState(if (adapter.isEnabled) ConnectionState.Error("HID service disconnected") else ConnectionState.BluetoothOff)
    }
    /** True once the HID application has actually been accepted by the Bluetooth stack. */
    val isRegistered: Boolean get() = registered

    fun bondedDevices(): List<BluetoothDevice> = adapter.bondedDevices.sortedBy { it.name ?: it.address }

    fun connect(device: BluetoothDevice): Boolean {
        if (!registered) return false
        onState(ConnectionState.Connecting(device))
        val accepted = hid?.connect(device) == true
        if (!accepted) onState(ConnectionState.Error("Connection request was rejected. Remove the old Bluetooth pairing and pair again while the gamepad is running."))
        return accepted
    }
    fun disconnect(): Boolean {
        val device = host ?: return false
        onState(ConnectionState.Disconnecting)
        val accepted = hid?.disconnect(device) == true
        if (!accepted) onState(ConnectionState.Error("Disconnect request was rejected"))
        return accepted
    }
    fun send(reportId: Int, payload: ByteArray): Boolean = host?.let { hid?.sendReport(it, reportId, payload) } == true
    fun close() {
        host?.let { hid?.disconnect(it) }
        if (registered) hid?.unregisterApp()
        hid?.let { adapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, it) }
        hid = null; registered = false; host = null
    }
}
