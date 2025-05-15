package io.github.grace.ni.fernan.status;

import io.github.grace.ni.fernan.CardSystem.Card;

public class BurnStatus implements Status {
    private int remainingTurns;
    private int damagePerTurn; // Made non-final

    /** No-argument constructor for JSON deserialization */
    public BurnStatus() {
        // Default values if necessary, or leave them to be set by deserializer
        this.damagePerTurn = 0;
        this.remainingTurns = 0;
    }

    public BurnStatus(int damagePerTurn, int duration) {
        this.damagePerTurn   = damagePerTurn;
        this.remainingTurns  = duration;
    }

    // Getters (you likely have these or need them)
    public int getRemainingTurns() {
        return remainingTurns;
    }

    public int getDamagePerTurn() {
        return damagePerTurn;
    }

    // Setters for JSON deserialization if fields were private
    public void setRemainingTurns(int remainingTurns) {
        this.remainingTurns = remainingTurns;
    }

    public void setDamagePerTurn(int damagePerTurn) {
        this.damagePerTurn = damagePerTurn;
    }

    @Override
    public void onTurnStart(Card owner) {
        owner.takeDamage(damagePerTurn);
        remainingTurns--;
    }

    @Override
    public boolean isNegative() {
        return true;
    }

    @Override public void onTurnEnd(Card owner) { /* no-op */ }
    @Override public boolean isExpired() { return remainingTurns <= 0; }
}
