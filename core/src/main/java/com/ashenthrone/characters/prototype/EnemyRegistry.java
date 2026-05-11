package com.ashenthrone.characters.prototype;

import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.EnemyBuilder;
import com.ashenthrone.strategy.MagicAttack;
import com.ashenthrone.strategy.PhysicalAttack;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Prototype registry for enemy templates.
 *
 * Loaded once at startup. All enemy spawning goes through {@link #spawn(String)},
 * which clones the stored template and applies ±5% HP variance so no two
 * encounters feel identical.
 *
 * Usage:
 *   EnemyRegistry registry = EnemyRegistry.getInstance();
 *   Enemy crawler = registry.spawn("ShadowCrawler");
 */
public class EnemyRegistry {

    private static EnemyRegistry instance;

    private final Map<String, Enemy> templates = new HashMap<>();
    private final Random random = new Random();

    private EnemyRegistry() {
        registerDefaults();
    }

    public static EnemyRegistry getInstance() {
        if (instance == null) {
            instance = new EnemyRegistry();
        }
        return instance;
    }

    // ---- Public API ----

    /**
     * Returns a fresh clone of the named template with ±5% HP variance.
     *
     * @param type the enemy type key (e.g. "ShadowCrawler")
     * @throws IllegalArgumentException if the type is not registered
     */
    public Enemy spawn(String type) {
        Enemy template = templates.get(type);
        if (template == null) {
            throw new IllegalArgumentException("Unknown enemy type: " + type);
        }
        Enemy copy = template.clone();
        applyHpVariance(copy);
        return copy;
    }

    /**
     * Registers a custom template, or replaces an existing one.
     * Useful for tests and future content additions.
     */
    public void register(String type, Enemy template) {
        if (type == null) throw new IllegalArgumentException("Enemy type key must not be null");
        if (template == null) throw new IllegalArgumentException("Enemy template must not be null");
        templates.put(type, template);
    }

    /** Returns true if a template with this key exists. */
    public boolean has(String type) {
        return templates.containsKey(type);
    }

    // ---- Internal helpers ----

    /** Applies a random HP multiplier in the range [0.95, 1.05] to a freshly cloned enemy. */
    private void applyHpVariance(Enemy enemy) {
        double variance = 0.95 + random.nextDouble() * 0.10; // [0.95, 1.05)
        int variedMaxHp = (int) Math.round(enemy.getMaxHp() * variance);
        int variedHp    = Math.min(enemy.getHp(), variedMaxHp);
        enemy.setMaxHp(variedMaxHp);
        enemy.setHp(variedHp);
    }

    /**
     * Registers the five canonical enemy templates.
     *
     * Stats tuned for Sprint-1 balance; adjust alongside AT-008 (Strategy) and
     * AT-010 (DamageCalculator) once the full formula is in place.
     *
     *  Enemy          HP   ATK  DEF  SPD  Role
     *  ShadowCrawler  52   15    6    9   Fast minion
     *  Wraith         46   18    4   13   Fast magic attacker
     *  HollowWolf     62   17    7   11   Balanced pursuer
     *  Treant        100   15   15    5   Slow tank
     *  HollowKing    380   34   18    9   Boss
     */
    private void registerDefaults() {
        // ShadowCrawler — fast melee minion uses a straight physical strike.
        Enemy shadowCrawler = new EnemyBuilder()
                .name("Shadow Crawler").type("ShadowCrawler")
                .hp(52).attack(15).defense(6).speed(9)
                .build();
        shadowCrawler.setCurrentStrategy(new PhysicalAttack());
        templates.put("ShadowCrawler", shadowCrawler);

        // Wraith — fast arcane attacker; ignores half of the hero's defence.
        Enemy wraith = new EnemyBuilder()
                .name("Wraith").type("Wraith")
                .hp(46).attack(18).defense(4).speed(13)
                .build();
        wraith.setCurrentStrategy(new MagicAttack());
        templates.put("Wraith", wraith);

        // Remaining enemies default to physical combat; strategy fallback in Enemy.chooseAction().
        templates.put("HollowWolf", new EnemyBuilder()
                .name("Hollow Wolf").type("HollowWolf")
                .hp(62).attack(17).defense(7).speed(11)
                .build());

        templates.put("Treant", new EnemyBuilder()
                .name("Treant").type("Treant")
                .hp(100).attack(15).defense(15).speed(5)
                .build());

        templates.put("HollowKing", new EnemyBuilder()
                .name("Hollow King").type("HollowKing")
                .hp(380).attack(34).defense(18).speed(9)
                .build());
    }
}
