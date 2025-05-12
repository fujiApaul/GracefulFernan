package io.github.grace.ni.fernan.status;

import io.github.grace.ni.fernan.CardSystem.Card;

/**
 * Adds a fixed bonus to whichever attack the card makes for N uses.
 */
public class FlatDamageBuffStatus implements Status {
    private int remainingUses;
    private final int bonusDamage;

    public FlatDamageBuffStatus(int bonusDamage, int duration) {
        this.bonusDamage  = bonusDamage;
        this.remainingUses = duration;
    }

    @Override
    public void onTurnStart(Card owner) {
        // no‐op here; we’ll consume it on attack
    }

    @Override
    public void onTurnEnd(Card owner) {
        // no‐op
    }

    @Override
    public boolean isExpired() {
        return remainingUses <= 0;
    }

    /**
     * Called from your damage resolution when this card attacks.
     * You’ll need to hook this into your DamageEffect logic:
     *
     *   int base = …;
     *   int total = base + any FlatDamageBuffStatus.bonusDamage;
     *   apply that damage; then status.remainingUses--;
     */
    public int getBonusDamage() {
        remainingUses--;
        return bonusDamage;
    }
}
