// PoisonStatus.java
package io.github.grace.ni.fernan.status;

import io.github.grace.ni.fernan.CardSystem.Card;

public class PoisonStatus implements Status {
    private int remainingTurns;
    private final int damagePerTurn;
    public PoisonStatus(int damagePerTurn, int duration) {
        this.damagePerTurn   = damagePerTurn;
        this.remainingTurns  = duration;
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
