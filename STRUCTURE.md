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
│   │       └── 2.json
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
│       │   │               ├── data
│       │   │               │   ├── InMemoryNotesRepository.kt
│       │   │               │   ├── Models.kt
│       │   │               │   ├── NotesRepository.kt
│       │   │               │   ├── RoomNotesRepository.kt
│       │   │               │   ├── db
│       │   │               │   │   ├── AppDatabase.kt
│       │   │               │   │   ├── Entities.kt
│       │   │               │   │   └── NoteDao.kt
│       │   │               │   └── prefs
│       │   │               │       └── SettingsRepository.kt
│       │   │               └── ui
│       │   │                   ├── AppScaffold.kt
│       │   │                   ├── NotesViewModel.kt
│       │   │                   ├── components
│       │   │                   │   └── Common.kt
│       │   │                   ├── navigation
│       │   │                   │   └── Nav.kt
│       │   │                   ├── screens
│       │   │                   │   ├── BugsScreen.kt
│       │   │                   │   ├── FoldersScreen.kt
│       │   │                   │   ├── MindMapScreen.kt
│       │   │                   │   ├── NoteEditorScreen.kt
│       │   │                   │   └── SearchScreen.kt
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
│       │       └── xml
│       │           ├── backup_rules.xml
│       │           └── data_extraction_rules.xml
│       └── test
│           └── java
│               └── com
│                   └── example
│                       └── bugmemo
│                           └── ExampleUnitTest.kt
├── build.gradle.kts
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

43 directories, 64 files
```
