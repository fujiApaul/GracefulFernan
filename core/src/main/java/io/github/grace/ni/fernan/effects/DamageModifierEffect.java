package io.github.grace.ni.fernan.effects;

import io.github.grace.ni.fernan.SkillEffect;
import io.github.grace.ni.fernan.CardSystem.Card;
import io.github.grace.ni.fernan.status.DamageModifierStatus;

public class DamageModifierEffect implements SkillEffect {
    private float pct;
    private int duration;

    /** JSON needs this */
    public DamageModifierEffect() {}

    public DamageModifierEffect(float pct, int duration) {
        this.pct = pct;
        this.duration = duration;
    }

    /** Setters for JSON deserialization */
    public void setPct(float pct) {
        this.pct = pct;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public void apply(Card user, Card target) {
        target.addStatus(new DamageModifierStatus(pct, duration));
    }
}
