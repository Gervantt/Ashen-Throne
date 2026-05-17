package com.ashenthrone.characters.prototype;

import com.ashenthrone.characters.Enemy;
import com.ashenthrone.characters.EnemyBuilder;
import com.ashenthrone.strategy.MagicAttack;
import com.ashenthrone.strategy.PhysicalAttack;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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

    public Enemy spawn(String type) {
        Enemy template = templates.get(type);
        if (template == null) {
            throw new IllegalArgumentException("Unknown enemy type: " + type);
        }
        Enemy copy = template.clone();
        applyHpVariance(copy);
        return copy;
    }


    public void register(String type, Enemy template) {
        if (type == null) throw new IllegalArgumentException("Enemy type key must not be null");
        if (template == null) throw new IllegalArgumentException("Enemy template must not be null");
        templates.put(type, template);
    }

    public boolean has(String type) {
        return templates.containsKey(type);
    }

    private void applyHpVariance(Enemy enemy) {
        double variance = 0.95 + random.nextDouble() * 0.10;
        int variedMaxHp = (int) Math.round(enemy.getMaxHp() * variance);
        int variedHp    = Math.min(enemy.getHp(), variedMaxHp);
        enemy.setMaxHp(variedMaxHp);
        enemy.setHp(variedHp);
    }


    private void registerDefaults() {

        Enemy shadowCrawler = new EnemyBuilder()
                .name("Shadow Crawler").type("ShadowCrawler")
                .hp(52).attack(15).defense(6).speed(9)
                .build();
        shadowCrawler.setCurrentStrategy(new PhysicalAttack());
        templates.put("ShadowCrawler", shadowCrawler);

        Enemy wraith = new EnemyBuilder()
                .name("Wraith").type("Wraith")
                .hp(46).attack(18).defense(4).speed(13)
                .build();
        wraith.setCurrentStrategy(new MagicAttack());
        templates.put("Wraith", wraith);

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
