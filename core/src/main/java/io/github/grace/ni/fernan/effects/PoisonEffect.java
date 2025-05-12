package io.github.grace.ni.fernan.effects;

import io.github.grace.ni.fernan.SkillEffect;
import io.github.grace.ni.fernan.CardSystem.Card;
import io.github.grace.ni.fernan.status.PoisonStatus;

public class PoisonEffect implements SkillEffect {
    private int dmg;
    private int duration;

    /** JSON needs this */
    public PoisonEffect() {
    }

    public PoisonEffect(int dmg, int duration) {
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
        target.addStatus(new PoisonStatus(dmg, duration));
    }
}
