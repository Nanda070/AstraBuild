<div align="center">

# ⚒️ AstraBuild

**Build mode plugin for the Astra Minecraft network**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21-62b347?style=flat-square&logo=modrinth&logoColor=white)](https://papermc.io)
[![Paper](https://img.shields.io/badge/Paper-API-f7cf6d?style=flat-square)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21-f89820?style=flat-square&logo=openjdk&logoColor=white)](https://adoptium.net)
[![Gradle](https://img.shields.io/badge/Gradle-KTS-02303a?style=flat-square&logo=gradle&logoColor=white)](https://gradle.org)
[![Version](https://img.shields.io/badge/version-1.0.0--SNAPSHOT-blue?style=flat-square)](#)

</div>

---

## Описание

AstraBuild — плагин для PaperMC-сервера, обеспечивающий изолированный build-режим в рамках экосистемы **Astra SMP**. Позволяет игрокам работать в отдельном строительном пространстве с расширенными правами без вмешательства в основную выживальческую механику.

> Часть монорепозитория [AstraSMP](https://github.com/Nanda070).

---

## Требования

| Компонент | Версия |
|-----------|--------|
| [Paper](https://papermc.io/downloads/paper) | 1.21+ |
| Java | 21+ |

---

## Установка

1. Скачай последний `.jar` из [Releases](https://github.com/Nanda070/AstraBuild/releases).
2. Помести в папку `plugins/` сервера.
3. Перезапусти сервер.

---

## Сборка из исходников

```bash
git clone https://github.com/Nanda070/AstraBuild.git
cd AstraBuild
./gradlew build
```

Артефакт появится в `build/libs/AstraBuild-1.0.0-SNAPSHOT.jar`.

Для работы с NMS (через `paperweight`) необходим первичный запуск:

```bash
./gradlew setupLocalDebugServer
```

---

## Структура проекта

```
AstraBuild/
├── src/main/java/com/astrabuild/   # Исходный код плагина
├── build.gradle.kts                # Конфигурация Gradle (Kotlin DSL)
├── settings.gradle.kts
└── gradle/wrapper/                 # Gradle Wrapper
```

---

## Конфигурация

После первого запуска в папке `plugins/AstraBuild/` появится `config.yml`.  
Документация по параметрам — в [Wiki](https://github.com/Nanda070/AstraBuild/wiki).

---

## Разработка

Проект использует [paperweight-userdev](https://github.com/PaperMC/paperweight) для доступа к internals без теневого Jar.

```kotlin
// build.gradle.kts
dependencies {
    paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")
}
```

Для подключения как модуль монорепы AstraSMP раскомментируй в `build.gradle.kts`:

```kotlin
compileOnly(project(":AstraSMP"))
```

---

## Лицензия

Распространяется под лицензией [MIT](LICENSE) — если не указано иное.

---

<div align="center">
<sub>AstraBuild · часть экосистемы Astra Network</sub>
</div>
