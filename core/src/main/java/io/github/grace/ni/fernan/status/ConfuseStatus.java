package io.github.grace.ni.fernan.status;

import io.github.grace.ni.fernan.CardSystem.Card;

/**
 * Causes the owner to have a chance to miss their attacks.
 */
public class ConfuseStatus implements Status {
    private int remainingTurns;
    private final float missChance;

    public ConfuseStatus(float missChance, int duration) {
        this.missChance    = missChance;
        this.remainingTurns = duration;
    }

    @Override
    public void onTurnStart(Card owner) {
        // nothing to do on start; reduction happens here
        remainingTurns--;
    }

    @Override
    public void onTurnEnd(Card owner) {
        // no end‐of‐turn behavior
    }

    @Override
    public boolean isNegative() {
        return true;
    }

    @Override
    public boolean isExpired() {
        return remainingTurns <= 0;
    }

    /**
     * Called by your attack resolution logic to see if this attack misses.
     */
    public float getMissChance() {
        return missChance;
    }
}
