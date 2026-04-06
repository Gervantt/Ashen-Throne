package com.ashenthrone.characters;

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
     * Basic AI: if HP is above 50%, attack. Otherwise, attempt to heal
     * (if a heal strategy is available) or just attack anyway.
     * Will be refined once Strategy pattern (AT-008) is integrated.
     */
    @Override
    protected void chooseAction() {
        // TODO: use AttackStrategy based on AI logic
        // Placeholder — always attacks for now.
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
            throw new AssertionError(e);
        }
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
