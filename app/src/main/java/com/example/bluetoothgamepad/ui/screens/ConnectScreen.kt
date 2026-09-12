package com.example.bluetoothgamepad.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluetoothgamepad.bluetooth.ConnectionState
import com.example.bluetoothgamepad.ui.UiStrings
import com.example.bluetoothgamepad.viewmodel.StartupBlocker

@SuppressLint("MissingPermission")
@Composable fun ConnectScreen(
    state: ConnectionState,
    devices: List<BluetoothDevice>,
    text: UiStrings,
    blocker: StartupBlocker?,
    onRefresh: () -> Unit,
    onConnect: (BluetoothDevice) -> Unit,
    onDisconnect: () -> Unit,
    onStop: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text.appName, style = MaterialTheme.typography.headlineMedium)
        Text(
            when (state) {
                is ConnectionState.Connected -> "${text.connected}: ${state.device.name ?: state.device.address}"
                is ConnectionState.Connecting -> text.connecting
                is ConnectionState.Error -> text.connectionError
                ConnectionState.BluetoothOff -> text.bluetoothOff
                ConnectionState.Registering -> text.registering
                ConnectionState.Disconnecting -> text.disconnecting
                else -> text.ready
            }
        )
        if (blocker != null) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        if (blocker == StartupBlocker.PERMISSION) text.bluetoothPermission else text.serviceBlocked,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (blocker == StartupBlocker.PERMISSION) {
                        Button(onClick = onOpenSettings) { Text(text.openSettings) }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRefresh) { Text(text.search) }
            if (state is ConnectionState.Connected) OutlinedButton(onClick = onDisconnect) { Text(text.disconnect) }
            OutlinedButton(onClick = onStop) { Text(text.stop) }
        }
        devices.forEach { device ->
            val paired = device.bondState == BluetoothDevice.BOND_BONDED
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(device.name ?: text.unknownDevice)
                        Text(device.address, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { onConnect(device) }) { Text(if (paired) text.connectButton else text.pair) }
                }
            }
        }
        Text(text.devicesHint, style = MaterialTheme.typography.bodySmall)
        Text(text.pairHint, style = MaterialTheme.typography.bodySmall)
    }
}
