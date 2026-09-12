package com.example.bluetoothgamepad.ui.navigation

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bluetoothgamepad.bluetooth.ConnectionState
import com.example.bluetoothgamepad.domain.ProfileKind
import com.example.bluetoothgamepad.ui.WallpaperLayer
import com.example.bluetoothgamepad.ui.screens.AppearanceScreen
import com.example.bluetoothgamepad.ui.screens.ConnectScreen
import com.example.bluetoothgamepad.ui.screens.GamepadScreen
import com.example.bluetoothgamepad.ui.screens.MousePadScreen
import com.example.bluetoothgamepad.ui.screens.SettingsScreen
import com.example.bluetoothgamepad.ui.strings
import com.example.bluetoothgamepad.viewmodel.GamepadViewModel

private enum class Page { CONNECT, GAMEPAD, SETTINGS, APPEARANCE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun AppNavigation(vm: GamepadViewModel) {
    var page by remember { mutableStateOf(Page.CONNECT) }
    var menuExpanded by remember { mutableStateOf(false) }
    val state by vm.connection.collectAsStateWithLifecycle()
    val devices by vm.devices.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val blocker by vm.blocker.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state) { if (state is ConnectionState.Connected && page == Page.CONNECT) page = Page.GAMEPAD }
    LaunchedEffect(settings.landscapeLocked) {
        (context as? Activity)?.requestedOrientation =
            if (settings.landscapeLocked) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    val text = strings(settings.language)
    val textColor = if (settings.automaticTextColor) {
        if (settings.backgroundUri != null) Color.White else {
            val c = settings.backgroundColor.toInt()
            val luminance = (0.2126 * ((c shr 16) and 255) + 0.7152 * ((c shr 8) and 255) + 0.0722 * (c and 255)) / 255.0
            if (luminance > 0.52) Color.Black else Color.White
        }
    } else Color(settings.textColor.toInt())

    val baseScheme = if (settings.darkTheme) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = baseScheme.copy(onBackground = textColor, onSurface = textColor, onSurfaceVariant = textColor)) {
        Box(Modifier.fillMaxSize()) {
            WallpaperLayer(settings)
            Scaffold(
                containerColor = Color.Transparent,
                contentColor = textColor,
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = textColor,
                            actionIconContentColor = textColor
                        ),
                        title = {
                            if (page != Page.GAMEPAD) Text(
                                when (page) {
                                    Page.CONNECT -> text.connect
                                    Page.SETTINGS -> text.settings
                                    Page.APPEARANCE -> text.appearance
                                    Page.GAMEPAD -> ""
                                }
                            )
                        },
                        actions = {
                            Box {
                                TextButton(
                                    onClick = { menuExpanded = true },
                                    modifier = Modifier.height(56.dp),
                                    contentPadding = PaddingValues(horizontal = 18.dp)
                                ) { Text("☰ ${text.menu}", fontSize = 22.sp) }
                                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                    DropdownMenuItem(text = { Text(text.connect) }, onClick = { page = Page.CONNECT; menuExpanded = false })
                                    DropdownMenuItem(text = { Text(text.gamepad) }, onClick = { page = Page.GAMEPAD; menuExpanded = false })
                                    DropdownMenuItem(text = { Text(text.settings) }, onClick = { page = Page.SETTINGS; menuExpanded = false })
                                    DropdownMenuItem(text = { Text(text.appearance) }, onClick = { page = Page.APPEARANCE; menuExpanded = false })
                                }
                            }
                        }
                    )
                }
            ) { padding ->
                Box(Modifier.padding(padding)) {
                    when (page) {
                        Page.CONNECT -> ConnectScreen(
                            state = state,
                            devices = devices,
                            text = text,
                            blocker = blocker,
                            onRefresh = vm::searchDevices,
                            onConnect = vm::connect,
                            onDisconnect = vm::disconnect,
                            onStop = {
                                vm.stopSession()
                                (context as? Activity)?.finishAndRemoveTask()
                            },
                            onOpenSettings = {
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", context.packageName, null)
                                        )
                                    )
                                }
                            }
                        )
                        Page.GAMEPAD -> {
                            val profile = ProfileKind.fromId(settings.selectedProfile)
                            if (profile == ProfileKind.MOUSE) {
                                MousePadScreen(settings, vm::moveMouse, vm::setMouseButton, vm::pressTvKey)
                            } else {
                                GamepadScreen(profile, settings, vm::update)
                            }
                        }
                        Page.SETTINGS -> SettingsScreen(settings, text, vm::setProfile, vm::setLanguage) { updated ->
                            vm.saveSettings(updated)
                            page = Page.GAMEPAD
                        }
                        Page.APPEARANCE -> AppearanceScreen(settings, text) { updated ->
                            vm.saveAppearance(updated)
                            page = Page.GAMEPAD
                        }
                    }
                }
            }
        }
    }
}
