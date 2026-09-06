package com.example.bluetoothgamepad.ui

enum class AppLanguage(val code: String, val nativeName: String) {
    RUSSIAN("ru", "Русский"), UKRAINIAN("uk", "Українська"), ENGLISH("en", "English");
    companion object { fun fromCode(code: String) = entries.firstOrNull { it.code == code } ?: RUSSIAN }
}

data class UiStrings(
    val appName:String,val connect:String,val gamepad:String,val settings:String,val ready:String,val bluetoothOff:String,
    val registering:String,val connecting:String,val connected:String,val disconnecting:String,val refresh:String,val disconnect:String,
    val connectButton:String,val unknownDevice:String,val pairHint:String,val controllerProfile:String,val deadZone:String,
    val sensitivity:String,val invertLeftY:String,val invertRightY:String,val haptics:String,val darkTheme:String,val save:String,
    val language:String,val multiTouchHint:String,val bluetoothPermission:String,val connectionError:String,val menu:String,val triggerStep:String,
    val appearance:String,val chooseBackground:String,val removeBackground:String,val buttonColor:String,val stickHoldDelay:String
)

fun strings(code:String):UiStrings = when(AppLanguage.fromCode(code)) {
    AppLanguage.RUSSIAN -> UiStrings("Bluetooth HID геймпад","Подключение","Геймпад","Настройки","Готово","Bluetooth выключен","Регистрация HID-профиля…","Подключение…","Подключено","Отключение…","Обновить список сопряжённых устройств","Отключиться","Подключить","Неизвестное устройство","Сначала выполните сопряжение с принимающим устройством в системных настройках Bluetooth.","Профиль контроллера","Мёртвая зона","Чувствительность","Инверсия левого стика по Y","Инверсия правого стика по Y","Вибрация","Тёмная тема","Сохранить","Язык интерфейса","Можно одновременно удерживать несколько элементов управления.","Требуется разрешение на использование Bluetooth","Не удалось выполнить операцию Bluetooth. Удалите старое сопряжение и выполните его повторно при запущенном приложении.","Меню","Шаг чувствительности L2/R2","Внешний вид","Выбрать фоновое изображение","Удалить фон","Цвет элементов управления","Задержка нажатия L3/R3")
    AppLanguage.UKRAINIAN -> UiStrings("Bluetooth HID геймпад","Підключення","Геймпад","Налаштування","Готово","Bluetooth вимкнено","Реєстрація HID-профілю…","Підключення…","Підключено","Відключення…","Оновити список спарених пристроїв","Відключитися","Підключити","Невідомий пристрій","Спочатку виконайте спарення з приймальним пристроєм у системних налаштуваннях Bluetooth.","Профіль контролера","Мертва зона","Чутливість","Інверсія лівого стіка по Y","Інверсія правого стіка по Y","Вібрація","Темна тема","Зберегти","Мова інтерфейсу","Можна одночасно утримувати кілька елементів керування.","Потрібен дозвіл на використання Bluetooth","Не вдалося виконати операцію Bluetooth. Видаліть старе спарення та виконайте його повторно із запущеним застосунком.","Меню","Крок чутливості L2/R2","Зовнішній вигляд","Вибрати фонове зображення","Видалити фон","Колір елементів керування","Затримка натискання L3/R3")
    AppLanguage.ENGLISH -> UiStrings("Bluetooth HID Gamepad","Connection","Gamepad","Settings","Ready","Bluetooth is off","Registering HID profile…","Connecting…","Connected","Disconnecting…","Refresh paired devices","Disconnect","Connect","Unknown device","Pair the receiving device in Android Bluetooth settings first.","Controller profile","Dead zone","Sensitivity","Invert left Y","Invert right Y","Haptics","Dark theme","Save","Interface language","You can hold multiple controls simultaneously.","Bluetooth permission required","Bluetooth operation failed. Remove the old pairing and pair again while the app is running.","Menu","L2/R2 sensitivity step","Appearance","Choose background image","Remove background","Control color","L3/R3 hold delay")
}
