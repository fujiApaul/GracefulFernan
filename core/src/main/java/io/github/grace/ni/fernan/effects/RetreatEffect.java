package io.github.grace.ni.fernan.effects;

import io.github.grace.ni.fernan.SkillEffect;
import io.github.grace.ni.fernan.CardSystem.Card;

public class RetreatEffect implements SkillEffect {
    /** JSON needs this */
    public RetreatEffect() {
    }

    @Override
    public void apply(Card user, Card target) {
        user.swapWithBench(target);
    }
}
