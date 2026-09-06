package com.example.bluetoothgamepad.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluetoothgamepad.bluetooth.ConnectionState
import com.example.bluetoothgamepad.ui.UiStrings

@SuppressLint("MissingPermission")
@Composable fun ConnectScreen(state:ConnectionState, devices:List<BluetoothDevice>, text:UiStrings, onRefresh:()->Unit, onConnect:(BluetoothDevice)->Unit, onDisconnect:()->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(text.appName,style=MaterialTheme.typography.headlineMedium)
        Text(when(state){ is ConnectionState.Connected->"${text.connected}: ${state.device.name ?: state.device.address}"; is ConnectionState.Connecting->text.connecting; is ConnectionState.Error->text.connectionError; ConnectionState.BluetoothOff->text.bluetoothOff; ConnectionState.Registering->text.registering; ConnectionState.Disconnecting->text.disconnecting; else->text.ready })
        val searchLabel=when(text.languageKey()){"uk"->"Шукати пристрої";"en"->"Search for devices";else->"Искать устройства"}
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){ Button(onClick=onRefresh){Text(searchLabel)}; if(state is ConnectionState.Connected) Button(onClick=onDisconnect){Text(text.disconnect)} }
        devices.forEach { device ->
            val paired=device.bondState==BluetoothDevice.BOND_BONDED
            val action=if(paired) text.connectButton else when(text.languageKey()){"uk"->"Спарити";"en"->"Pair";else->"Сопрячь"}
            ElevatedCard(Modifier.fillMaxWidth()) { Row(Modifier.padding(12.dp).fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){ Column{Text(device.name ?: text.unknownDevice);Text(device.address,style=MaterialTheme.typography.bodySmall)}; Button(onClick={onConnect(device)}){Text(action)} } }
        }
        Text(when(text.languageKey()){"uk"->"Оберіть спарений пристрій або запустіть пошук нового.";"en"->"Choose a paired device or search for a new one.";else->"Выберите сопряжённое устройство или запустите поиск нового."},style=MaterialTheme.typography.bodySmall)
    }
}

private fun UiStrings.languageKey()=when{save=="Save"->"en";save=="Зберегти"->"uk";else->"ru"}
