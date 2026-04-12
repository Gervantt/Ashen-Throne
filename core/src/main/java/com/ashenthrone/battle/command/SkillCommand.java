package com.ashenthrone.battle.command;

import com.ashenthrone.characters.AbstractCharacter;
import com.ashenthrone.strategy.AttackStrategy;
import com.ashenthrone.strategy.PhysicalAttack;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes the attacker's active {@link AttackStrategy} against a list of targets.
 *
 * Because strategies vary (single-target damage, AoE damage, self-heal), undo
 * snapshots the HP of both the attacker and every target before execution and
 * restores them all on undo — a safe, strategy-agnostic reversal.
 *
 * If the attacker has no strategy set, falls back to {@link PhysicalAttack}.
 */
public class SkillCommand implements BattleCommand {

    private final AbstractCharacter attacker;
    private final List<AbstractCharacter> targets;

    // HP snapshots for undo
    private int attackerHpBefore;
    private final List<Integer> targetHpsBefore = new ArrayList<>();

    /**
     * @param attacker the character executing the skill
     * @param targets  the characters affected (may be a single enemy, all enemies,
     *                 or empty — strategy decides what to do)
     */
    public SkillCommand(AbstractCharacter attacker, List<AbstractCharacter> targets) {
        this.attacker = attacker;
        this.targets  = List.copyOf(targets);
    }

    @Override
    public void execute() {
        // Capture HP state before anything changes.
        attackerHpBefore = attacker.getHp();
        targetHpsBefore.clear();
        for (AbstractCharacter t : targets) {
            targetHpsBefore.add(t.getHp());
        }

        AttackStrategy strategy = attacker.getCurrentStrategy();
        if (strategy == null) {
            strategy = new PhysicalAttack();
        }
        strategy.execute(attacker, targets);
    }

    @Override
    public void undo() {
        attacker.setHp(attackerHpBefore);
        for (int i = 0; i < targets.size() && i < targetHpsBefore.size(); i++) {
            targets.get(i).setHp(targetHpsBefore.get(i));
        }
    }
}