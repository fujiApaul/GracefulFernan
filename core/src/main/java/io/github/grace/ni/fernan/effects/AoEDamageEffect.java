package io.github.grace.ni.fernan.effects;

import io.github.grace.ni.fernan.SkillEffect;
import io.github.grace.ni.fernan.CardSystem.Card;

public class AoEDamageEffect implements SkillEffect {
    private int amt;  // removed final

    /** JSON needs this public no-arg constructor */
    public AoEDamageEffect() {
    }

    /** existing constructor */
    public AoEDamageEffect(int amt) {
        this.amt = amt;
    }

    /** setter for JSON deserialization */
    public void setAmt(int amt) {
        this.amt = amt;
    }

    @Override
    public void apply(Card user, Card target) {
        // your original logic
        target.takeDamage(amt);
        for (Card b : user.getBench()) {
            b.takeDamage(amt);
        }
    }
}
