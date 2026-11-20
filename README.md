# 🎬 KinoPulse — Android-приложение для просмотра фильмов

![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Media3](https://img.shields.io/badge/Media3-ExoPlayer-F5B041?style=for-the-badge)

**KinoPulse** — современное Android-приложение для просмотра фильмов на **Kotlin + Jetpack Compose (Material 3)** с поддержкой офлайн-режима, персональных рекомендаций, быстрого кеширования, ночного AMOLED-режима, уведомлений о новинках и встроенного киноклуба.

---

## 📥 Скачать готовый APK

Готовые сборки автоматически публикуются в разделе **Releases** этого репозитория:

1. Откройте вкладку **[Releases](../../releases)** справа на странице репозитория.
2. Найдите последний релиз (например, **`v1.0.0`**).
3. В блоке **Assets** скачайте файл **`KinoPulse-v1.0.0.apk`** на Android-устройство и установите его.

> 💡 *После каждого пуша в ветку `main` или создания тега `v*` GitHub Actions автоматически прогоняет тесты, собирает APK и обновляет релиз.*

---

## ✨ Основные возможности

| Модуль | Описание |
| :--- | :--- |
| 🎥 **Видеоплеер (Media3 ExoPlayer)** | Потоковое воспроизведение и локальный запуск скачанных фильмов, сохранение позиции («Продолжить просмотр») и ночной фильтр затемнения прямо в плеере. |
| ✈️ **Офлайн-режим и быстрое кеширование** | Скачивание фильмов на устройство с прогрессом загрузки, мониторинг сети (`ConnectivityManager`), база **Room**, дисковый кеш **Coil** (до 150 МБ) и кеш **OkHttp** (до 50 МБ). |
| ✨ **Персональные рекомендации** | Алгоритм подбора фильмов на основе вашего избранного, любимых жанров, эпохи и истории просмотра с процентом совпадения и объяснением причины. |
| 🔖 **Избранное и коллекции** | Добавление в избранное в одно касание, сортировка по личным коллекциям (*«Любимые»*, *«Смотреть позже»*, *«На выходные»*) и отдельная вкладка офлайн-загрузок. |
| 🔎 **Поиск по жанрам и годам выпуска** | Поиск по названию и режиссёру, мультивыбор жанров, быстрые эпохи, слайдер диапазона лет (`1994–2026`), сортировка и фильтр «Только офлайн». |
| 🌙 **Ночной режим и AMOLED** | Быстрое переключение в верхней панели, выбор **Ночной / Дневной / Авто** и режим **AMOLED глубокий чёрный** для комфортного просмотра ночью. |
| 🔔 **Уведомления о новинках** | Системные уведомления Android (`NotificationChannel`), фоновая проверка премьер через **WorkManager** и встроенная лента уведомлений. |
| 💬 **Встроенный чат (Киноклуб)** | Общий чат и комнаты обсуждения для каждого фильма с ответами на реплики и лайками (локальный режим хранения на устройстве). |
| 🔄 **Перенос и синхронизация устройств** | Экспорт и импорт компактного снимка профиля (`KP1:...`) с избранным, коллекциями, прогрессом просмотра и настройками темы. |

---

## 🛠 Технологический стек

- **UI & Навигация:** Jetpack Compose, Material Design 3, адаптивная навигация (`NavigationBar` / `NavigationRail`), Type-Safe Navigation Compose, шрифты *Montserrat* и *Manrope*.
- **Архитектура:** MVVM + Repository (`ViewModel`, `StateFlow`, `Kotlin Coroutines & Flow`).
- **Медиа:** Jetpack Media3 (`media3-exoplayer`, `media3-ui`).
- **Хранение и кеш:** Room Database (KSP), Jetpack DataStore Preferences, Coil Disk/Memory Cache, OkHttp HTTP Cache.
- **Фоновые задачи:** WorkManager (`NewReleaseWorker`) + Android Notifications.
- **Сериализация:** `kotlinx.serialization`.

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
git tag v1.0.1
git push origin v1.0.1
```

### Локальная сборка на компьютере
1. Откройте папку проекта в **Android Studio** (или используйте установленный Gradle 9.3.1).
2. Выполните:
   ```bash
   gradle :app:assembleDebug
   ```
3. Готовый файл появится по пути `app/build/outputs/apk/debug/app-debug.apk`.
