package io.github.grace.ni.fernan.effects;

import io.github.grace.ni.fernan.SkillEffect;
import io.github.grace.ni.fernan.CardSystem.Card;
import io.github.grace.ni.fernan.status.BurnStatus; // Import BurnStatus

public class BurnEffect implements SkillEffect {
    private int dmg; // Damage per turn
    private int duration;

    /** JSON needs this */
    public BurnEffect() {
    }

    public BurnEffect(int dmg, int duration) {
        this.dmg      = dmg;
        this.duration = duration;
    }

    /** Setters for JSON deserialization */
    public void setDmg(int dmg) {
        this.dmg = dmg;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public void apply(Card user, Card target) {
        target.addStatus(new BurnStatus(dmg, duration)); // Apply BurnStatus
    }
}
