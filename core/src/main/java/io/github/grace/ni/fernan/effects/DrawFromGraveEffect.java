package io.github.grace.ni.fernan.effects;

import io.github.grace.ni.fernan.SkillEffect;
import io.github.grace.ni.fernan.CardSystem.Card;

public class DrawFromGraveEffect implements SkillEffect {
    private int count;

    /** JSON needs this */
    public DrawFromGraveEffect() {
    }

    public DrawFromGraveEffect(int count) {
        this.count = count;
    }

    /** Setter for JSON deserialization */
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public void apply(Card user, Card target) {
        for (int i = 0; i < count; i++) {
            Card c = user.drawFromGraveyard();
            if (c != null) user.addToHand(c);
        }
    }
}
