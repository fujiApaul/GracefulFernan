package io.github.grace.ni.fernan.effects;

import io.github.grace.ni.fernan.SkillEffect;
import io.github.grace.ni.fernan.CardSystem.Card;
import io.github.grace.ni.fernan.status.ConfuseStatus;

public class ConfuseEffect implements SkillEffect {
    private float chance;
    private int duration;

    /** JSON needs this */
    public ConfuseEffect() {}

    public ConfuseEffect(float chance, int duration) {
        this.chance = chance;
        this.duration = duration;
    }

    /** Setters for JSON deserialization */
    public void setChance(float chance) {
        this.chance = chance;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public void apply(Card user, Card target) {
        target.addStatus(new ConfuseStatus(chance, duration));
    }
}
