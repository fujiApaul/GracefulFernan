package io.github.grace.ni.fernan.effects;

import io.github.grace.ni.fernan.SkillEffect;
import io.github.grace.ni.fernan.CardSystem.Card;
import io.github.grace.ni.fernan.status.StunStatus;

public class StunEffect implements SkillEffect {
    private int duration;

    /** JSON needs this */
    public StunEffect() {
    }

    /**
     * @param duration number of turns to stun
     */
    public StunEffect(int duration) {
        this.duration = duration;
    }

    /** Setter for JSON deserialization */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public void apply(Card user, Card target) {
        target.addStatus(new StunStatus(duration));
    }
}
