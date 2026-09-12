package com.example.bluetoothgamepad

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.bluetoothgamepad.bluetooth.HidGamepadService
import com.example.bluetoothgamepad.ui.navigation.AppNavigation
import com.example.bluetoothgamepad.viewmodel.GamepadViewModel
import com.example.bluetoothgamepad.viewmodel.StartupBlocker

class MainActivity : ComponentActivity() {

    private val vm by viewModels<GamepadViewModel>()
    private var bound = false
    private var serviceRequested = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            bound = true
            vm.attach((binder as HidGamepadService.LocalBinder).service)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            bound = false
            serviceRequested = false
            vm.detach()
        }
    }

    private val bluetoothPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        // Never start the session blindly: on Android 14+ a connectedDevice foreground service
        // without BLUETOOTH_CONNECT is rejected with a SecurityException.
        startAndBind()
    }

    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* informational only */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppNavigation(vm) }
        requestPermissionsOrStart()
    }

    override fun onStart() {
        super.onStart()
        // Picks up a permission granted from system settings while the app was in the background.
        startAndBind()
    }

    override fun onStop() {
        // A held button or stick must not stay pressed on the host once the UI is gone.
        vm.neutralize()
        super.onStop()
    }

    override fun onDestroy() {
        if (bound) {
            unbindService(serviceConnection)
            bound = false
        }
        super.onDestroy()
    }

    private fun requestPermissionsOrStart() {
        if (!hasBluetoothConnect()) {
            val missing = requiredBluetoothPermissions().filter { !isGranted(it) }
            if (missing.isEmpty()) startAndBind() else bluetoothPermissions.launch(missing.toTypedArray())
            return
        }
        if (Build.VERSION.SDK_INT >= 33 && !isGranted(Manifest.permission.POST_NOTIFICATIONS)) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        startAndBind()
    }

    private fun startAndBind() {
        if (!hasBluetoothConnect()) {
            vm.setBlocker(StartupBlocker.PERMISSION)
            return
        }
        vm.setBlocker(null)
        if (serviceRequested) return
        val intent = Intent(this, HidGamepadService::class.java)
        try {
            ContextCompat.startForegroundService(this, intent)
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
            serviceRequested = true
        } catch (t: Throwable) {
            Log.e(TAG, "Unable to start the HID foreground service", t)
            vm.setBlocker(StartupBlocker.SERVICE)
        }
    }

    private fun hasBluetoothConnect(): Boolean =
        Build.VERSION.SDK_INT < 31 ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

    private fun requiredBluetoothPermissions(): List<String> =
        if (Build.VERSION.SDK_INT >= 31) {
            listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADVERTISE)
        } else {
            emptyList()
        }

    private fun isGranted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private companion object { const val TAG = "BluetoothGamepad" }
}
