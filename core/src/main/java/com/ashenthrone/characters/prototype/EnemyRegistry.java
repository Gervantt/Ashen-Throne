package com.ashenthrone.characters.prototype;

import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.EnemyBuilder;

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
     *  ShadowCrawler  40   10    5    8   Fast minion
     *  Wraith         35   14    3   12   Fast magic attacker
     *  HollowWolf     50   12    6   10   Balanced pursuer
     *  Treant         80    8   14    4   Slow tank
     *  HollowKing    120   18   10    7   Boss
     */
    private void registerDefaults() {
        templates.put("ShadowCrawler", new EnemyBuilder()
                .name("Shadow Crawler").type("ShadowCrawler")
                .hp(40).attack(10).defense(5).speed(8)
                .build());

        templates.put("Wraith", new EnemyBuilder()
                .name("Wraith").type("Wraith")
                .hp(35).attack(14).defense(3).speed(12)
                .build());

        templates.put("HollowWolf", new EnemyBuilder()
                .name("Hollow Wolf").type("HollowWolf")
                .hp(50).attack(12).defense(6).speed(10)
                .build());

        templates.put("Treant", new EnemyBuilder()
                .name("Treant").type("Treant")
                .hp(80).attack(8).defense(14).speed(4)
                .build());

        templates.put("HollowKing", new EnemyBuilder()
                .name("Hollow King").type("HollowKing")
                .hp(120).attack(18).defense(10).speed(7)
                .build());
    }
}