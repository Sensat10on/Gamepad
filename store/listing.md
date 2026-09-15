# Тексты для карточки Google Play

Лимиты Play: название ≤ 30, короткое описание ≤ 80, полное описание ≤ 4000 символов.
Длина каждого блока проверена — см. вывод в конце файла.

---

## Русский (ru-RU) — основной язык листинга

### Название (25 / 30)
```
Bluetooth GamePad & Mouse
```

### Короткое описание (71 / 80)
```
Телефон как Bluetooth-геймпад, тачпад и пульт для Android TV и Windows.
```

### Полное описание
```
Bluetooth GamePad & Mouse превращает телефон в стандартный Bluetooth-геймпад,
мышь-тачпад и пульт для Android TV, Windows и других Android-устройств.

Устанавливать что-либо на принимающее устройство не нужно: телефон регистрируется
как обычное Bluetooth HID-устройство, и хост видит его так же, как любой другой
геймпад или мышь.

ГЕЙМПАД
• Четыре раскладки: стандартный геймпад, Android-геймпад, 8Bit и Sega на шесть кнопок
• Два аналоговых стика, крестовина, два аналоговых триггера и 16 кнопок
• Любые элементы можно удерживать одновременно
• Настройка мёртвой зоны, чувствительности и инверсии осей
• Тактильная отдача при нажатии
• Отдельный скин под каждый тип геймпада

МЫШЬ И ТАЧПАД
• Перемещение курсора пальцем по панели
• Левая и правая кнопки, перетаскивание, прокрутка
• Двойное касание как клик
• Скорость курсора и шаг прокрутки настраиваются отдельно от системных

УПРАВЛЕНИЕ ANDROID TV
• Стрелки и Enter для навигации по интерфейсу телевизора

ВНЕШНИЙ ВИД
• Обои из галереи с масштабированием и кадрированием
• Отдельная картинка на корпусе геймпада
• Настройка цветов элементов, подписей и линий
• Три языка интерфейса: русский, украинский, английский
• Книжная и альбомная ориентация, отдельные раскладки для телефона и планшета

ПРИВАТНОСТЬ
Приложение не собирает и не передаёт никакие данные. У него нет разрешения на
доступ в интернет — всё работает только на вашем устройстве.

ЧТО ПОЛЕЗНО ЗНАТЬ
• Телефон должен поддерживать роль Bluetooth HID Device (Android 9 и новее)
• Принимающее устройство нужно сначала сопрячь в системных настройках Bluetooth
• Игры, требующие XInput, могут потребовать сторонний маппер: приложение
  определяется как универсальный HID-геймпад
```

---

## English (en-US)

### Name (25 / 30)
```
Bluetooth GamePad & Mouse
```

### Short description (73 / 80)
```
Phone as a Bluetooth gamepad, touchpad and TV remote. No host app needed.
```

### Full description
```
Bluetooth GamePad & Mouse turns your phone into a standard Bluetooth gamepad,
mouse/touchpad and remote for Android TV, Windows and other Android devices.

Nothing needs to be installed on the receiving device: the phone registers itself
as a standard Bluetooth HID device, and the host sees it like any other gamepad
or mouse.

GAMEPAD
• Four layouts: standard gamepad, Android gamepad, 8Bit and Sega six-button
• Two analog sticks, a D-pad, two analog triggers and 16 buttons
• Any controls can be held at the same time
• Adjustable dead zone, sensitivity and axis inversion
• Haptic feedback on press
• A separate skin for every gamepad type

MOUSE AND TOUCHPAD
• Move the pointer by sliding a finger
• Left and right buttons, drag-and-drop, scrolling
• Double tap to click
• Pointer speed and scroll step are configurable, independent of the system setting

ANDROID TV NAVIGATION
• Arrow keys and Enter for navigating the TV interface

APPEARANCE
• Wallpaper from your gallery with zoom and cropping
• A separate image on the controller shell
• Configurable control, label and line colours
• Three interface languages: Russian, Ukrainian, English
• Portrait and landscape, with separate phone and tablet layouts

PRIVACY
The app collects and transmits no data at all. It has no internet permission —
everything happens on your device.

GOOD TO KNOW
• Your phone must support the Bluetooth HID Device role (Android 9 or newer)
• Pair the receiving device in the system Bluetooth settings first
• Games that require XInput may need a third-party mapper: the app identifies
  itself as a generic HID gamepad
```

---

## Примечания к заполнению

- **Категория:** Tools (или «Инструменты»). Не Games — приложение само является контроллером,
  а не игрой.
- **Теги:** не обязательны; Play сам подбирает по описанию.
- **Короткое описание** показывается в результатах поиска — оно должно читаться как
  законченная фраза, без перечисления ключевых слов.
- В полном описании **нет разметки и ссылок** — Play не поддерживает HTML; переносы строк
  сохраняются.
- Если листинг добавляется на английском, тексты выше уже готовы; для украинского можно
  перевести по образцу, но это не обязательно.
- В описании **не заявляется XInput и не упоминаются бренды Sony/Microsoft** — приложение не
  выдаёт себя за их контроллеры, и обещание несуществующей совместимости было бы нарушением
  политики Metadata.

---

## Примечания к выпуску (Release notes)

Поле в Play Console → «Создание выпуска» → **Примечания к выпуску**. Лимит — **500 символов**
на язык. Эти примечания видит не только ревьюер, но и **каждый тестер** в Play Store на странице
приложения, поэтому для закрытого теста это прямой канал к нему — и его стоит использовать,
чтобы сразу объяснить про второе устройство.

### Формат: теги языка

Play Console открывает поле с заготовкой:

```
<ru-RU>
Введите примечания к выпуску на этом языке: ru-RU
</ru-RU>
```

Это **шаблон, а не текст** — служебную фразу внутри нужно заменить своим текстом, а теги
`<ru-RU>…</ru-RU>` оставить: именно по ним консоль понимает, к какому языку относится блок.
Ниже поля есть счётчик **«Примечания к выпуску предоставлены на N языке»** — он и есть проверка:

- после вставки текста счётчик должен стать **1** — формат принят;
- если остался **0**, уберите теги и оставьте только текст.

Дополнительно можно вставить вторым блоком `<en-US>…</en-US>` — тогда счётчик покажет 2.

### Русский вариант — 493 / 500

```
<ru-RU>
Первая сборка для закрытого теста.

Телефон как Bluetooth-геймпад, тачпад и пульт для Android TV и Windows. На ТВ или ПК ставить ничего не нужно.

Нужно второе устройство (ТВ, ПК или телефон), сопряжённое по Bluetooth.

Что проверить:
• Подключение к сопряжённому устройству
• Стики, крестовина, кнопки, триггеры — на хосте видно каждое нажатие
• Тачпад: курсор, кнопки мыши, прокрутка
• Стрелки и Enter на Android TV

О проблемах пишите: модель телефона, версия Android и к чему подключались.
</ru-RU>
```

### Английский вариант — 494 / 500

```
<en-US>
First build for a closed test.

Turns your phone into a Bluetooth gamepad, touchpad and TV remote. Nothing to install on the TV or PC.

Testing needs a second device (TV, PC or another phone) paired over Bluetooth.

What to check:
• Connecting to the paired device
• Sticks, D-pad, buttons, triggers — every press visible on the host
• Touchpad: pointer, buttons, scrolling
• Arrow keys and Enter for Android TV

Report problems with your phone model, Android version and what you connected to.
</en-US>
```

### Для следующих выпусков

Примечания нужны **на каждый выпуск**, и они видны тестерам как «Что нового». Для последующих
сборок схема простая: одна строка о том, что изменилось, плюс напоминание, что именно проверить.
Например:

```
<ru-RU>
Исправлено подключение на Android 13, убраны лишние круги на скине, стрелки на триггерах L2/R2.

Проверьте, пожалуйста, подключение к вашему ТВ или ПК и напишите, если что-то отваливается.
</ru-RU>
```

Не пишите «улучшения и исправления ошибок» — тестеру из этого непонятно, что проверять, а
именно на его отзывы вы будете опираться в заявке на Production.

