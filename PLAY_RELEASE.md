# Публикация в Google Play — чеклист

Состояние проекта: **`io.github.sensat10on.gamepad`**, versionCode 4, versionName 1.2.0,
minSdk 28, **targetSdk 36**, compileSdk 36. AAB 3.25 МБ, APK 2.85 МБ. Тесты 23/23, lint 0 ошибок.
Release подписан upload-ключом `CN=Sensat10on` (не debug).

Требования сверены с официальной справкой Google Play 12 сентября 2026 г.

---

## 0. Критично по срокам

| Срок | Что | Последствие |
|---|---|---|
| **30.09.2026** | Зарегистрировать имя пакета в **Android developer verification** (Play Console → Home → Android developer verification) | *«Apps not registered by Sep 30, 2026 will be removed from Play»* |
| 31.08.2026 (наступил) | Новые приложения и обновления обязаны таргетить **API 36** | ✅ уже исправлено |

---

## 1. Сделано в коде (пункты 1–7)

### 1.1. ✅ applicationId заменён
`com.example.bluetoothgamepad` → **`io.github.sensat10on.gamepad`**. Переименованы `namespace`,
`applicationId`, все пакеты Kotlin (45 файлов) и дерево каталогов.

**Важно для пользователей:** приложение на телефоне теперь другое — старая debug-сборка и
Play-версия не обновляются друг на друга, требуется удалить и поставить заново. Настройки при
этом теряются.

### 1.2. ✅ Upload-ключ создан, AAB собран
- `release.jks` — RSA 4096, срок действия до 24.07.2059 (Play требует «после 22.10.2033»).
- `keystore.properties` — пароль сгенерирован (40 символов) и лежит **только** в этом файле.
  Оба файла в `.gitignore`.
- `app/build/outputs/bundle/release/app-release.aab` — 3.25 МБ, подпись проверена (`jar verified`).
- `app/build/outputs/apk/release/app-release.apk` — 2.85 МБ, `Signer #1 certificate DN: CN=Sensat10on`.

> **Сделайте резервную копию `release.jks` и `keystore.properties` в двух независимых местах.**
> Потеря ключа = невозможность обновлять приложение. Google может сбросить upload key при
> компрометации, но не app signing key.

Play App Signing обязателен: при первой загрузке AAB Google создаст app signing key и будет
подписывать им финальные APK.

### 1.3. ✅ Политика конфиденциальности
`PRIVACY.md` и готовая для GitHub Pages копия `docs/privacy.md`.

Как опубликовать (2 минуты):
1. Запушьте репозиторий с папкой `docs/`.
2. GitHub → Settings → Pages → Source: *Deploy from a branch* → Branch: `main`, папка `/docs` → Save.
3. Через минуту URL: `https://sensat10on.github.io/Gamepad/privacy.html`

⚠️ **Репозиторий приватный, а Play требует публично доступный URL.** Варианты:
- сделать репозиторий публичным;
- либо создать отдельный публичный репозиторий только с политикой;
- либо опубликовать текст как публичный Gist — URL гиста тоже подходит
  (требование: «active, publicly accessible and non-geofenced URL, no PDFs»).

✅ Контактный адрес в политике: **Senast10on85@gmail.com**. Его же укажите в Play Console →
Store settings → Contact details, чтобы адреса совпадали.

### 1.4. ✅ Сценарий видео для декларации foreground service
Play требует *«a link to a video demonstrating each foreground service feature»*. Снимите
экран телефона, 40–60 секунд, без монтажа:

| # | Кадр | Что должно быть видно |
|---|---|---|
| 1 | Экран «Подключение» | приложение запущено, список сопряжённых устройств |
| 2 | Тап «Подключить» на хосте | состояние меняется на «Подключено», сверху появляется постоянное уведомление |
| 3 | Потянуть шторку | уведомление «Bluetooth GamePad & Mouse» с действием «Остановить и выйти» |
| 4 | Свернуть приложение кнопкой Home | уведомление остаётся, игра/ТВ продолжает принимать ввод |
| 5 | Вернуться, нажать кнопку на экране | на хосте видно отклик (курсор, кнопка в игре) |
| 6 | Действие «Остановить и выйти» | уведомление исчезает, сессия завершается |

Что написать в описании: *«Foreground service type `connectedDevice` keeps the Bluetooth HID
session alive while the user plays. Deferring or interrupting it disconnects the controller
mid-game. The user starts it by tapping Connect and stops it with the notification action or the
Stop and exit button.»*

Use case для выбора в форме: **Continuous Data Transfer to an External Device**.

### 1.5. ✅ Тип аккаунта — что выбрать
| | Личный аккаунт | Организация |
|---|---|---|
| Closed-тест до Production | **12 тестеров × 14 непрерывных дней** (если аккаунт создан после 13.11.2023) | не требуется |
| Верификация | личность + **device verification** (реальный Android-девайс и приложение Play Console) | D-U-N-S (до 30 дней), сайт, телефон |
| Публичные данные | legal name, страна, e-mail | + адрес и телефон |

Если аккаунт личный и создан после 13.11.2023 — **начинайте closed-тест заранее**, это самые
долгие 14 дней в процессе. Пока критерий не выполнен, кнопки Production и Pre-registration
отключены. Тестеры должны быть подписаны непрерывно: отписался и вернулся — отсчёт заново.

### 1.6. ✅ Инструкция для ревьюеров (App access)
Скопируйте в Play Console → App content → App access. Без этого высок риск отказа
«app is not functional»: приложение бессмысленно на одном устройстве.

```
No account or login is required.

This app turns an Android phone into a standard Bluetooth HID gamepad, mouse and
Android TV remote. It needs a SECOND device to be meaningful.

To test:
1. Grant the "Nearby devices" permission when asked (Android 12+). Without it the app
   cannot register its HID profile and shows a banner with a shortcut to settings.
2. Pair the test device with any Bluetooth host from the system Bluetooth settings:
   - Android TV / Google TV: Settings > Remotes & Accessories > Pair accessory
   - Windows PC: Settings > Bluetooth & devices > Add device > Bluetooth
   - another Android phone/tablet
   The host must support the Bluetooth HID Device role; most Android TV boxes, PCs
   and phones do.
3. Open the app, pick a profile in Settings, select the paired device on the
   Connection screen and tap Connect.
4. Move the sticks, press the buttons and use the touchpad profile; verify input on
   the host (on Windows run joy.cpl; on Android TV navigate the system UI).

Marker: the state line on the Connection screen shows "Connected" and a permanent
notification "Bluetooth GamePad & Mouse" appears.

Notes for the reviewer:
- The app has no INTERNET permission and never transmits data.
- The foreground service keeps the HID session alive while the user plays; it is
  started by tapping Connect and stopped by the notification action or "Stop and exit".
- A demo video of the whole flow is attached to the foreground service declaration.
```

### 1.7. ✅ Название приложения
«BlueTooth GamePad & Mouse» → **«Bluetooth GamePad & Mouse»** (исправлена заглавная T).
Обновлены `strings.xml`, `Localization.kt` (ru/uk/en), README, RELEASE_NOTES, PRIVACY.

---

## 2. Что заполнить в Play Console

| Раздел | Что указать |
|---|---|
| **Data safety** | «Данные не собираются и не передаются». Разрешения `INTERNET` нет вообще |
| **Privacy policy** | URL из п. 1.3 (обязателен для всех приложений) |
| **Content rating** | Пройти опросник IARC. Без рейтинга приложение «Unrated» и может быть удалено |
| **Ads** | **Нет**. Если появятся баннеры для продвижения своих приложений — это уже «Contains ads» |
| **Target audience** | Не выбирать детские группы «на всякий случай» — тянет Families Policy. Форма не откроется, пока не заданы Ads, app access и privacy policy |
| **App access** | Инструкция из п. 1.6 |
| **Foreground service types** | `connectedDevice` + описание и видео из п. 1.4 |
| **News declaration** | Нет |

---

## 3. Ассеты для листинга

| Актив | Требование | Статус |
|---|---|---|
| Иконка | 32-bit PNG с альфой, **512×512**, ≤ 1 МБ | ✅ `store/play-icon-512.png` |
| Feature graphic | JPEG или 24-bit PNG **без альфы**, **1024×500** | ✅ `store/play-feature-graphic-1024x500.png` |
| Скриншоты | минимум **2**, до **8** на тип устройства; без альфы; 320–3840 px, максимум ≤ 2× минимума | ⬜ выбрать из `screens/` |
| Short description | ≤ **80** символов | ✅ 71 (ru) / 73 (en) — `store/listing.md` |
| Full description | ≤ **4000** символов | ✅ 1693 (ru) / 1608 (en) — `store/listing.md` |
| Название | ≤ 30 символов | ✅ «Bluetooth GamePad & Mouse» — 25 |
| Контактный e-mail | обязателен | ⬜ |

Скриншоты: в `screens/` есть реальные кадры 1080×2340 и 2340×1080 — подходят. Для попадания в
рекомендательные форматы желательно ≥4 штук с разрешением ≥1080 px.

---

## 4. Порядок действий

1. ✅ Переименован пакет, создан ключ, собран подписанный AAB.
2. ⬜ Проверить `app-release.apk` (release, минифицированный) на реальном устройстве.
3. ⬜ Сделать резервную копию `release.jks` + `keystore.properties`.
4. ⬜ Зарегистрировать имя пакета в Android developer verification — **до 30.09.2026**.
5. ⬜ Опубликовать политику конфиденциальности, заменить контактный e-mail.
6. ⬜ Пройти верификацию личности (+ device verification для личного аккаунта).
7. ⬜ Создать приложение в Play Console, загрузить AAB в **closed testing**.
8. ⬜ Заполнить Data safety, privacy policy, content rating, ads, target audience, app access,
   декларацию `connectedDevice` с видео.
9. ⬜ Добавить store-ассеты: иконка, feature graphic, скриншоты, описания, контакт.
10. ⬜ Запустить closed-тест (12 тестеров × 14 дней, если аккаунт личный и новый).
11. ⬜ Подать заявку на Production (Dashboard → Apply for production), рассмотрение ~7 дней.
12. ⬜ Staged rollout и мониторинг Pre-launch report.

---

## 5. Что осталось за владельцем

1. ✅ **Контактный e-mail** — `Senast10on85@gmail.com` (вписан в политику; продублировать в Play Console → Store settings).
2. ⬜ **Где разместить политику** — репозиторий приватный, нужен публичный URL (п. 1.3).
3. ⬜ **Видео** для декларации foreground service — снять по сценарию из п. 1.4.
4. ⬜ **Резервная копия ключа** — `release.jks` + `keystore.properties` в два независимых места.
5. ⬜ **Скриншоты** — выбрать 2–8 кадров из `screens/` (тексты описаний уже готовы: `store/listing.md`).
6. ⬜ **Тип аккаунта** — от него зависит, нужен ли closed-тест на 12 человек.
