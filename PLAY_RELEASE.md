# Публикация в Google Play — чеклист

Состояние проекта: versionCode 4, versionName 1.2.0, minSdk 28, **targetSdk 36**, compileSdk 36.
Release APK ≈ 2.84 МБ. Тесты 23/23, lint 0 ошибок.

Документ описывает, что уже сделано в коде, что нужно сделать в Play Console и какие решения
остались за владельцем продукта. Требования сверены с официальной справкой Google Play
12 сентября 2026 г.

---

## 0. Критично по срокам

| Срок | Что | Последствие |
|---|---|---|
| **30.09.2026** | Зарегистрировать имя пакета в **Android developer verification** (Play Console → Home → Android developer verification) | *«Apps not registered by Sep 30, 2026 will be removed from Play»* |
| 31.08.2026 (уже наступил) | Новые приложения и обновления обязаны таргетить **API 36** | Загрузка не пройдёт. В проекте уже исправлено |

Ссылки: [Registering Play package names](https://support.google.com/googleplay/android-developer/answer/16984799?hl=en) ·
[Target API level requirements](https://support.google.com/android-developer/answer/11926878?hl=en) ·
[developer.android.com/developer-verification](https://developer.android.com/developer-verification)

---

## 1. Блокеры, которые нужно закрыть до загрузки

### 1.1. applicationId — решение владельца

Сейчас: `com.example.bluetoothgamepad` (и `namespace`, и `applicationId`).

Google официально **не публикует** правило «com.example отклоняется», но использует этот префикс
как placeholder и предписывает заменить его. Практически: консоль такие загрузки отклоняет.
Главное — **applicationId нельзя изменить после первой публикации**: *«If you change the
application ID, Google Play Store treats the upload as a completely different app.»*

Предлагаемый вариант, производный от вашего GitHub: `io.github.sensat10on.gamepad`.
Менять нужно и `namespace`, и пакеты Kotlin — это механическая правка ~35 файлов; сделать её надо
**один раз и до загрузки**.

### 1.2. Ключ подписи

Сейчас release-сборка подписывается **отладочным** ключом (для локальной установки). Для Play это
недопустимо: *«most app stores (including the Google Play Store) do not accept apps signed with a
debug certificate»*.

Нужно:
1. Создать upload-ключ: `keytool -genkeypair -v -keystore release.jks -alias gamepad -keyalg RSA -keysize 4096 -validity 10000`
   (срок действия должен заканчиваться **после 22.10.2033**).
2. Положить `keystore.properties` рядом с проектом (файл уже поддержан в `app/build.gradle.kts`
   и внесён в `.gitignore`).
3. Собрать **AAB**, а не APK: `./gradlew bundleRelease` → `app/build/outputs/bundle/release/app-release.aab`.
4. Play App Signing обязателен для новых приложений; upload key ≠ app signing key.

Загружать нужно AAB с **release**-подписью. Важно понимать: пользователи вашей локальной
debug-подписанной сборки обновиться до Play-версии **не смогут** — потребуется удалить и
поставить заново.

### 1.3. Privacy policy

Обязательна **для всех** приложений, даже не собирающих данные: *«Apps that do not access any
personal and sensitive user data must still submit a privacy policy.»* Нужен активный публичный
URL (не PDF, без геоограничений) + ссылка внутри приложения.

Черновик готов: `PRIVACY.md`. Осталось: указать контактный e-mail и разместить (например,
GitHub Pages из этого репозитория).

### 1.4. Декларация foreground service `connectedDevice`

Обязательна при targetSdk 34+. Потребуются:
1. описание функциональности;
2. влияние на пользователя, если задача будет отложена или прервана системой;
3. **ссылка на видео**, демонстрирующее, как пользователь включает эту функцию;
4. выбранный use case — для нас «Continuous Data Transfer to an External Device».

Заявлять тип без обоснования нельзя: это нарушение Device and Network Abuse.
Ссылка: [Understanding foreground service and full-screen intent requirements](https://support.google.com/googleplay/android-developer/answer/13392821?hl=en)

---

## 2. Что уже сделано в коде

| Пункт | Статус |
|---|---|
| targetSdk 35 → **36** | ✅ (иначе загрузка отклоняется) |
| Adaptive launcher icon (был один PNG 1254×1254) | ✅ vector + фон, `mipmap-anydpi-v26` |
| R8 + shrinkResources, ProGuard-правила | ✅ APK 24 МБ → 2.84 МБ |
| Конфигурация release-подписи через `keystore.properties` | ✅ |
| `debuggable` в release | ✅ отсутствует (проверено `aapt2 dump`) |
| Разрешение `INTERNET` | ✅ отсутствует — приложение физически не может передавать данные |
| Privacy policy | ✅ черновик `PRIVACY.md` |
| Store-ассеты: иконка 512×512, feature graphic 1024×500 | ✅ в `store/` |
| Отчёт аудита и runbook выпуска | ✅ `AUDIT.md`, `RELEASE.md` |

---

## 3. Что нужно заполнить в Play Console

| Раздел | Что указать |
|---|---|
| **Data safety** | «Данные не собираются и не передаются». Приложение обрабатывает данные только на устройстве, разрешения `INTERNET` нет вообще. Заполнить обязаны все, включая приложения без сбора данных |
| **Privacy policy** | URL из п. 1.3 |
| **Content rating** | Пройти опросник IARC. Без рейтинга приложение «Unrated» и может быть удалено |
| **Ads** | **Нет** рекламы. Важно: если внутри появятся баннеры для продвижения *своих* приложений — это уже «Contains ads» |
| **Target audience** | Не выбирать детские возрастные группы «на всякий случай» — это тянет Families Policy. Форма не заполнится, пока не заданы Ads, app access и privacy policy |
| **App access** | Экран подключения требует Bluetooth-разрешений. Ревьюеру нужна инструкция: приложение работает без аккаунта, но требует разрешения «Рядом с устройствами» и **второго устройства** (ТВ/ПК). Это стоит описать явно — иначе высок риск отказа «app is not functional» |
| **News declaration** | Нет |
| **Foreground service types** | Декларация `connectedDevice` + видео (п. 1.4) |

Отдельно: приложение нельзя проверить на одном устройстве — ему нужен Bluetooth-хост.
Дайте ревьюерам максимально подробную инструкцию и, если возможно, видео.

---

## 4. Ассеты для листинга

| Актив | Требование | Готово |
|---|---|---|
| Иконка | 32-bit PNG с альфой, **512×512**, ≤ 1 МБ | ✅ `store/play-icon-512.png` |
| Feature graphic | JPEG или 24-bit PNG **без альфы**, **1024×500** | ✅ `store/play-feature-graphic-1024x500.png` |
| Скриншоты | минимум **2**, до **8** на тип устройства; JPEG/PNG без альфы; 320–3840 px, максимум ≤ 2× минимума | ❌ нужно выбрать из реальных снимков |
| Short description | ≤ **80** символов | ❌ |
| Full description | ≤ **4000** символов | ❌ |
| Название приложения | ≤ 30 символов | ⚠️ сейчас «BlueTooth GamePad & Mouse» — обратите внимание на необычное «BlueTooth» |
| Контактный e-mail | обязателен | ❌ |

Скриншоты: у вас есть реальные кадры в `screens/` (1080×2340 и 2340×1080) — они подходят.
Для попадания в рекомендательные форматы желательно ≥4 скриншотов с разрешением ≥1080 px.

---

## 5. Аккаунт разработчика

| Требование | Детали |
|---|---|
| Верификация личности | Play Console → привязка Google Payments profile; публично показываются legal name, страна, e-mail |
| Device verification | Для **новых личных** аккаунтов: подтвердить наличие реального Android-устройства через мобильное приложение Play Console |
| Closed testing | Личные аккаунты, созданные после 13.11.2023: **12 тестеров × 14 непрерывных дней** до доступа к Production. Если тестер отписался и вернулся — 14 дней считаются заново |
| Organization | Не требуется, если аккаунт личный. Для организации обязателен D-U-N-S |

---

## 6. Порядок действий

1. Решить `applicationId` (п. 1.1) и переименовать пакеты.
2. Создать upload-ключ, собрать AAB с release-подписью.
3. Проверить релизный AAB на реальном устройстве (R8).
4. Зарегистрировать имя пакета в Android developer verification — **до 30.09.2026**.
5. Пройти верификацию личности и (для личного аккаунта) device verification.
6. Создать приложение в Play Console, загрузить AAB в **closed testing**.
7. Заполнить: Data safety, privacy policy, content rating, ads, target audience, app access,
   декларацию `connectedDevice` с видео.
8. Добавить store-ассеты: иконка, feature graphic, ≥2 скриншота, описания, контакт.
9. Запустить closed-тест на 12 тестеров × 14 дней.
10. Подать заявку на Production (Dashboard → Apply for production), рассмотрение ~7 дней.
11. После одобрения — поэтапный выпуск (staged rollout) и мониторинг pre-launch report.

---

## 7. Открытые вопросы к владельцу

1. **applicationId** — какой использовать? (предлагаю `io.github.sensat10on.gamepad`)
2. **Контактный e-mail** для privacy policy и листинга.
3. **Где разместить** privacy policy (GitHub Pages из этого репозитория?).
4. **Название приложения** — оставляем «BlueTooth GamePad & Mouse» или правим на «Bluetooth…»?
5. **Видео** для декларации foreground service — запишете сами или нужен сценарий?
6. **Тип аккаунта** — личный или организация? От этого зависит необходимость closed-теста.
