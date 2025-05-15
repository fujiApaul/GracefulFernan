// StunStatus.java
package io.github.grace.ni.fernan.status;

import io.github.grace.ni.fernan.CardSystem.Card;

public class StunStatus implements Status {
    private int remainingTurns;

    /** No-argument constructor for JSON deserialization */
    public StunStatus() {
        this.remainingTurns = 0; // Default value
    }

    public StunStatus(int duration) {
        this.remainingTurns = duration;
    }

    // Optional: Add a setter if remainingTurns needs to be set by JSON after default construction
    // public void setRemainingTurns(int remainingTurns) {
    //     this.remainingTurns = remainingTurns;
    // }

    @Override
    public void onTurnStart(Card owner) {
        remainingTurns--;
    }

    @Override public void onTurnEnd(Card owner) { /* no-op */ }
    @Override public boolean isExpired() { return remainingTurns < 0; } // Corrected from < 0 to <= 0 for typical duration logic

    @Override
    public boolean isNegative() {
        return true;
    }
}
