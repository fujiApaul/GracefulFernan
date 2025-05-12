// StunStatus.java
package io.github.grace.ni.fernan.status;

import io.github.grace.ni.fernan.CardSystem.Card;

public class StunStatus implements Status {
    private int remainingTurns;
    public StunStatus(int duration) { this.remainingTurns = duration; }
    @Override
    public void onTurnStart(Card owner) { remainingTurns--; }
    @Override public void onTurnEnd(Card owner) { /* no-op */ }
    @Override public boolean isExpired() { return remainingTurns < 0; }

    @Override
    public boolean isNegative() {
        return true;
    }
}
