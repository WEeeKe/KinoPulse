# 🎬 KinoPulse — Android-приложение для просмотра фильмов

![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Minimal%20UI-FF375F?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Media3](https://img.shields.io/badge/Media3-ExoPlayer-FFD60A?style=for-the-badge)

**KinoPulse** — минималистичное Android-приложение для просмотра настоящих фильмов на **Kotlin + Jetpack Compose** (без Material You) со шрифтом в стиле SF Pro (`Inter`), иконками Lucide, интеграцией **Internet Archive + Blender HD**, офлайн-режимом, гибкими фильтрами и настройкой карточек.

---

## 📥 Скачать готовый APK

Готовые сборки автоматически публикуются в разделе **Releases** этого репозитория:

1. Откройте вкладку **[Releases](../../releases)** справа на странице репозитория.
2. Найдите последний релиз (например, **`v2.0.0`**).
3. В блоке **Assets** скачайте файл **`KinoPulse-v2.0.0.apk`** на Android-устройство и установите его.

> 💡 *После каждого пуша в ветку `main` или создания тега `v*` GitHub Actions автоматически прогоняет тесты, собирает APK и обновляет релиз.*

---

## ✨ Основные возможности

| Модуль | Описание |
| :--- | :--- |
| 🎞 **Настоящие фильмы (Archive.org + Blender HD)** | 20 легальных классических и HD-фильмов с постерами прямо на старте + кнопка пополнения каталога и живой онлайн-поиск в Internet Archive. |
| 🎥 **Видеоплеер (Media3 ExoPlayer)** | Автоматическая загрузка прямого MP4-потока Archive.org, локальное воспроизведение скачанных фильмов, выбор скорости (`0.75x–2x`), режим кадра, автосохранение позиции и ночной фильтр затемнения. |
| ✈️ **Офлайн-режим и быстрое кеширование** | Скачивание фильмов на устройство с прогрессом, мониторинг сети (`ConnectivityManager`), база **Room**, дисковый кеш **Coil** (до 150 МБ) и кеш **OkHttp** (до 50 МБ). |
| 🎛 **Фильтры, сортировка и настройки карточек** | Компактные шторки без перегрузки интерфейса: жанры, эпохи и диапазон лет (`1900–2026`), рейтинг (`7.0+`, `7.5+`, `8.0+`), длительность, HD/избранное/офлайн, 5 вариантов сортировки и выбор сетки (`2 в ряд`, `3 в ряд`, `Список`) с настройкой метаданных. |
| ✨ **Персональные рекомендации** | Алгоритм подбора фильмов на основе избранного, любимых жанров, эпохи и истории просмотра. |
| 🔖 **Избранное и коллекции** | Добавление в избранное в одно касание, личные коллекции (*«Любимые»*, *«Смотреть позже»*, *«На выходные»*) и отдельная вкладка офлайн-загрузок. |
| 🌙 **Минималистичные темы и AMOLED** | Палитра Carbon Obsidian (`#0A0A0D`), AMOLED (`#000000`) и светлая Alabaster Studio (`#F6F6F8`) с акцентом Cinema Red (`#FF375F`) и парящей стеклянной нижней панелью. |
| 🔔 **Уведомления о новинках** | Системные уведомления Android (`NotificationChannel`), фоновая проверка премьер через **WorkManager** и встроенная лента уведомлений. |
| 💬 **Встроенный чат (Киноклуб)** | Общий чат и комнаты обсуждения для каждого фильма с ответами на реплики и лайками (локальное хранение на устройстве). |
| 🔄 **Перенос и синхронизация устройств** | Экспорт и импорт компактного снимка профиля (`KP1:...`) с избранным, коллекциями, прогрессом просмотра и настройками. |

---

## 🛠 Технологический стек

- **UI & Дизайн:** Jetpack Compose, кастомная минималистичная дизайн-система (`LocalKinoPalette`), векторные скруглённые иконки **Lucide**, шрифт **Inter** с оптическим трекингом в стиле SF Pro, пружинные анимации и парящая стеклянная навигация.
- **Архитектура:** MVVM + Repository (`ViewModel`, `StateFlow`, `Kotlin Coroutines & Flow`).
- **Медиа и API:** Jetpack Media3 (`media3-exoplayer`, `media3-ui`), живой клиент Internet Archive API (OkHttp + `kotlinx.serialization`).
- **Хранение и кеш:** Room Database (KSP), Jetpack DataStore Preferences, Coil Disk/Memory Cache, OkHttp HTTP Cache.
- **Фоновые задачи:** WorkManager (`NewReleaseWorker`) + Android Notifications.

---

## 🚀 Сборка и публикация релизов

### Автоматическая сборка через GitHub Actions
В проекте настроен workflow [`.github/workflows/android-release.yml`](.github/workflows/android-release.yml), который:
1. Устанавливает JDK 21 и Gradle 9.3.1.
2. Запускает юнит-тесты (`:app:testDebugUnitTest`).
3. Собирает подписанный отладочный APK (`:app:assembleDebug`).
4. Загружает APK в артефакты сборки и публикует **GitHub Release** с готовым файлом `KinoPulse-*.apk`.

### Выпуск новой версии по тегу
```bash
git tag v2.0.0
git push origin v2.0.0
```

### Локальная сборка на компьютере
1. Откройте папку проекта в **Android Studio** (или используйте установленный Gradle 9.3.1).
2. Выполните:
   ```bash
   gradle :app:assembleDebug
   ```
3. Готовый файл появится по пути `app/build/outputs/apk/debug/app-debug.apk`.
