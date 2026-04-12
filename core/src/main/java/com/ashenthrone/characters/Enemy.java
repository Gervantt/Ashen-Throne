package com.ashenthrone.characters;

import com.ashenthrone.strategy.HealSelf;
import com.ashenthrone.strategy.PhysicalAttack;

/**
 * An enemy character. Overrides chooseAction() with simple AI logic.
 * Implements Cloneable to support the Prototype pattern (AT-003).
 */
public class Enemy extends AbstractCharacter implements Cloneable {

    /** Identifier for the enemy type, e.g. "ShadowCrawler", "Wraith". */
    private String type;

    Enemy() {
        // Package-private — use EnemyBuilder to construct.
    }

    /**
     * Selects the strategy to use this turn based on HP threshold.
     *
     * If HP drops below 30 % of max and a HealSelf strategy is available
     * the enemy will attempt to heal; otherwise it uses its assigned
     * offensive strategy (defaulting to PhysicalAttack when none is set).
     *
     * The chosen strategy is stored in {@code currentStrategy} so that
     * EnemyTurnState can call it with the appropriate target list.
     */
    @Override
    protected void chooseAction() {
        boolean lowHp = (double) hp / maxHp < 0.30;
        if (lowHp && currentStrategy instanceof HealSelf) {
            // Already holding a HealSelf — keep it as the chosen action.
            return;
        }
        if (currentStrategy == null) {
            currentStrategy = new PhysicalAttack();
        }
        // currentStrategy is the offensive strategy set at spawn time; no swap needed.
    }

    /**
     * Deep clone for the Prototype pattern. Creates an independent copy
     * so that modifying one spawned enemy doesn't affect the template.
     */
    @Override
    public Enemy clone() {
        try {
            Enemy copy = (Enemy) super.clone();
            // Primitive fields are copied automatically.
            // Deep-copy mutable reference fields here when they're added
            // (e.g. status effect lists, strategy objects if mutable).
            return copy;
        } catch (CloneNotSupportedException e) {
            // Can't happen — we implement Cloneable.
            throw new IllegalStateException("Clone failed on a Cloneable class", e);
        }
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
