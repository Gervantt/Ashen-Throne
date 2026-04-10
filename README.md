# Ashen Throne

A turn-based dark fantasy combat game built with Java 17 and libGDX.

---

## What is this?

Ashen Throne is a single-hero encounter-based combat game. You fight through a sequence of battles against enemies from two realms — the Abyss and the Cursed Forest. The project is built as a study of Gang of Four design patterns, with each task implementing one or more patterns (Builder, Prototype, Abstract Factory, Decorator, State, Command, Strategy, Observer, Facade, Composite, Adapter, Template Method, Singleton).

---

## Prerequisites

Before cloning, make sure you have the following installed:

### 1. Git
Download from **git-scm.com** and install. Verify with:
```bash
git --version
```

### 2. Java 21+ (or any JDK 17+)
The project targets Java 17 source compatibility but runs on any modern JDK.

- **Mac:** `brew install openjdk` or download from **adoptium.net**
- **Windows:** Download from **adoptium.net** (Temurin is recommended)

Verify with:
```bash
java -version
```

### 3. Gradle (only needed once to generate the wrapper)
- **Mac:** `brew install gradle`
- **Windows:** Download from **gradle.org/install** and add to PATH

Verify with:
```bash
gradle --version
```

---

## Setup

### 1. Clone the repository
```bash
git clone <repository-url>
cd ashen-throne
```

### 2. Generate the Gradle wrapper (first time only)
```bash
gradle wrapper
```

This creates `gradlew` (Mac/Linux) and `gradlew.bat` (Windows). After this step you no longer need Gradle installed globally — all commands use the wrapper.

### 3. Build the project
```bash
./gradlew build          # Mac / Linux
gradlew.bat build        # Windows
```

---

## Running the game

```bash
./gradlew desktop:run          # Mac / Linux
gradlew.bat desktop:run        # Windows
```

This opens a 1280x720 window.

---

## Useful commands

| Command | What it does |
|---------|-------------|
| `./gradlew desktop:run` | Run the game |
| `./gradlew core:compileJava` | Quick compile check (no full build) |
| `./gradlew build` | Full build (all modules) |
| `./gradlew desktop:jar` | Build a fat JAR you can share |

---

## Project structure

```
ashen-throne/
├── core/                     # All game logic
│   └── src/main/java/com/ashenthrone/
│       ├── core/             # AshenThroneGame, GameSession (singletons)
│       ├── characters/       # AbstractCharacter, Hero, Enemy, Builders, CharacterDecorator
│       │   └── prototype/    # EnemyRegistry (Prototype pattern)
│       ├── factory/          # RealmFactory, realm factories, EnemySpawner (Abstract Factory + Factory Method)
│       ├── equipment/        # FireAmulet, CursedRing, ShadowBlade (Decorator pattern)
│       ├── battle/           # (planned) BattleEngine, states, commands
│       ├── strategy/         # (planned) AttackStrategy implementations
│       ├── screens/          # (planned) MainMenuScreen, BattleScreen, etc.
│       ├── observer/         # (planned) EventManager
│       ├── ui/               # (planned) UIComponent composite tree
│       ├── input/            # (planned) BattleInputAdapter
│       └── audio/            # (planned) AudioManager
├── desktop/                  # Desktop launcher only (DesktopLauncher.java)
├── gradle/                   # Gradle wrapper files
├── build.gradle              # Root build config
├── settings.gradle           # Module definitions
└── gradle.properties         # Versions (libGDX 1.12.1, JVM args)
```

---

## Design patterns implemented

| Task | Pattern(s) | Status |
|------|-----------|--------|
| AT-001 | Singleton | Done |
| AT-002 | Builder, Template Method | Done |
| AT-003 | Prototype | Done |
| AT-004 | Abstract Factory, Factory Method | Done |
| AT-005 | Decorator | Done |
| AT-006 | State | Planned |
| AT-007 | Command | Planned |
| AT-008 | Strategy | Planned |
| AT-009 | Observer | Planned |
| AT-010 | Facade | Planned |
| AT-011 | Composite | Planned |
| AT-012 | Adapter | Planned |
| AT-013 | Singleton (Screens) | Planned |
| AT-014 | Singleton (Audio) | Planned |
| AT-015 | Composite (Visual Polish) | Planned |

---

## Troubleshooting

**`gradle wrapper` fails with "unknown property sourceCompatibility"**
You are using Gradle 9+. This is already handled in the build files — just make sure you pulled the latest code.

**`./gradlew` fails with "Cannot find a Java installation matching languageVersion=17"**
Your JDK version is being detected incorrectly. The build uses `options.release = 17` (not a toolchain), so any JDK 17+ should work. Run `java -version` to confirm your JDK is installed.

**Game window doesn't open**
Make sure you're running `desktop:run`, not `core:run`. The `core` module has no launcher.