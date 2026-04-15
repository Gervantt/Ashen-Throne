package com.ashenthrone.battle;

import com.ashenthrone.battle.command.BattleCommand;
import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.Hero;
import com.ashenthrone.strategy.AttackStrategy;
import com.ashenthrone.strategy.MagicAttack;
import com.ashenthrone.strategy.PhysicalAttack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Facade that coordinates all battle subsystems (AT-010).
 *
 * BattleScreen is the only caller — it never reaches into TurnManager,
 * DamageCalculator, StatusEffectProcessor, or DeathChecker directly.
 *
 * Lifecycle:
 *   engine.startBattle(hero, enemies)   — call once per encounter
 *   engine.executePlayerAction(cmd)     — called by PlayerTurnState on confirm
 *   engine.executeEnemyTurns()          — called by EnemyTurnState on update
 *   engine.isOver() / engine.getResult() — queried by states after each action
 *
 * Command history (undo) was previously owned by BattleScreen; it now lives
 * here so all battle state is centralised in the engine.
 */
public class BattleEngine {

    // ---- Subsystems ----
    private final TurnManager            turnManager            = new TurnManager();
    private final DamageCalculator       damageCalculator       = new DamageCalculator();
    private final StatusEffectProcessor  statusEffectProcessor  = new StatusEffectProcessor();
    private final DeathChecker           deathChecker           = new DeathChecker();

    // ---- Battle state ----
    private Hero         hero;
    private List<Enemy>  enemies;

    // ---- Command history (moved from BattleScreen) ----
    private final Deque<BattleCommand> commandHistory = new ArrayDeque<>();

    // ---- Lifecycle ----

    /**
     * Initialises battle state. Must be called before any other method.
     * Safe to call again to reset for a retry (replaces previous state).
     */
    public void startBattle(Hero hero, List<Enemy> enemies) {
        if (hero == null)    throw new IllegalArgumentException("hero must not be null");
        if (enemies == null) throw new IllegalArgumentException("enemies must not be null");
        this.hero    = hero;
        this.enemies = new ArrayList<>(enemies);
        commandHistory.clear();
    }

    // ---- Player action ----

    /**
     * Executes a player-chosen command and records it on the undo stack.
     *
     * @throws IllegalStateException if startBattle has not been called
     */
    public void executePlayerAction(BattleCommand cmd) {
        requireStarted();
        if (cmd == null) return;
        cmd.execute();
        commandHistory.push(cmd);
    }

    /** Undoes the most recently executed player command. No-op if history is empty. */
    public void undoLastCommand() {
        if (!commandHistory.isEmpty()) {
            commandHistory.pop().undo();
        }
    }

    /** True if there is at least one command that can be undone. */
    public boolean canUndo() {
        return !commandHistory.isEmpty();
    }

    // ---- Enemy phase ----

    /**
     * Runs every living enemy's turn in speed order (fastest first).
     *
     * For each enemy:
     *   1. Status effects are ticked via StatusEffectProcessor.
     *   2. The enemy's AttackStrategy determines the defence-penetration modifier
     *      (MagicAttack ignores 50 % of defence; everything else uses full defence).
     *   3. DamageCalculator applies the base formula + crit roll.
     *   4. Damage is applied to the hero via takeDamage().
     *
     * Stops early if the hero dies mid-turn.
     */
    public void executeEnemyTurns() {
        requireStarted();
        List<Enemy> actingEnemies = turnManager.getActingOrder(enemies);

        for (Enemy enemy : actingEnemies) {
            if (!hero.isAlive()) break;

            statusEffectProcessor.process(enemy);

            AttackStrategy strategy = enemy.getCurrentStrategy();
            if (strategy == null) strategy = new PhysicalAttack();

            // Strategy type determines defence penetration; DamageCalculator applies the formula.
            int effectiveDefense = (strategy instanceof MagicAttack)
                    ? hero.getDefense() / 2
                    : hero.getDefense();

            int damage = damageCalculator.calculate(enemy.getAttack(), effectiveDefense);
            hero.takeDamage(damage);
        }
    }

    // ---- Win / lose checks ----

    /**
     * True if the battle is over (hero dead or all enemies dead).
     * Query this after every player action and after executeEnemyTurns().
     */
    public boolean isOver() {
        requireStarted();
        return deathChecker.isOver(hero, enemies);
    }

    /**
     * Returns "VICTORY", "DEFEAT", or {@code null} if the battle is still ongoing.
     * Victory takes precedence if both conditions are somehow met simultaneously.
     */
    public String getResult() {
        requireStarted();
        return deathChecker.getResult(hero, enemies);
    }

    // ---- Accessors ----

    public Hero getHero() {
        requireStarted();
        return hero;
    }

    /** Returns an unmodifiable view of the enemy list. */
    public List<Enemy> getEnemies() {
        requireStarted();
        return Collections.unmodifiableList(enemies);
    }

    // ---- Internal ----

    private void requireStarted() {
        if (hero == null) {
            throw new IllegalStateException("BattleEngine.startBattle() must be called before use");
        }
    }
}