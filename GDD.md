# Ashen Throne — Game Design Document

**Genre:** Turn-based dark fantasy RPG
**Platform:** Desktop (Windows / macOS / Linux)
**Engine:** Java 17 + libGDX 1.12
**Resolution:** 1280×720

---

## 1. Game Summary

*Ashen Throne* is a single-hero, turn-based dark fantasy combat game where the player picks one of five heroes and fights through three escalating realms — The Abyss, the Cursed Forest, and the Ashen Throne — each composed of one or more enemy waves. Combat is menu-driven: every turn the player chooses **Attack**, **Skill**, **Defend**, or **Item**, and resolves the action against the chosen target. Gold earned from victories is spent in the shop on equipment that permanently boosts the hero's stats. The player wins by defeating the final boss, **The Hollow King**, and loses if the hero's HP drops to zero (with the option to retry the wave).

---

## 2. Controls

### Battle screen
| Key | Action |
|-----|--------|
| `1` | Select Attack |
| `2` | Select Skill |
| `3` | Select Defend |
| `4` | Select Item |
| `←` / `→` or mouse click | Select target enemy |
| `Enter` | Confirm action |
| `Escape` | Cancel selection / open Pause menu |
| `U` | Undo last action (before confirming the turn) |

### Menu screens
| Key | Action |
|-----|--------|
| Mouse click | Press any button |
| `Escape` | Back to previous screen |

---

## 3. Player

### Heroes (pick one per run)
| Hero | Role | HP | ATK | DEF | SPD | Default Skill |
|------|------|----|-----|-----|-----|---------------|
| **Kael** | Warrior — balanced | 120 | 18 | 12 | 10 | PhysicalAttack |
| **Malachar** | Dark mage — glass cannon | 90 | 24 | 6 | 11 | MagicAttack (ignores 50% DEF) |
| **Thane** | Tank — soak damage | 160 | 12 | 18 | 7 | PhysicalAttack |
| **Vex** | Assassin — fast, fragile | 95 | 20 | 8 | 16 | PhysicalAttack |
| **Sera** | Healer — sustain | 110 | 14 | 10 | 11 | HealSelf (+30% maxHP) |

### Player abilities (Strategy pattern)
- **PhysicalAttack** — single target, damage = `ATK − DEF`
- **MagicAttack** — single target, ignores 50% of target DEF
- **HealSelf** — restores 30% of maxHP
- **AreaOfEffect** — hits all enemies for 60% damage
- **Defend** — halves incoming damage for one turn
- **Item use** — consume an item from inventory

### Equipment (Decorator pattern, bought in shop)
| Item | Effect | Cost |
|------|--------|------|
| Fire Amulet | +5 ATK | 50 g |
| Cursed Ring | +8 DEF, −2 ATK | 40 g |
| Shadow Blade | +10 ATK | 80 g |

---

## 4. Enemies

| Enemy | Realm | Tier | HP | ATK | DEF | SPD | Behaviour | Gold |
|-------|-------|------|----|----|-----|-----|-----------|------|
| **Stonewarden** (blue golem) | The Abyss | Minion | 60 | 10 | 12 | 6 | PhysicalAttack on hero | 10–20 |
| **Emberclaw** (orange golem) | The Abyss | Elite | 110 | 16 | 10 | 9 | PhysicalAttack, hits harder | 30–50 |
| **Hollow Wolf** | Cursed Forest | Minion | 50 | 12 | 6 | 14 | Fast PhysicalAttack | 10–20 |
| **Treant** | Cursed Forest | Elite | 140 | 14 | 14 | 5 | Heavy PhysicalAttack | 30–50 |
| **The Hollow King** | Ashen Throne | Boss | 250 | 22 | 14 | 10 | Mix of MagicAttack + AoE every 3rd turn | 100 |

All enemies are spawned via the **Prototype** registry (deep clone with ±5% HP variance) and assembled into waves by **Abstract Factory** + **Iterator** (`WaveIterator`).

---

## 5. Levels (Realms & Waves)

The game has **3 realms**, played top-to-bottom in a tower. Each realm is unlocked only after the previous one is cleared.

| # | Realm | Wave 1 | Wave 2 | Background |
|---|-------|--------|--------|------------|
| 1 | **The Abyss** | 2× Stonewarden | 1× Emberclaw + 2× Stonewarden | Dark cavern |
| 2 | **Cursed Forest** | 3× Hollow Wolves | 1× Treant + 2× Hollow Wolves | Twisted forest |
| 3 | **Ashen Throne** | 1× The Hollow King (final boss) | — | Throne hall |

Between waves: short reward screen showing gold earned. Realm complete → return to RealmSelectScreen with the realm marked as conquered. Final boss defeated → EndGameScreen.

---

## 6. Win / Lose Screens

### Victory screen (shown after every won wave)
- Gold earned (animated counter)
- "Continue" → next wave, or RealmSelectScreen if realm is cleared
- Plays `victory_sting` SFX

### Defeat screen (HP hits 0)
- "Reborn" — restart the current wave with full HP
- "Main Menu" — back to MainMenuScreen
- Plays `defeat_sting` SFX

### End-game screen (after Hollow King)
- Congratulatory text and credits
- Single button: "Return to Main Menu"

### Pause screen (Escape during battle)
- Dimmed overlay
- "Resume" / "Quit to Menu"
- Pauses game logic and AI

---

## 7. Game Flow

```
MainMenu ──► HeroSelect ──► RealmSelect ──► Battle (wave 1)
   │              │             ▲              │
   │              │             │              ▼
   │              │             │          Victory ──► Battle (wave 2) ──► Victory (realm done)
   │              │             └──────────────────────────────────────────────┘
   │              │
   ├─► Shop ──────┘
   ├─► Settings
   └─► Exit

Battle ──► Defeat ──► (Reborn) Battle | (Main Menu) MainMenu
Hollow King defeated ──► EndGame ──► MainMenu
```

All transitions go through **TransitionManager** (Mediator pattern): fade-out → swap music → setScreen → fade-in.

---

## 8. Art Style

- **Pixel-art / hand-painted dark fantasy** — sources from itch.io / OpenGameArt under CC licences
- **Palette:** muted blacks, deep blues, blood reds, ash greys; accent colours for elites (orange) and bosses (purple)
- **Sprites:** static 2D; combat shows hero on the left, enemies on the right
- **UI:** dark semi-transparent panels with light text, ornate frame borders for buttons
- **Backgrounds:** one full-screen image per realm
- **Effects:** floating damage numbers (tween up + fade), screen shake on hit (2–3 frame offset), 0.3 s fade between screens

---

## 9. Audio

### Music (looped)
- `main_theme` — Main menu, hero/realm/shop/settings screens
- `battle_theme` — Standard waves
- `boss_theme` — Hollow King fight

### One-shot stings
- `victory_sting`, `defeat_sting`

### SFX
- `sword_hit` — PhysicalAttack
- `fireball` — MagicAttack
- `heal_cast` — HealSelf
- `enemy_death`, `hero_hurt`
- `transition_whoosh` — every button / screen change
- `purchase_sound` — successful shop buy

All audio is routed through the **AudioManager** singleton, which subscribes to `EventManager` events to fire the correct SFX automatically.

---

## 10. Architecture (summary)

The game implements **16 Gang of Four design patterns** as part of the SDP coursework:

| # | Pattern | Where |
|---|---------|-------|
| 1 | Singleton | `GameSession`, `AudioManager`, `TransitionManager`, `EventManager` |
| 2 | Builder | `HeroBuilder`, `EnemyBuilder` |
| 3 | Prototype | `Enemy.clone()` + `EnemyRegistry` |
| 4 | Abstract Factory | `RealmFactory` (Abyss, Forest, Throne) |
| 5 | Factory Method | `EnemySpawner.createEnemy()` |
| 6 | Decorator | `CharacterDecorator` + equipment |
| 7 | State | `BattleState` (Player/Enemy/Animation/Victory/Defeat/Pause) |
| 8 | Command | `BattleCommand` + undo stack |
| 9 | Strategy | `AttackStrategy` (Physical/Magic/Heal/AoE) |
| 10 | Observer | `EventManager` ↔ HUD/Audio/Log |
| 11 | Facade | `BattleEngine` |
| 12 | Composite | `UIComponent` tree |
| 13 | Adapter | `BattleInputAdapter` (libGDX → game events) |
| 14 | Template Method | `AbstractCharacter.takeTurn()` |
| 15 | Mediator | `TransitionManager` |
| 16 | Iterator | `WaveIterator` per realm |

See `CLAUDE.md` for full task breakdown (AT-001 — AT-026).

---

## 11. Required Diagrams

To be produced in **draw.io** and exported to PDF/PNG (per SDP guideline):

1. **Game Flow Diagram** — all 9 screens and their transitions (see Section 7).
2. **Class Diagram** — main classes per package (`characters/`, `battle/`, `factory/`, `strategy/`, `equipment/`, `ui/`, `screens/`).
3. **Level Sketch** — layout of one battle screen (hero left, enemies right, HUD positions).

Stored under `/docs/diagrams/`.
