```
bugmemo/
├── Readme.md
├── STRUCTURE.md
├── app
│   ├── build.gradle.kts
│   ├── lint-baseline.xml
│   ├── proguard-rules.pro
│   ├── schemas
│   │   └── com.example.bugmemo.data.db.AppDatabase
│   │       ├── 2.json
│   │       ├── 3.json
│   │       └── 4.json
│   └── src
│       ├── androidTest
│       │   └── java
│       │       └── com
│       │           └── example
│       │               └── bugmemo
│       │                   ├── ExampleInstrumentedTest.kt
│       │                   └── data
│       │                       ├── RoomNotesRepositoryTest.kt
│       │                       └── db
│       │                           └── NoteDaoTest.kt
│       ├── main
│       │   ├── AndroidManifest.xml
│       │   ├── ic_launcher-playstore.png
│       │   ├── java
│       │   │   └── com
│       │   │       └── example
│       │   │           └── bugmemo
│       │   │               ├── MainActivity.kt
│       │   │               ├── core
│       │   │               │   ├── AppLocaleManager.kt
│       │   │               │   └── FeatureFlags.kt
│       │   │               ├── data
│       │   │               │   ├── InMemoryNotesRepository.kt
│       │   │               │   ├── Models.kt
│       │   │               │   ├── NotesRepository.kt
│       │   │               │   ├── RoomNotesRepository.kt
│       │   │               │   ├── db
│       │   │               │   │   ├── AppDatabase.kt
│       │   │               │   │   ├── NoteDao.kt
│       │   │               │   │   ├── NoteEntity.kt
│       │   │               │   │   └── NoteFts.kt
│       │   │               │   └── prefs
│       │   │               │       └── SettingsRepository.kt
│       │   │               └── ui
│       │   │                   ├── AppScaffold.kt
│       │   │                   ├── NotesViewModel.kt
│       │   │                   ├── common
│       │   │                   │   ├── MarkdownBoldVisual.kt
│       │   │                   │   ├── MarkdownEditUtils.kt
│       │   │                   │   └── MarkdownText.kt
│       │   │                   ├── components
│       │   │                   │   └── Common.kt
│       │   │                   ├── mindmap
│       │   │                   │   └── MindMapViewModel.kt
│       │   │                   ├── navigation
│       │   │                   │   └── Nav.kt
│       │   │                   ├── screens
│       │   │                   │   ├── AllNotesScreen.kt
│       │   │                   │   ├── BugsScreen.kt
│       │   │                   │   ├── FoldersScreen.kt
│       │   │                   │   ├── MindMapScreen.kt
│       │   │                   │   ├── NoteEditorScreen.kt
│       │   │                   │   ├── SearchScreen.kt
│       │   │                   │   └── SettingsScreen.kt
│       │   │                   └── theme
│       │   │                       └── AppTheme.kt
│       │   └── res
│       │       ├── drawable
│       │       │   ├── ic_launcher_background.xml
│       │       │   └── ic_launcher_foreground.xml
│       │       ├── mipmap-anydpi
│       │       ├── mipmap-anydpi-v26
│       │       │   ├── ic_launcher.xml
│       │       │   └── ic_launcher_round.xml
│       │       ├── mipmap-hdpi
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-mdpi
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-xhdpi
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-xxhdpi
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-xxxhdpi
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── values
│       │       │   ├── colors.xml
│       │       │   ├── strings.xml
│       │       │   └── themes.xml
│       │       ├── values-en
│       │       │   └── strings.xml
│       │       └── xml
│       │           ├── backup_rules.xml
│       │           ├── data_extraction_rules.xml
│       │           └── locale_config.xml
│       └── test
│           └── java
│               └── com
│                   └── example
│                       └── bugmemo
│                           └── ExampleUnitTest.kt
├── build.gradle.kts
├── docs
│   ├── architecture
│   │   ├── algorithms.md
│   │   ├── app_scaffold.md
│   │   ├── nav.md
│   │   ├── overview.md
│   │   └── settingsscreen + appLocalemanager.md
│   └── img
│       ├── BugMemo-Home.png
│       ├── BugMemo-Mindmap.png
│       ├── BugMemo-Search.png
│       └── BugMemo_Setting.png
├── gradle
│   ├── libs.versions.toml
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties
└── settings.gradle.kts

50 directories, 86 files
```
