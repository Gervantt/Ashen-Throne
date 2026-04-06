package com.ashenthrone.characters;

/**
 * Fluent builder for Enemy.
 * Usage: new EnemyBuilder().name("Shadow Crawler").type("ShadowCrawler").hp(40).attack(10).defense(5).speed(8).build()
 */
public class EnemyBuilder {

    private final Enemy enemy;

    public EnemyBuilder() {
        enemy = new Enemy();
    }

    public EnemyBuilder name(String name) {
        enemy.name = name;
        return this;
    }

    public EnemyBuilder type(String type) {
        enemy.setType(type);
        return this;
    }

    public EnemyBuilder hp(int hp) {
        enemy.hp = hp;
        enemy.maxHp = hp;
        return this;
    }

    public EnemyBuilder attack(int attack) {
        enemy.attack = attack;
        return this;
    }

    public EnemyBuilder defense(int defense) {
        enemy.defense = defense;
        return this;
    }

    public EnemyBuilder speed(int speed) {
        enemy.speed = speed;
        return this;
    }

    // TODO: public EnemyBuilder strategy(AttackStrategy strategy) — AT-008

    public Enemy build() {
        if (enemy.name == null) throw new IllegalStateException("Enemy must have a name");
        if (enemy.maxHp <= 0) throw new IllegalStateException("Enemy must have positive HP");
        return enemy;
    }
}
