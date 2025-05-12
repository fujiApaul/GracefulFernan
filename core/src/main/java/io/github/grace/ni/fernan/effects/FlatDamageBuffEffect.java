package io.github.grace.ni.fernan.effects;

import io.github.grace.ni.fernan.SkillEffect;
import io.github.grace.ni.fernan.CardSystem.Card;
import io.github.grace.ni.fernan.status.FlatDamageBuffStatus;

public class FlatDamageBuffEffect implements SkillEffect {
    private int bonus;
    private int duration;

    /** JSON needs this */
    public FlatDamageBuffEffect() {
    }

    public FlatDamageBuffEffect(int bonus, int duration) {
        this.bonus    = bonus;
        this.duration = duration;
    }

    /** Setters for JSON deserialization */
    public void setBonus(int bonus) {
        this.bonus = bonus;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public void apply(Card user, Card target) {
        user.addStatus(new FlatDamageBuffStatus(bonus, duration));
    }
}
