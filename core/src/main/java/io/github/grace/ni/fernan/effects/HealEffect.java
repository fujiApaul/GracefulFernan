package io.github.grace.ni.fernan.effects;

import io.github.grace.ni.fernan.SkillEffect;
import io.github.grace.ni.fernan.CardSystem.Card;

public class HealEffect implements SkillEffect {
    private int amount;

    /** JSON needs this */
    public HealEffect() {
    }

    public HealEffect(int amount) {
        this.amount = amount;
    }

    /** Setter for JSON deserialization */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public void apply(Card user, Card target) {
        target.heal(amount);
    }
}
