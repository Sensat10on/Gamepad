package com.example.bluetoothgamepad

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.bluetoothgamepad.bluetooth.HidGamepadService
import com.example.bluetoothgamepad.ui.navigation.AppNavigation
import com.example.bluetoothgamepad.viewmodel.GamepadViewModel

class MainActivity : ComponentActivity() {
    private val vm by viewModels<GamepadViewModel>()
    private var bound=false
    private val serviceConnection=object:ServiceConnection{
        override fun onServiceConnected(name:ComponentName,binder:IBinder){bound=true;vm.attach((binder as HidGamepadService.LocalBinder).service)}
        override fun onServiceDisconnected(name:ComponentName){bound=false;vm.detach()}
    }
    private val permissions=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){startAndBind()}
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{AppNavigation(vm)};requestPermissionsOrStart()}
    private fun requestPermissionsOrStart(){
        val required=buildList{if(Build.VERSION.SDK_INT>=31)addAll(listOf(Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_ADVERTISE));if(Build.VERSION.SDK_INT>=33)add(Manifest.permission.POST_NOTIFICATIONS)}
        val missing=required.filter{ContextCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED}
        if(missing.isEmpty())startAndBind() else permissions.launch(missing.toTypedArray())
    }
    private fun startAndBind(){val i=Intent(this,HidGamepadService::class.java);ContextCompat.startForegroundService(this,i);bindService(i,serviceConnection,BIND_AUTO_CREATE)}
    override fun onDestroy(){vm.neutralize();if(bound)unbindService(serviceConnection);super.onDestroy()}
}
