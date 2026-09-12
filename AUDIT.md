# Аудит проекта «BlueTooth GamePad & Mouse» 1.1.0

Дата аудита: текущее состояние ветки `main` (HEAD `483179d`), версия `versionCode=2`, `versionName=1.1.0`.
Объём: 30 исходных файлов Kotlin (~1 100 строк), Gradle-конфигурация, манифест, ресурсы, собранные артефакты.

---

## 0. Статус после правок (1.2.0)

Отчёт ниже описывает состояние на момент аудита (1.1.0). Ниже — что уже исправлено в 1.2.0.

| ID | Статус | Что сделано |
|---|---|---|
| P0-1 | **исправлено** | `signingConfigs.release` из git-ignored `keystore.properties`, `keystore.properties.example`, `app/proguard-rules.pro`, предупреждение при сборке без ключа; порядок выпуска — `RELEASE.md`. Сам ключ должен создать владелец репозитория |
| P0-2 | **отложено осознанно** | `applicationId` не менялся: приватная раздача APK этого не требует, а смена ID заставит всех пользователей переустанавливать приложение, и решать это нужно вместе с будущей публикацией. Зафиксировано в README и `RELEASE.md` |
| P0-3 | **исправлено** | R8 + `shrinkResources` включены, добавлены правила ProGuard. **APK: 24.13 МБ → 3.17 МБ** |
| P0-4 | **исправлено** | Все 8 ошибок `MissingPermission` устранены проверками и точечными `@SuppressLint` с обоснованием. `lintDebug` проходит: **0 errors, 1 warning** (было 9/18) |
| P0-5 | **исправлено** | `MainActivity` проверяет результат запроса разрешений, не запускает службу без `BLUETOOTH_CONNECT`, показывает баннер с переходом в настройки; `startForeground` обёрнут в try/catch; добавлен `onStart`-путь для разрешения, выданного из системных настроек |
| P1-1 | **исправлено** | Раскладки выбираются по `maxWidth` (`wide`/`singleRow`/`compact`), добавлены компактные варианты для портрета и узких экранов; `targetSdk` понижен 36 → 35, чтобы Android 16 не игнорировал фиксацию ориентации |
| P1-2 | **исправлено** | Профиль и язык применяются из коллекции `settings` в `attach()`, а не из начального значения DataStore |
| P1-3 | **исправлено** | Кнопка «Остановить и выйти», действие `ACTION_STOP` в уведомлении, `stopGamepad()` синхронно отправляет нейтральный отчёт до закрытия прокси |
| P1-4 | **исправлено** | `neutralize()` на `onStop`; все жесты в `AnalogStick`/`DPad`/`TriggerControl`/кнопках мыши обёрнуты в `try/finally` |
| P1-5 | **исправлено** | `MutableStateFlow` + `sample(10 ms)` + `distinctUntilChanged` вместо опроса 100 Гц; принудительный отчёт при подключении хоста через `MutableSharedFlow` |
| P1-6 | **исправлено** | `cancelDiscovery()` перед подключением и сопряжением, `pendingPairAddress` сбрасывается при `BOND_NONE` |
| P2-1…P2-6 | **частично** | Исправлены: сброс черновика настроек, декодирование обоев (`inSampleSize`, IO-диспетчер, `LocalWindowInfo`), хак `languageKey()`, локализация названий профилей и уведомления, drag на тачпаде, `dataExtractionRules`/`fullBackupContent`, `RECEIVER_NOT_EXPORTED`, защита от «протухшего» уведомления и от устаревших колбэков прокси, null-safe вибрация. Осталось: адаптивная иконка (иконка по-прежнему PNG 1254×1254), `values-night`, миграция строк в `strings.xml` |
| P3-1 | **частично** | Удалены `TouchRouter` и `GamepadButton`, добавлены тесты `TriggerMathTest` (13 → 17 тестов). Осталось: CI, LICENSE, ktlint/detekt, instrumentation-тесты |

Проверено после правок: `./gradlew clean lintDebug testDebugUnitTest assembleRelease` — успешно; 17 тестов,
0 падений; lint 0 errors / 1 warning; релизный APK 3.17 МБ (без ключа — unsigned, как и задумано).

---

## 1. Методика и что фактически проверено
| Проверка | Команда / способ | Результат |
|---|---|---|
| Сборка debug + unit-тесты | `./gradlew.bat testDebugUnitTest assembleDebug` | **BUILD SUCCESSFUL**, 13 тестов, 0 падений |
| Android Lint | `./gradlew.bat :app:lintDebug` | **BUILD FAILED** — 9 errors, 18 warnings |
| Разбор релизного APK | `aapt2 dump badging/xmltree`, `apksigner verify --print-certs` | APK подписан **отладочным** сертификатом `CN=Android Debug` |
| Разбор release-сборки | `app/build/outputs/apk/release/` | `app-release-unsigned.apk`, 24.13 МБ, R8 выключен |
| Git-гигиена | `git ls-files`, `git status`, `.gitignore` | 57 файлов, рабочее дерево чистое, скриншоты/APK не в индексе |
| Статический разбор кода | чтение всех 30 файлов `app/src` | см. разделы 3–6 |

Версии инструментов: JDK 26, Gradle 9.5.0, AGP 9.3.1, Kotlin 2.3.0, Compose BOM 2026.06.01, compileSdk 36, minSdk 28, targetSdk 36.
Доступны платформы `android-36` и `android-37.0`, build-tools 36.0.0.

---

## 2. Итоговая сводка

| Уровень | Кол-во | Суть |
|---|---|---|
| **P0 — блокеры релиза** | 5 | отладочная подпись, `com.example.*`, R8 выключен, падающий lint, краш при отказе в разрешениях |
| **P1 — серьёзные дефекты** | 6 | сломанный портретный UI на Android 16, гонка профиля (не работает тачпад), нет выхода из приложения, залипание кнопок в фоне, гонка данных в sender-loop, discovery без остановки |
| **P2 — ошибки/недоделки** | 6 | сброс черновика настроек, OOM обоев, смешение языков, мёртвый код, нелокализованные уведомления, манифест/иконка |
| **P3 — качество кода** | 1 блок | форматирование, отсутствие CI/License/ktlint, смешение слоёв, нет UI-тестов |

Главный вывод: **ядро HID-логики спроектировано правильно** (составной дескриптор с Push/Pop, корректные report ID, coalescing отчётов, покрытие ключевой математики тестами), но **релизная инженерия отсутствует полностью**: нет подписи, нет минификации, нет CI, нет проверки разрешений, а заявленный APK 1.1.0 подписан общеизвестным отладочным ключом.

---

## 3. P0 — блокеры релиза

### P0-1. Релизный APK подписан отладочным сертификатом; воспроизводимой подписи нет

**Доказательства**

```
apksigner verify --print-certs Release\BlueTooth-GamePad-Mouse-1.1.0.apk
  Verified using v3 scheme: true
  Signer #1 certificate DN: C=US, O=Android, CN=Android Debug
  Signer #1 certificate SHA-256: 72a58ca5...72e9861
```

* `app/build.gradle.kts` не содержит блока `buildTypes { release { … } }` и `signingConfigs` → `assembleRelease` создаёт `app-release-unsigned.apk` (24.13 МБ), а распространяемый APK (25.33 МБ) подписан ключом из `~/.android/debug.keystore` (пароль `android`).
* README и RELEASE_NOTES называют это «development-ключом», но это именно общеизвестный debug-ключ.

**Последствия:** Google Play и любая площадка отклонят сборку; приватный ключ известен всем; при переустановке ОС/смене машины debug.keystore будет перегенерирован, подпись изменится, и обновление «поверх» станет невозможным — пользователям придётся удалять приложение с потерей настроек.

**Исправление:** сгенерировать отдельный upload-keystore, вынести его из репозитория, добавить `signingConfigs.release` c чтением из `keystore.properties`/переменных окружения, публиковать только `app-release.apk` и проверять подпись в CI. Сделать это **до** того, как появится база пользователей.

---

### P0-2. `applicationId = "com.example.bluetoothgamepad"`

`app/build.gradle.kts:7,11` — и `namespace`, и `applicationId` используют префикс `com.example`. Google Play не принимает идентификаторы, начинающиеся с `com.example`. Смена `applicationId` позже = новое приложение в сторе и потеря всех установок. Менять сейчас (например, `io.github.<owner>.gamepad`), заодно переименовать пакет.

---

### P0-3. R8/минификация и сжатие ресурсов выключены → APK 25 МБ

Блока `buildTypes` нет вообще, `isMinifyEnabled`/`isShrinkResources` не заданы, файла `proguard-rules.pro` нет.

Содержимое релизного APK:

| Компонент | Размер |
|---|---|
| `classes.dex` | 13.9 МБ |
| `classes2.dex` | 9.8 МБ |
| `resources.arsc` | 0.46 МБ |
| `app_icon` (`Fk.png`) | 0.36 МБ |

Это весь Compose + lifecycle + datastore без шринка. После включения R8 ожидаемый размер — **3–5 МБ** (в 5–8 раз меньше), что напрямую влияет на конверсию установок и на доверие пользователей к «25-мегабайтному геймпаду».

**Исправление:**
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        signingConfig = signingConfigs.getByName("release")
    }
}
```
Проверить, что R8 не ломает рефлексию DataStore/Compose (обычно достаточно default-правил).

---

### P0-4. Задача `lintDebug` падает: 9 errors

`./gradlew :app:lintDebug` завершается `BUILD FAILED`. Часть срабатываний `MissingPermission` — ложные (проверки есть в обёртке `hasBluetoothPermission()`), они снимаются `@SuppressLint`/`@RequiresPermission` на конкретных методах. Но **три ошибки реальны**:

| Место | Проблема |
|---|---|
| `HidGamepadService.kt:45` | `device.bondState` читается в `BroadcastReceiver` без проверки `BLUETOOTH_CONNECT` |
| `HidGamepadService.kt:111` (`addNearby`) | `it.name` вызывается из BroadcastReceiver без проверки разрешения |
| `HidGamepadService.kt:125` (`updateNotification`) | `state.device.name` без проверки разрешения |

При отзыве разрешения в системных настройках во время работы приложения (или до его выдачи) эти вызовы дадут `SecurityException` → краш.

**Исправление:** обернуть чтение `device.name` в проверку разрешения (или в `runCatching`), остальные 7 подавить точечно и включить `lint { abortOnError = true; warningsAsErrors = false }`, чтобы гейт работал в CI.

---

### P0-5. Краш при отказе пользователя в разрешениях

`MainActivity.kt:23,26-30`:
```kotlin
private val permissions = registerForActivityResult(RequestMultiplePermissions()) { startAndBind() }
…
if (missing.isEmpty()) startAndBind() else permissions.launch(missing.toTypedArray())
```
Результат запроса игнорируется: `startAndBind()` вызывается **даже если пользователь отказал во всех разрешениях**. Далее:
`ContextCompat.startForegroundService()` → `HidGamepadService.onStartCommand` → `startForeground(NOTIFICATION_ID, …)` для типа `connectedDevice` (`AndroidManifest.xml:22`).

По документации Android для типа `connectedDevice` приложение обязано иметь хотя бы одно из разрешений (`BLUETOOTH_CONNECT` / `BLUETOOTH_ADVERTISE` / `BLUETOOTH_SCAN` и др.); если ни одного нет, `startForeground()` выбрасывает `SecurityException`. Плюс все последующие вызовы `BluetoothAdapter` в этом сценарии также небезопасны. Путь воспроизводится на Android 14+ тривиально: «Запретить» в диалоге разрешений.

**Исправление:** проверять карту `grantResults`; при отсутствии `BLUETOOTH_CONNECT` показывать объяснение с кнопкой «Открыть настройки» и **не** запускать сервис; `startForeground` обернуть в `try/catch` (`ForegroundServiceStartNotAllowedException`/`SecurityException`); добавить обработку `shouldShowRequestPermissionRationale`.

---

## 4. P1 — серьёзные дефекты

### P1-1. Android 16 игнорирует фиксацию ориентации — на планшетах UI развалится

Lint (`DiscouragedApi`, `AndroidManifest.xml:16`): *«Fixed screen orientations will be ignored in most cases, starting from Android 16»*. При `targetSdk = 36` на устройствах с smallest width ≥ 600dp (планшеты, складные) система проигнорирует `android:screenOrientation="sensorLandscape"`, и приложение окажется в портрете.

Портретная раскладка геймпада не предусмотрена: `GamepadScreen.kt:79-89` — `Row` из `DPad(150dp) + AnalogStick(150dp) + FaceCluster(145dp) + AnalogStick(150dp)` ≈ 600dp **без** `horizontalScroll`. На экране 360–600dp `Row` ужимает детей до нуля (модификатор `size()` коэрцится по входящим ограничениям) — стики и кнопки исчезают или накладываются. Планшетный порог `maxWidth >= 840.dp` (`GamepadScreen.kt:19`) не спасает: планшет 800dp в портрете тоже попадает в `ModernLayout`.

Дополнительно: при `landscapeLocked = false` (**значение по умолчанию**) `AppNavigation.kt:31` выставляет `SCREEN_ORIENTATION_UNSPECIFIED`, что снимает ограничение манифеста и разрешает портрет уже на телефоне.

**Исправление:** делать адаптивную раскладку, а не полагаться на блокировку ориентации: `BoxWithConstraints` + компактный вариант для узких экранов (вертикальный стек или `horizontalScroll`), порог планшета проверять по `maxWidth` в портрете тоже. Ориентацию оставить как есть (она всё равно будет игнорироваться).

---

### P1-2. Профиль применяется к сервису из ещё не загруженного DataStore → тачпад не работает после холодного старта

* `GamepadViewModel.kt:20` — `settings = repository.settings.stateIn(viewModelScope, Eagerly, GamepadSettings())`. Стартовое значение — **дефолт**, где `selectedProfile = "generic"`.
* `GamepadViewModel.kt:33` — `attach()` сразу вызывает `service.startGamepad(ProfileKind.fromId(settings.value.selectedProfile).profile)`, то есть часто **до** первого чтения DataStore.
* `HidGamepadService.kt:106-107` — `sendMouse()`/`sendKey()` жёстко заблокированы условием `profile === MouseProfile`.

**Сценарий:** пользователь сохранил профиль «Мышь / тачпад», закрыл приложение, запустил снова. UI (читающий DataStore уже после загрузки) покажет тачпад, но сервис останется в `GenericHidProfile` → курсор не двигается, пока пользователь не перевыберет профиль в настройках. Гонка `startGamepad` vs первое чтение DataStore; также `startGamepad` при повторном вызове меняет `profile`, но `attach()` больше не вызывается.

**Исправление:** в `attach()` подписаться на поток настроек и применять профиль на каждое изменение:
```kotlin
viewModelScope.launch { settings.map { it.selectedProfile }.distinctUntilChanged()
    .collect { service?.setProfile(ProfileKind.fromId(it).profile) } }
```
(для этого в сервисе достаточно оставить существующий `startGamepad`, который присваивает `profile` до early-return, либо добавить отдельный `setProfile`). Плюс убрать вторую точку правды: UI и сервис должны читать один и тот же источник.

---

### P1-3. Из приложения нельзя выйти; сессия и уведомление не завершаются

* `HidGamepadService.stopGamepad()` (`:108`) **не вызывается нигде** — мёртвый код.
* В UI нет кнопки «Остановить». `ConnectScreen` умеет только `disconnect()` (разрыв связи с хостом), сервис при этом продолжает жить в foreground.
* `onStartCommand` возвращает `START_NOT_STICKY`, но `android:stopWithTask` не задан, а `unbindService` сервис не останавливает → после свайпа из Recents сервис с уведомлением остаётся навсегда. `onTaskRemoved` не переопределён.
* Действия «Отключиться/Остановить» в уведомлении нет.

**Исправление:** кнопка «Остановить и выйти» в UI (вызывает `stopGamepad()`), действие `Disconnect`/`Stop` в `NotificationCompat.Builder.addAction`, переопределить `onTaskRemoved` для решения о завершении сессии.

---

### P1-4. Состояние кнопок не сбрасывается при уходе приложения в фон

`MainActivity.kt:31` — `neutralize()` вызывается только в `onDestroy()`. При сворачивании, блокировке экрана или потере фокуса активити не уничтожается, а нейтральный отчёт не отправляется. Если жест не завершился событием `ACTION_CANCEL`, на хосте остаётся зажатая кнопка/стик. README утверждает «clean neutral reports on release» и «lifecycle cleanup sends a neutral report when the activity closes» — это верно только для закрытия.

Дополнительно: обработчики жестов в `AnalogStick.kt:20-32`, `DPad.kt:19-35`, `TriggerControl.kt:22-32` не обёрнуты в `try/finally` — при отмене корутины `pointerInput` код после цикла (`onDirection(CENTER)`, `onValue(0f,0f)`, `onStickPressed(false)`) не выполнится.

**Исправление:** `DefaultLifecycleObserver`/`LifecycleEventObserver` на `ON_STOP` → `vm.neutralize()`; в контролах — `try { … } finally { onValue(0f, 0f) }`.

---

### P1-5. Гонка данных в цикле отправки отчётов и постоянный опрос на 100 Гц

`HidGamepadService.kt:33-34, 78-86`:
```kotlin
private var pending: GamepadState? = null
private var lastState: GamepadState? = null
…
sender = scope.launch {
    while (isActive) {
        val state = pending
        if (state != null && state != lastState) { … }
        delay(10) // навсегда, даже когда ничего не нажато
    }
}
```
* `pending` пишется из UI-потока (`sendState`) и читается из `Dispatchers.Default` без `@Volatile`/`AtomicReference` — формальная гонка данных, отсутствие гарантий видимости по JMM.
* Цикл крутится 100 раз в секунду всё время жизни сессии, даже в полном покое → лишние пробуждения CPU и расход батареи.
* При переподключении к хосту `startGamepad` сбрасывает `lastState`, но **нейтральный отчёт новому хосту не отправляется**, пока состояние не изменится.

**Исправление:** заменить на `MutableStateFlow<GamepadState>` + `sample(10.milliseconds)` / `distinctUntilChanged()` — событийная отправка вместо опроса; отправлять нейтральный отчёт по факту `STATE_CONNECTED`.

---

### P1-6. Discovery не останавливается перед подключением

`HidGamepadService.searchDevices()` (`:90-96`) запускает `adapter.startDiscovery()` без таймаута, `connect()` (`:97-103`) не вызывает `cancelDiscovery()`. Известная особенность Android: попытка подключения во время активного discovery часто отклоняется или сильно затягивается; обработчика `ACTION_DISCOVERY_FINISHED` нет, индикатора процесса поиска в UI нет.

**Исправление:** `cancelDiscovery()` перед `connect()`/`createBond()`, подписка на `ACTION_DISCOVERY_STARTED/FINISHED`, ограничение по времени.

---

## 5. P2 — ошибки и недоделки

### P2-1. `SettingsScreen`: несохранённые настройки теряются при смене профиля или языка
`SettingsScreen.kt:17` — `var draft by remember(settings) { mutableStateOf(settings) }`. Обработчики профиля (`:23`) и языка (`:27`) пишут в DataStore немедленно (`onProfile` → `repository.setProfile`), `settings` меняется → ключ `remember` меняется → `draft` пересоздаётся **из сохранённого состояния**, стирая все правки ползунков. Пользователь двигает «Мёртвую зону», затем меняет язык — правка исчезает без предупреждения. Исправление: не пересоздавать `draft` при изменении `settings` (использовать `rememberSaveable`/`LaunchedEffect` только для внешних изменений) либо сохранять всё одной кнопкой.

### P2-2. Декодирование обоев: OOM/фризы
`WallpaperLayer.kt:26` — `BitmapFactory.decodeStream` **без** `inSampleSize`, на UI-потоке, синхронно в `remember`, `ImageBitmap` не освобождается. Фото 12 МП = ~48 МБ ARGB_8888 на кадр; на слабом устройстве это OOM или заметный фриз при каждом пересоздании Activity (в т.ч. при повороте). Исправление: `BitmapFactory.Options` c `inSampleSize` под размер экрана, декодирование в `produceState` на `Dispatchers.IO`, `Bitmap.recycle()`.

### P2-3. Локализация: смешение языков и отсутствие ресурсов
* `MouseProfile.kt:5` — `displayName = "Мышь / тачпад"` (по-русски), тогда как `Generic HID`, `Xbox-style`, `Sega 6-button` — по-английски. Выпадающий список профилей всегда смешанный.
* `ConnectScreen.kt:31` — `languageKey()` определяет язык сравнением строк `save == "Save"`. Хрупкий хак: любая правка перевода ломает определение языка.
* `HidGamepadService.kt:123-132` — тексты уведомления только на английском.
* Все строки захардкожены в `Localization.kt`; в `res/values/strings.xml` только `app_name`. Нет `values-ru`, `values-uk`, нет поддержки системной локали, нет доступности (TalkBack не прочитает ни один контрол: `Canvas`-кнопки без `semantics`/`contentDescription`).
* `UiStrings.pairHint`, `multiTouchHint`, `bluetoothPermission` не используются.

### P2-4. Мёртвый код и незавершённые функции
| Элемент | Статус |
|---|---|
| `input/TouchRouter.kt` | не используется нигде |
| `ui/controls/GamepadButton.kt` | не используется нигде |
| `HidDescriptor.combinedBytes` | не используется |
| `lastHostAddress` в `SettingsRepository` | сохраняется (`GamepadViewModel.kt:43`), но не читается — автоподключение к последнему хосту не реализовано, хотя README заявляет «last host» |
| `AppNavigation.kt:30` `activeProfile` | вычисляется и не используется |
| `HidConnectionManager.profile` | дублирует состояние `HidGamepadService.profile` |
| `stopGamepad()` | см. P1-3 |

### P2-5. Мелкие дефекты логики
* **Ложное уведомление после закрытия.** `HidGamepadService.onDestroy` вызывает `manager.close()` → `unregisterApp()` → колбэк `onAppStatusChanged(isRegistered=false)` → `onState(Error("HID registration lost"))` → `updateNotification()` постит уведомление **после** `stopForeground(STOP_FOREGROUND_REMOVE)`. Уведомление о потере регистрации может остаться висеть.
* **Отсутствие начального отчёта при подключении** — если пользователь ничего не нажимает, новый хост не получает ни одного отчёта (см. P1-5).
* **`clickMouse`/`pressTvKey`.** `viewModelScope.launch { send; delay(35/45); send(0) }` — выполняется на Main, тогда как sender-loop работает на `Dispatchers.Default`; порядок отчётов в HID-канал не гарантирован. Пауза 35 мс может быть слишком короткой для Android TV (надёжнее 60–100 мс). Быстрые двойные нажатия перетирают друг друга.
* **Тачпад не умеет drag.** `GamepadViewModel.kt:51` — `sendMouse(dx, dy, wheel)` всегда отправляет `buttons=0`, а `MouseButtons` делает press+release. Перетаскивание (drag&drop) невозможно.
* **`pendingPairAddress` не сбрасывается** при неудачном `createBond()` — последующее сопряжение любого устройства инициирует неожиданное подключение (`HidGamepadService.kt:99-102`).
* **`HidConnectionManager.connect()` требует `registered`**, а флаг выставляется асинхронно в `onAppStatusChanged`. Сразу после регистрации возможен ложный отказ «Connection request was rejected».
* **`vibrate()`** (`GamepadViewModel.kt:59`) — `getSystemService(VibratorManager::class.java).defaultVibrator` без проверки на `null` (NPE-риск на девайсах без вибро).
* **`TriggerControl`.** Значение сбрасывается в 0 при отпускании, но при отмене жеста — нет (см. P1-4). Текст поверх триггера всегда `Color.White` — на светлом фоне нечитаем.
* **`AppNavigation.kt:28`** — `LaunchedEffect(state) { if (state is Connected) page = GAMEPAD }` выдёргивает пользователя с экрана настроек/внешнего вида в момент подключения.

### P2-6. Манифест, ресурсы, сборка
* `android:allowBackup="true"` без `dataExtractionRules`/`fullBackupContent`: восстановленный `background_uri` теряет persistable-разрешение (открытие отражено в `runCatching`, но фон молча пропадёт).
* Иконка: `res/drawable-nodpi/app_icon.png` 1254×1254, 413 КБ, **не** адаптивная; `roundIcon` указывает на тот же квадрат → lint `IconLauncherShape`. Нет `mipmap-anydpi-v26`/`ic_launcher_foreground`, нет плотностных вариантов.
* `res/values/styles.xml` — `Theme.Material.Light` при тёмной теме по умолчанию → белая вспышка при старте; `windowFullscreen=true` убирает статус-бар; нет `values-night`.
* `HidGamepadService.kt:57` — `registerReceiver(..., RECEIVER_EXPORTED)`. Для приёмника, слушающего только системные broadcast'ы, рекомендуется `RECEIVER_NOT_EXPORTED` (снижает площадь атаки).
* `gradle/wrapper/gradle-wrapper.properties` — нет `distributionSha256Sum` (supply-chain).
* Расхождения версий из lint: доступны Gradle 9.7.1, AGP 9.4.0, compose-bom 2026.09.00, activity-compose 1.13.0, lifecycle 2.11.0, coroutines 1.11.0, compileSdk 37.
* Нет `LICENSE`, нет `CHANGELOG`, нет CI, нет `proguard-rules.pro`, `baseline.prof` генерируется, но `baselineProfiles` не подключены явно.

---

## 6. Тесты

**Есть (13 тестов, все зелёные):** `HidReportEncoderTest` (8) — нейтральный отчёт, полный диапазон осей, упаковка 16 кнопок, hat, совпадение дескриптора у профилей, мышь, клавиатура; `CombinedHidDescriptorTest` (1) — собственный мини-парсер HID проверяет, что относительные оси мыши не наследуют физический диапазон hat'а (регрессионный тест на реальный баг — сильная сторона проекта); `StickMathTest` (4) — центр, клампинг, диагональ, dead zone.

**Чего нет:**
* ни одного теста на `SettingsRepository` (маппинг ключей/дефолтов, миграции);
* ни одного теста на логику профилей и переключение профиля (см. P1-2 — именно там баг);
* тестов на `TouchRouter` (мёртвый код);
* квантизация триггера заперта внутри composable `TriggerControl` → нетестируема; вынести в чистую функцию `TriggerMath.quantize()`;
* `testInstrumentationRunner` объявлен, но `androidTest`-источника и зависимостей (`androidx.test`, espresso, compose-ui-test) нет — `androidTestImplementation(platform(libs.compose.bom))` висит впустую;
* нет macrobenchmark/проверки задержки отчётов (а README заявляет требование «sustained report latency»).

---

## 7. Предложения по доработке

### Короткий срок (до релиза, 1–2 дня)
1. Подпись, `applicationId`, R8 — P0-1…P0-3.
2. Починить 9 ошибок lint и включить `abortOnError` — P0-4.
3. Проверка разрешений + `try/catch` вокруг `startForeground` — P0-5.
4. Гонка профиля (P1-2) и `cancelDiscovery()` (P1-6) — дешёвые правки с большим эффектом.
5. `ON_STOP → neutralize()` (P1-4) — 10 строк, снимает целый класс жалоб «кнопка залипла».
6. Кнопка «Стоп» + действие в уведомлении (P1-3).
7. `strings.xml` + `values-ru`/`values-uk` (P2-3), единый язык в `displayName`.
8. Адаптивная раскладка геймпада для узких экранов (P1-1).

### Средний срок (1–2 недели)
9. Sender-loop на `MutableStateFlow` + `sample` (P1-5) и периодический keep-alive-отчёт по настройке.
10. `TouchRouter` — либо реально применить (он ровно для этого и написан: владение указателями), либо удалить. Удалить `GamepadButton`, `HidDescriptor.combinedBytes`.
11. Реализовать автоподключение к `lastHostAddress` или убрать функцию из README.
12. Drag на тачпаде (удержание кнопки + движение), длительность нажатия TV-клавиш в настройках.
13. Адаптивная иконка + `values-night` + `dataExtractionRules` + `RECEIVER_NOT_EXPORTED`.
14. Декодирование обоев с `inSampleSize` на IO-диспетчере (P2-2).
15. Вынести квантизацию триггера и маппинг настроек в чистые функции, покрыть тестами; добавить `androidTest`-смоук (запуск MainActivity, переключение профилей, отсутствие краша).
16. CI (GitHub Actions): `lint`, `testDebugUnitTest`, `assembleRelease`, подпись из секретов, `apksigner verify`, публикация артефакта с SHA-256.
17. ktlint/detekt + `lint-baseline`, нормальное форматирование (сейчас по 3–5 инструкций в строке, что затрудняет ревью).

### Долгий срок (M5 из README и далее)
18. Перемещаемые/масштабируемые контролы и пресеты раскладки.
19. Расширенная вибро-отдача (паттерны, привязка к событиям хоста через `onInterruptData`).
20. Опционально: цифровые триггеры (кнопки L2/R2) для игр, ждущих дискретный ввод.
21. Macrobenchmark + Baseline Profiles в релизной сборке.

---

## 8. Шаги для релиза

### Этап A. Подготовка подписи и идентификатора (одноразово)
1. Сгенерировать upload-ключ:
   ```powershell
   keytool -genkeypair -v -keystore release.jks -alias gamepad -keyalg RSA -keysize 4096 -validity 10000
   ```
2. Создать `keystore.properties` (в `.gitignore`) и `signingConfigs.release` в `app/build.gradle.kts`; проверить, что `release.jks` не попал в индекс (`git status`).
3. Сделать резервную копию `release.jks` и пароля в двух независимых местах (потеря = невозможность обновлять приложение).
4. Сменить `namespace`/`applicationId` с `com.example.*` на собственный домен **именно сейчас**, пока база установок мала. Предупредить пользователей: потребуется удалить старую сборку.
5. Сменить иконку на адаптивную, убрать 413-КБ PNG 1254×1254.

### Этап B. Правки кода перед релизом
6. P0-4, P0-5 (lint + разрешения), P1-2, P1-3, P1-4, P1-6, P2-1, P2-3.
7. Включить R8 и убедиться, что приложение не падает после шринка (проверить все экраны, DataStore, Bluetooth).
8. Обновить `versionCode` (сейчас 2 → 3) и `versionName`.
9. Обновить `README.md` и `RELEASE_NOTES.md`: убрать утверждения, не соответствующие коду («last host», «no unbounded report queue» без теста), описать известные ограничения (нет drag, нет XInput, портрет на планшетах).

### Этап C. Проверка качества (гейт)
10. `./gradlew.bat clean lint testDebugUnitTest assembleRelease bundleRelease` — всё должно быть зелёным.
11. `apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk` — DN должен быть вашим, **не** `CN=Android Debug`.
12. Ручное тестирование на матрице устройств:
    * телефоны: Android 9, 12, 14, 16 — в портрете и в ландшафте;
    * планшет Android 16 (проверка игнорирования блокировки ориентации, P1-1);
    * Android TV / Google TV — подключение, все кнопки, hat, стики;
    * Windows — `joy.cpl` (DirectInput), проверка осей/16 кнопок/hat;
    * сценарии: отказ в разрешениях, отзыв разрешения в процессе, выключение Bluetooth, disconnect/reconnect, сворачивание с зажатой кнопкой, поворот экрана, перезапуск сервиса.
13. Проверить критерии приёмки из README: одновременные Left Stick + Right Stick + R2 + South, нейтральные отчёты при отпускании, переподключение после перезапуска сервиса, отсутствие роста очереди отчётов.
14. Зафиксировать результаты в `docs/device-validation.md` (модель, версия Android, ОС хоста, что распозналось, задержка).

### Этап D. Публикация
15. `git tag -a v1.2.0 -m "…"` (тегов сейчас нет вообще), `git push --tags`.
16. Опубликовать APK + его SHA-256; убедиться, что в `Release/` лежит файл, собранный `assembleRelease` с релизной подписью, а не копия debug-сборки.
17. `RELEASE_NOTES.md` — на русском и английском, с разделом «требуется переустановка из-за смены подписи/идентификатора».
18. Сохранить предыдущий APK для отката.

### Этап E. Если планируется Google Play
19. Проверить, что `applicationId` больше не `com.example.*`.
20. Заполнить Data safety (приложение не собирает и не передаёт данные), политику конфиденциальности (использование Bluetooth-разрешений), декларацию foreground-сервиса типа `connectedDevice`.
21. Добавить `LICENSE` (сейчас отсутствует) и `CHANGELOG.md`.
22. Пройти Play App Review с учётом требований к target API level; проверить предупреждения `OldTargetApi` (доступен compileSdk 37).

---

## 9. Приложение: сводная таблица находок

| ID | Уровень | Файл(ы) | Кратко |
|---|---|---|---|
| P0-1 | блокер | `app/build.gradle.kts`, `Release/*.apk` | отладочная подпись, нет `signingConfigs`, релиз не воспроизводим |
| P0-2 | блокер | `app/build.gradle.kts:7,11` | `com.example.*` |
| P0-3 | блокер | `app/build.gradle.kts` | нет R8/shrink → 25 МБ APK |
| P0-4 | блокер | `HidGamepadService.kt:45,89,94,95,99,101,111,125` | `lintDebug` падает, 9 errors |
| P0-5 | блокер | `MainActivity.kt:23,26-30` + `AndroidManifest.xml:22` | старт FGS при отказе в разрешениях → SecurityException |
| P1-1 | высокий | `AndroidManifest.xml:16`, `GamepadScreen.kt:68-91`, `AppNavigation.kt:31` | портретная раскладка разваливается; Android 16 игнорирует блокировку |
| P1-2 | высокий | `GamepadViewModel.kt:20,33`, `HidGamepadService.kt:106-107` | профиль из незагруженного DataStore → мёртвый тачпад |
| P1-3 | высокий | `HidGamepadService.kt:108`, `MainActivity.kt` | нет способа завершить сервис/выйти |
| P1-4 | высокий | `MainActivity.kt:31`, `AnalogStick/DPad/TriggerControl` | залипание кнопок при уходе в фон |
| P1-5 | высокий | `HidGamepadService.kt:33-34,78-86` | гонка `pending`, опрос 100 Гц |
| P1-6 | высокий | `HidGamepadService.kt:90-103` | discovery не отменяется перед подключением |
| P2-1 | средний | `SettingsScreen.kt:17,23,27` | сброс черновика настроек |
| P2-2 | средний | `WallpaperLayer.kt:26` | OOM/фризы при декодировании обоев |
| P2-3 | средний | `Localization.kt`, `MouseProfile.kt:5`, `ConnectScreen.kt:31` | смешение языков, хак `languageKey()`, нет `strings.xml` |
| P2-4 | средний | `TouchRouter.kt`, `GamepadButton.kt`, `HidDescriptor.kt` | мёртвый код, нереализованный `lastHost` |
| P2-5 | средний | `HidGamepadService`, `GamepadViewModel` | ложное уведомление, порядок отчётов, нет drag, `pendingPairAddress` |
| P2-6 | средний | `AndroidManifest.xml`, `styles.xml`, `res/` | backup, иконка, тема, `RECEIVER_EXPORTED` |
| P3-1 | низкий | весь проект | форматирование, нет CI/License/ktlint, смешение слоёв, нет UI-тестов |
