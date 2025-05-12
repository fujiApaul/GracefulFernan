package io.github.grace.ni.fernan.status;

import io.github.grace.ni.fernan.CardSystem.Card;

/**
 * Modifies damage dealt or taken by a percentage.
 * positive percent = buff (deal more); negative = debuff (deal less).
 */
public class DamageModifierStatus implements Status {
    private int remainingTurns;
    private final float percent;

    public DamageModifierStatus(float percent, int duration) {
        this.percent        = percent;
        this.remainingTurns = duration;
    }

    @Override
    public void onTurnStart(Card owner) {
        // expire at turn start
        remainingTurns--;
    }

    @Override
    public void onTurnEnd(Card owner) {
        // no end‐of‐turn behavior
    }

    @Override
    public boolean isExpired() {
        return remainingTurns <= 0;
    }

    /**
     * Called by your damage calculation logic to adjust the raw damage.
     * e.g. finalDamage = baseDamage * (1 + percent)
     */
    public float getPercent() {
        return percent;
    }
}
