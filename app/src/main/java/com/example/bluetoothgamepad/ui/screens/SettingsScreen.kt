package com.example.bluetoothgamepad.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import android.view.HapticFeedbackConstants
import com.example.bluetoothgamepad.data.GamepadSettings
import com.example.bluetoothgamepad.domain.ProfileKind
import com.example.bluetoothgamepad.ui.*

@Composable fun SettingsScreen(settings:GamepadSettings,text:UiStrings,onProfile:(ProfileKind)->Unit,onLanguage:(String)->Unit,onSave:(GamepadSettings)->Unit) {
    var draft by remember(settings){mutableStateOf(settings)}
    val view=LocalView.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text(text.controllerProfile,style=MaterialTheme.typography.titleLarge)
        var profilesExpanded by remember{mutableStateOf(false)}
        val selectedProfile=ProfileKind.fromId(draft.selectedProfile)
        Box{OutlinedButton(onClick={profilesExpanded=true},modifier=Modifier.width(280.dp)){Text("${selectedProfile.profile.displayName}  ▼")};DropdownMenu(expanded=profilesExpanded,onDismissRequest={profilesExpanded=false},modifier=Modifier.width(280.dp)){ProfileKind.entries.forEach{kind->DropdownMenuItem(text={Text(kind.profile.displayName)},onClick={draft=draft.copy(selectedProfile=kind.profile.id);onProfile(kind);profilesExpanded=false})}}}
        Text("${text.language}:")
        var languagesExpanded by remember{mutableStateOf(false)}
        val selectedLanguage=AppLanguage.fromCode(draft.language)
        Box{OutlinedButton(onClick={languagesExpanded=true},modifier=Modifier.width(280.dp)){Text("${selectedLanguage.nativeName}  ▼")};DropdownMenu(expanded=languagesExpanded,onDismissRequest={languagesExpanded=false},modifier=Modifier.width(280.dp)){AppLanguage.entries.forEach{lang->DropdownMenuItem(text={Text(lang.nativeName)},onClick={draft=draft.copy(language=lang.code);onLanguage(lang.code);languagesExpanded=false})}}}
        if(selectedProfile==ProfileKind.MOUSE){
            val labels=when(draft.language){"uk"->listOf("Швидкість курсора","Рядків за прокрутку","Інтервал подвійного торкання");"en"->listOf("Pointer speed","Lines per scroll","Double-tap interval");else->listOf("Скорость курсора","Строк за прокрутку","Интервал двойного касания")}
            Text("${labels[0]}: ${"%.2f".format(draft.touchpadSpeed)}×");Slider(draft.touchpadSpeed,{draft=draft.copy(touchpadSpeed=it)},valueRange=.4f..3f)
            Text("${labels[1]}: ${draft.touchpadScrollLines}");Slider(draft.touchpadScrollLines.toFloat(),{draft=draft.copy(touchpadScrollLines=it.toInt())},valueRange=1f..10f,steps=8)
            Text("${labels[2]}: ${draft.touchpadDoubleTapMs} мс");Slider(draft.touchpadDoubleTapMs.toFloat(),{draft=draft.copy(touchpadDoubleTapMs=(it/25).toInt()*25)},valueRange=100f..800f,steps=27)
        }else{
            Text("${text.deadZone} ${(draft.stickDeadZone*100).toInt()}%"); Slider(draft.stickDeadZone,{draft=draft.copy(stickDeadZone=it)},valueRange=0f..0.4f)
            Text("${text.sensitivity} ${"%.2f".format(draft.stickSensitivity)}"); Slider(draft.stickSensitivity,{draft=draft.copy(stickSensitivity=it)},valueRange=.5f..1.5f)
            Text("${text.triggerStep}: ${(draft.triggerStep*100).toInt()}%"); Slider(draft.triggerStep,{draft=draft.copy(triggerStep=it)},valueRange=.01f..0.25f,steps=23)
            Text("${text.stickHoldDelay}: ${draft.stickHoldDelayMs} мс"); Slider(draft.stickHoldDelayMs.toFloat(),{draft=draft.copy(stickHoldDelayMs=(it/50).toInt()*50)},valueRange=0f..3000f,steps=59)
            SettingSwitch(text.invertLeftY,draft.invertLeftY){draft=draft.copy(invertLeftY=it)};SettingSwitch(text.invertRightY,draft.invertRightY){draft=draft.copy(invertRightY=it)}
        }
        val orientationLabel=when(draft.language){"uk"->"Фіксувати альбомну орієнтацію";"en"->"Lock landscape orientation";else->"Фиксировать альбомную ориентацию"}
        SettingSwitch(orientationLabel,draft.landscapeLocked){draft=draft.copy(landscapeLocked=it)}
        SettingSwitch(text.haptics,draft.hapticsEnabled){if(it)view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);draft=draft.copy(hapticsEnabled=it)}
        Button(onClick={onSave(draft)}){Text(text.save)}
    }
}
@Composable private fun SettingSwitch(label:String,checked:Boolean,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(label);Switch(checked,onChange)}}
