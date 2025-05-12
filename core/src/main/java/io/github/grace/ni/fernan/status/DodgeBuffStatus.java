package io.github.grace.ni.fernan.status;

import io.github.grace.ni.fernan.CardSystem.Card;

/**
 * Gives the owner a chance to dodge incoming attacks.
 */
public class DodgeBuffStatus implements Status {
    private int remainingTurns;
    private final float dodgeChance;

    public DodgeBuffStatus(float dodgeChance, int duration) {
        this.dodgeChance    = dodgeChance;
        this.remainingTurns = duration;
    }

    @Override
    public void onTurnStart(Card owner) {
        // expire at turn start
        remainingTurns--;
    }

    @Override
    public boolean isNegative() {
        return true;
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
     * Called by your incoming‐attack logic to roll a dodge.
     */
    public float getDodgeChance() {
        return dodgeChance;
    }
}
