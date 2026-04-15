in 
# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Ashen Throne

Turn-based dark fantasy combat game. Java 17 + libGDX 1.12 + Gradle.

## Commands

```bash
./gradlew desktop:run          # Run the game
./gradlew core:compileJava     # Compile core module only (fast check)
./gradlew build                # Full build (all modules)
./gradlew desktop:jar          # Build fat JAR (Main-Class: DesktopLauncher)
./gradlew core:test            # Run tests (once tests exist)
```

No linting or test framework is configured yet. Add JUnit 5 to `core/build.gradle` under `testImplementation` when writing tests.

## Project Structure

```
core/src/main/java/com/ashenthrone/
  core/          — AshenThroneGame (Game singleton), GameSession (run state singleton)
  characters/    — AbstractCharacter, Hero, Enemy, HeroBuilder, EnemyBuilder
  screens/       — (planned) MainMenuScreen, BattleScreen, VictoryScreen, DefeatScreen
  battle/        — (planned) BattleEngine facade, state/, command/ subpackages
  strategy/      — (planned) AttackStrategy implementations
  equipment/     — (planned) CharacterDecorator and equipment decorators
  observer/      — (planned) EventManager
  ui/            — (planned) UIComponent composite tree
  input/         — (planned) BattleInputAdapter
  audio/         — (planned) AudioManager singleton
desktop/src/     — DesktopLauncher only; never imported from core
core/assets/     — game assets (textures, audio, etc.)
```

## Architecture

Single-hero encounter-based combat using 14 Gang of Four patterns. The game progresses through a sequence of battle encounters; `GameSession` tracks run state (hero, gold, encounter index).

**libGDX lifecycle:** `AshenThroneGame.create()` fires once after OpenGL context is ready → set the initial `Screen`. Each frame, `Game.render()` delegates to the current `Screen`. Screen transitions go through `AshenThroneGame.setScreen()`.

**Turn skeleton (Template Method):** `AbstractCharacter.takeTurn()` is `final` — sequence is `beginTurn() → applyStatusEffects() → chooseAction() → executeAction() → endTurn()`. Only `chooseAction()` is abstract; Hero awaits player input, Enemy runs AI.

**Planned wiring (many TODOs still in code):**
- `GameSession.hero` is `Object` until AT-003+; `GameSession.inventory` is `List<Object>` until items exist
- `AbstractCharacter.currentStrategy` is `Object` until AT-008 introduces `AttackStrategy`
- `BattleScreen` will delegate all logic to `BattleEngine` (AT-010 Facade), never touching subsystems directly
- All screen transitions route through `AshenThroneGame.setScreen()`; `AshenThroneGame` itself is a singleton

## Conventions

- Builders for any class with 4+ constructor params (`HeroBuilder`, `EnemyBuilder`)
- Singletons: lazy init, private constructor, `getInstance()` — no double-checked locking needed (single-threaded game loop)
- `getAttack()`/`getDefense()` must stay non-`final` — the Decorator pattern (AT-005) overrides them
- `Enemy` implements `Cloneable` for the Prototype pattern (AT-003)
- All game logic in `core/`; never import `desktop` packages from `core`
- `TODO: AT-XXX` comments mark planned integration points — keep them until the task is implemented

---

## Jira Tasks

### AT-001 — Project Setup & libGDX Scaffold ✅
**Sprint 1 | 3 SP | Singleton**
Initialize libGDX project with Gradle. Create AshenThroneGame main class extending Game. Set up package structure (core, screens, characters, factory, battle, strategy, equipment, observer, ui, input, audio). Configure desktop launcher with 1280x720 window. Add GameSession singleton with getInstance(), hero reference, gold, encounter index.

### AT-002 — Hero & Enemy Models with Builder Pattern ✅
**Sprint 1 | 5 SP | Builder, Template Method**
Create AbstractCharacter base class with fields: name, hp, maxHp, attack, defense, speed. Implement final takeTurn() template method: beginTurn() → applyStatusEffects() → chooseAction() → executeAction() → endTurn(). Create Hero subclass (overrides chooseAction for player input) and Enemy subclass (overrides chooseAction for AI). Implement HeroBuilder and EnemyBuilder with fluent API: new HeroBuilder().name("Kael").hp(120).attack(18).defense(12).build().

### AT-003 — Enemy Prototype Registry ✅
**Sprint 1 | 3 SP | Prototype**
Create Cloneable interface on Enemy class. Build EnemyRegistry that stores pre-configured enemy templates: ShadowCrawler, Wraith, HollowWolf, Treant, HollowKing. Implement clone() that deep-copies stats with ±5% HP variance. Registry loaded once at startup, all enemy spawning goes through clone.

### AT-004 — Realm Factories & Enemy Spawner ✅
**Sprint 2 | 5 SP | Abstract Factory, Factory Method**
Create RealmFactory interface: createMinion(), createElite(), createBoss(), createBackground(). Implement AbyssRealmFactory (returns ShadowCrawler, Wraith, HollowKing) and CursedForestFactory (returns HollowWolf, Treant, HollowKing). Create abstract EnemySpawner with spawnWave(int count) calling abstract createEnemy(). Subclasses AbyssSpawner and ForestSpawner override createEnemy().

### AT-005 — Equipment Decorator System ✅
**Sprint 2 | 5 SP | Decorator**
Create CharacterDecorator extending AbstractCharacter that wraps a base character. Override getAttack() and getDefense() to delegate + modify. Implement 3 equipment decorators: FireAmulet (+5 attack), CursedRing (+8 defense, -2 attack), ShadowBlade (+10 attack). Equipment applied between encounters. Example: new FireAmulet(new CursedRing(baseHero)). Each decorator modifies stats through wrapping.

### AT-006 — Battle State Machine ✅
**Sprint 1 | 8 SP | State**
Create BattleState interface with handleInput(), update(), render() methods. Implement 5 states: PlayerTurnState (shows action menu, waits for selection), EnemyTurnState (AI picks and executes actions sequentially), AnimationState (plays attack/damage visual feedback, 0.5-1s delay), VictoryState (show rewards, proceed button), DefeatState (show retry/menu buttons). BattleScreen holds currentState reference and delegates all calls. Each state transitions to the next via setState().

### AT-007 — Command System with Undo ✅
**Sprint 1 | 5 SP | Command**
Create BattleCommand interface with execute() and undo(). Implement AttackCommand (stores target, damage dealt; undo restores target HP), DefendCommand (stores previous defense multiplier; undo resets it), SkillCommand (stores strategy used, target, effect; undo reverses), UseItemCommand (stores item consumed; undo returns to inventory). Add command history stack in BattleEngine. Add 'Undo' button visible during PlayerTurnState before confirm.

### AT-008 — Strategy Pattern for Skills ✅
**Sprint 2 | 5 SP | Strategy**
Create AttackStrategy interface: execute(AbstractCharacter attacker, List<AbstractCharacter> targets). Implement PhysicalAttack (single target, damage = atk - def), MagicAttack (single target, ignores 50% defense), HealSelf (restores 30% maxHp), AreaOfEffect (all enemies, 60% damage). Hero's current strategy swaps when player selects Skill submenu. Enemies hold fixed strategy reference (Wraith=MagicAttack, Crawler=PhysicalAttack).

### AT-009 — Observer Event System ⬜
**Sprint 2 | 5 SP | Observer**
Create EventManager with subscribe(EventType, EventListener) and publish(EventType, data). Define events: DAMAGE_DEALT (source, target, amount), CHARACTER_DIED (character), ITEM_USED (item, target), BATTLE_END (result). Subscribers: HealthBar UI updates on DAMAGE_DEALT, AudioManager plays SFX on DAMAGE_DEALT/CHARACTER_DIED, BattleLog appends text on all events, VictoryChecker evaluates win/lose on CHARACTER_DIED.

### AT-010 — BattleEngine Facade ✅
**Sprint 2 | 5 SP | Facade**
Create BattleEngine class exposing: startBattle(Hero hero, List<Enemy> enemies), executePlayerAction(BattleCommand cmd), executeEnemyTurns(), isOver(), getResult(). Internally coordinates: TurnManager (track turn order by speed), DamageCalculator (apply formulas, crits, defense), StatusEffectProcessor (poison, buffs per turn), DeathChecker (remove dead, check win/lose). BattleScreen only talks to BattleEngine, never to subsystems directly.

### AT-011 — Composite UI Component Tree ⬜
**Sprint 1 | 5 SP | Composite**
Create abstract UIComponent with render(SpriteBatch), update(float delta), and addChild(UIComponent). Implement Panel (container, renders children recursively), HealthBar (shows current/max HP as bar), ActionMenu (contains ActionButton children), ActionButton (clickable, highlight on hover), BattleLog (scrolling text list, max 6 lines visible). Battle HUD = root Panel containing all sub-components. Call root.render() to draw everything.

### AT-012 — Input Adapter for libGDX ⬜
**Sprint 1 | 3 SP | Adapter**
Create BattleInputAdapter implementing libGDX InputProcessor. Translate raw input: keyDown(keycode) and touchDown(x,y) into game-level calls: onActionSelected(ActionType), onTargetSelected(int enemyIndex), onConfirm(), onCancel(). Map keys: 1-4 for actions, arrow keys/click for target selection, Enter for confirm, Escape for cancel. Game logic references only the adapter interface, never libGDX input constants.

### AT-013 — Screen Flow Implementation ⬜
**Sprint 1 | 5 SP | Singleton**
Implement 4 screens using libGDX Screen interface. MainMenuScreen: dark background, game title, 'Begin' button → starts new GameSession, switches to BattleScreen. BattleScreen: hosts battle loop, delegates to BattleState. VictoryScreen: shows gold earned, equipment loot option, 'Continue' or 'Next Encounter' button. DefeatScreen: 'Retry' (restart encounter) and 'Main Menu' buttons. All screen transitions through AshenThroneGame.setScreen().

### AT-014 — Asset Loading & Audio Manager ⬜
**Sprint 2 | 3 SP | Singleton**
Set up AudioManager singleton. Load and manage: 1 menu music track, 1 battle music track (loop), SFX for attack hit, skill use, enemy death, hero hurt, victory fanfare, defeat sting. Use libGDX AssetManager for texture loading: hero sprite, 5 enemy sprites, 2 background images, UI elements (buttons, bars, panels). Create loading screen or synchronous load on startup.

### AT-015 — Battle Screen Layout & Visual Polish ⬜
**Sprint 2 | 5 SP | Composite**
Implement the full battle screen layout: background image (fullscreen), hero sprite (left side, 20% from left), enemy sprites (right side, evenly spaced), health bars above each character (red bar on dark background), action menu (bottom center, 4 styled buttons in row), battle log (bottom-left, semi-transparent dark background, white text), turn indicator (top center, text showing current turn). Add damage number popups (float up and fade) using simple tween. Add screen shake on hit (2-3 frame offset).
