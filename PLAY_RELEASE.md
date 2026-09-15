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

✅ **Сделано.** Репозиторий переведён в публичный, Pages включён, страница проверена:
`https://sensat10on.github.io/Gamepad/privacy.html` отдаёт **HTTP 200**, с тему Primer,
контактным адресом и разделом «Контакты». Эту ссылку указывать в Play Console →
App content → Privacy policy и в Data safety.

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
2. ✅ **Публичный URL политики** — репозиторий публичный, Pages включён:
   `https://sensat10on.github.io/Gamepad/privacy.html` (проверено, HTTP 200).
3. ⬜ **Видео** для декларации foreground service — снять по сценарию из п. 1.4.
4. ⬜ **Резервная копия ключа** — `release.jks` + `keystore.properties` в два независимых места.
5. ⬜ **Скриншоты** — выбрать 2–8 кадров из `screens/` (тексты описаний уже готовы: `store/listing.md`).
6. ✅ **Тип аккаунта** — личный, создан после 13.11.2023 → нужен closed-тест, см. раздел 6.

---

## 6. План для личного аккаунта (создан после 13.11.2023)

### Критический путь — 14 дней закрытого теста

Требование: *«Developers with personal accounts created after November 13, 2023, must run a
closed test for their app with a minimum of 12 testers who have been opted in continuously for at
least 14 days.»* Пока критерий не выполнен, кнопки **Production и Pre-registration отключены**.

Отсчёт 14 дней начинается **не с загрузки сборки**, а с момента, когда 12 тестеров
**непрерывно** подписаны на закрытый тест. Отписался и вернулся — 14 дней считаются заново.

Отсюда единственно верная последовательность: **сначала запустить закрытый тест, потом всё
остальное в параллель.** Это единственные 14 дней, которые нельзя ускорить деньгами или
работой.

### Календарь (сегодня 15.09.2026)

Ключевое совпадение: **до дедлайна регистрации 14 дней и закрытый тест занимает ровно 14 дней.**
Значит запускать тест надо сегодня, а верификацию проходить параллельно с ним.

| День | Дата | Что |
|---|---|---|
| 0 | **15–16.09** | Создать приложение, загрузить AAB в **closed testing**, добавить 14 тестеров |
| 0 | **сегодня же** | Запустить **device verification** и **верификацию личности** — у Google свои сроки |
| 1–3 | 16–18.09 | Заполнить App content, добавить ассеты; тестеры переходят по ссылке и ставят приложение |
| **14** | **29.09** | **Крайний срок: Android developer verification закрыт — иначе приложение удалят из Play** |
| 14 | 29–30.09 | Подать заявку на Production (Dashboard → Apply for production) |
| 14–21 | 30.09–07.10 | Ревью заявки и самого приложения (~7 дней) |
| 21+ | первая половина октября | Поэтапный выпуск в Production |

**Итог: раньше начала октября в открытый доступ выйти не получится**, и это при старте сегодня.
Самая частая ошибка — сначала месяц полировать сборку, а потом узнать про 14 дней.

### Что сделать прямо сейчас

1. **Device verification** — для новых личных аккаунтов обязательно: *«New personal accounts will
   be required to verify that they have access to a real Android mobile device using the Play
   Console mobile app.»* Возьмите телефон, поставьте приложение Play Console, войдите тем же
   аккаунтом.
2. **Верификация личности** — привязка Google Payments profile; публично показываются legal name,
   страна и e-mail. Legal name и адрес берутся из Payments profile.
3. **Android developer verification** — Play Console → Home. Проверьте баннер и требования к имени
   пакета `io.github.sensat10on.gamepad`. **Дедлайн 30.09.2026.**
4. **Создать приложение и загрузить AAB** в закрытый тест:
   `Release/Bluetooth-GamePad-Mouse-1.2.0.aab`.

### ⚠️ Внутреннее тестирование в эти 14 дней НЕ засчитывается

Требование сформулировано строго: нужен **closed test** (в русском интерфейсе — **«Закрытое
тестирование»**). Вкладка **«Внутреннее тестирование»** — отдельный трек, и проведённые в нём
две недели не считаются. Это распространённая и очень дорогая ошибка: люди спокойно отсиживают
14 дней во внутреннем треке и узнают о проблеме только при попытке подать заявку на Production.

Что делать: внутренний трек использовать как **быстрый smoke-тест** (он публикуется без ревью и
за пару минут), но **сразу же создать отдельный выпуск в «Закрытом тестировании»** — именно он
запускает отсчёт.

### Тестеры — 12 человек, берите 15–16

- **«Подписан» ≠ «есть в списке адресов».** Тестер засчитывается только когда он: открыл
  ссылку-приглашение → нажал **«Стать тестировщиком»** → появился как активный тестер в треке →
  установил приложение по этой же ссылке.
- **Главная ловушка:** тестер принимает приглашение с ноутбука под аккаунтом A, а Play Store на
  телефоне залогинен под аккаунтом B. Со стороны тестера всё сделано, в Play Console — ничего.
  Поэтому **проверяйте каждого тестера в Play Console**, а не по своему списку рассылки.
- **Непрерывность.** Счётчик должен держаться на 12+ все 14 дней подряд. Один отписавшийся на
  11-й день — отсчёт с нуля, возврат не восстанавливает серию. Отсюда запас: 15–16 человек
  вместо 12.
- Скажите тестерам не удалять приложение и не отписываться до вашего сигнала.
- **Engagement учитывают.** 12 человек, которые установили и ни разу не открыли, — слабее, чем
  12 человек, которые попользовались и прислали замечания. Соберите обратную связь дважды за
  окно (примерно на 4-й и 11-й день) и **записывайте ответы с первого дня**: после 14 дней в
  заявке спросят, как вы набирали тестеров, насколько они были вовлечены, как собирали отзывы и
  что исправили по итогам. Восстанавливать это на 15-й день по памяти — плохая идея.
- Бюджет времени: набор и проверка подписок 2–4 дня + 14 дней окна + ревью. **Планируйте три
  недели, а не две.**

### Где взять 12 тестеров, если никого нет

Реалистичная оценка: **12 человек — это немного, сложность не в количестве, а в том, чтобы они
додержались 14 дней.** Отсюда три рабочих канала, от быстрого к надёжному.

**1. Личный круг — быстрый старт, но самый ненадёжный.**
Друзья и знакомые соглашаются охотно и отваливаются так же охотно: половина не установит,
вторая половина забудет про приложение через два дня. Годятся как добор к остальным, но
строить на них весь тест нельзя. Обязательно проверьте каждого в Play Console: тестер
засчитан только когда он перешёл по ссылке, нажал «Стать тестировщиком» и появился в треке.

**2. Обмен тестерами — самый надёжный путь без аудитории.**
Практика, где разработчики тестируют приложения друг друга, распространена и не запрещена:
вы ставите чужое приложение на 14 дней, взамен ставят ваше.
- **r/androiddev** — обмен тестерами здесь обычное дело;
- **r/betatesting** — люди, которые целенаправленно ищут приложения для теста;
- приложения-биржи прямо в Google Play: *12 Testers Community*, *BetaCircle*, *Testers*;
- тематические Telegram- и Discord-группы по Android-разработке и бета-тестам.

Минус: у обменных тестеров почти нет мотивации пользоваться вашим приложением. Формально
требование закрывается, но на вопрос об вовлечённости в заявке отвечать будет нечем.

**3. Профильные сообщества — здесь ваше преимущество.**
Ваше приложение не «ещё один блокнот»: оно закрывает конкретную потребность конкретных людей.
Это позволяет набирать тестеров, которым приложение действительно нужно, — а значит, они
додержатся до конца и дадут настоящие отзывы.

Куда идти:
- **Android TV**: r/AndroidTV, r/GoogleTV, профильные Telegram-каналы по ТВ-приставкам;
- **геймпады и эмуляторы**: r/EmulationOnAndroid, r/AndroidGaming, r/RetroGaming;
- **русскоязычные**: 4PDA (ветки Android TV и эмуляторов), Habr, тематические Telegram-чаты;
- **XDA Developers** — там же, где живут прошивки приставок.

Ключевое требование к объявлению: **честно сказать, что нужен второй Bluetooth-хост.**
Отсечёт тех, кто всё равно не сможет проверить, и повысит качество отзывов.

### Готовый текст для набора тестеров

Русская версия (4PDA, Telegram, Habr):
```
Ищу 12–16 тестеров для закрытого теста Android-приложения. Нужно 14 дней.

Что это: Bluetooth GamePad & Mouse — превращает телефон в Bluetooth-геймпад,
мышь-тачпад и пульт для Android TV и Windows. На телевизор или ПК ставить ничего
не нужно: телефон регистрируется как стандартное Bluetooth HID-устройство.

От вас нужно: Android 9+, Google-аккаунт и ОБЯЗАТЕЛЬНО второе устройство для
подключения по Bluetooth — Android TV, ПК с Windows или второй телефон.
Без второго устройства приложение проверить не получится.

Что сделать: перейти по ссылке, нажать «Стать тестировщиком», установить
приложение из Play и не удалять его 14 дней. Пользоваться не обязательно, но если
подключитесь пару раз и напишете, что не работает, — это очень поможет.

Взамен: ранний доступ, приложение бесплатное и без рекламы, все замечания попадут
в релиз.
```

Английская версия (Reddit, XDA):
```
Looking for 12–16 testers for a Google Play closed test (14 days).

What it is: Bluetooth GamePad & Mouse — turns your phone into a Bluetooth gamepad,
mouse/touchpad and remote for Android TV and Windows. Nothing to install on the TV or
PC: the phone registers as a standard Bluetooth HID device.

You need: Android 9+, a Google account, and — this is required — a second device to
connect to over Bluetooth (Android TV, a Windows PC or another phone). Without one the
app cannot really be tested.

What to do: open the opt-in link, tap "Become a tester", install from Play, and keep it
installed for 14 days. Using it is optional, but a quick connect plus a note about what
broke helps a lot.

In return: early access, the app is free and ad-free, and your feedback goes into the
release.
```

### Таблица учёта (ведите с первого дня)

| # | Кто | Google-аккаунт | Опт-ин подтверждён в Play Console | Установил | День 4 | День 11 | Активен |
|---|---|---|---|---|---|---|---|
| 1 | | | ⬜ | ⬜ | | | |
| … | | | | | | | |

Проверка «опт-ин подтверждён» — **только по Play Console**, не по словам тестера.
Напоминания в день 4 и день 11 снижают риск отвала в середине окна.

### Чего делать НЕ стоит

- **Платные «12 тестеров за деньги».** Услуга существует, но тестеры там не вовлечены:
  число закрывается, а на вопрос о качестве теста в заявке отвечать нечем. Плюс это
  выглядит как накрутка.
- **Свои дополнительные аккаунты на одном устройстве.** Легко вычисляется и грозит
  блокировкой аккаунта разработчика — цена несопоставима с выигрышем.
- **Организационный аккаунт как обходной путь.** Требование 12×14 на организации не
  распространяется, но получить его за 14 дней нереально: нужен D-U-N-S (оформление до 30
  дней), а тип аккаунта выбирается при регистрации. Как вариант на будущее — да, как
  решение текущего дедлайна — нет.

### Обязательно предупредите тестеров

Этому приложению **нужен второй Bluetooth-хост**, иначе оно выглядит неработающим. Разошлите им
инструкцию (она же пойдёт ревьюерам — п. 1.6):

> 1. Разрешите доступ «Рядом с устройствами».
> 2. Сопрягите телефон с ТВ, ПК или вторым телефоном через системные настройки Bluetooth.
> 3. Откройте приложение → выберите устройство → «Подключить».
> 4. Понажимайте кнопки и проверьте отклик на хосте.

### Заявка на Production

Dashboard → **Apply for production**, три раздела:

| Раздел | О чём спрашивают | Что отвечать |
|---|---|---|
| About your closed test | сколько тестеров, как долго, что они делали | 12+ тестеров, 14+ дней, реальное подключение к ТВ/ПК |
| About your app | что за приложение, для кого, чем полезно | превращает телефон в Bluetooth-геймпад/мышь/пульт, для владельцев Android TV и ПК |
| About your production readiness | готова ли сборка, исправлены ли проблемы из отзывов | да; перечислите, что починили по фидбеку |

Заявку рассматривают *«usually seven days or less, but can occasionally take longer»*.

### Чего избегать на этом этапе

- **Не заявлять FGS-тип без видео** — это блокирующая декларация, без неё публикация не пройдёт.
- **Не оставлять падений.** Pre-launch report автоматически гоняет сборку, а политика прямо
  запрещает приложения, которые *«crash, force close, freeze, or otherwise function abnormally»*.
  Перед загрузкой прогоните `Release/Bluetooth-GamePad-Mouse-1.2.0.apk` на телефоне.
- **Не выбирать детские возрастные группы** в Target audience — потянет Families Policy.
- **Не заявлять News** и не указывать в описании XInput/бренды Sony и Microsoft.

