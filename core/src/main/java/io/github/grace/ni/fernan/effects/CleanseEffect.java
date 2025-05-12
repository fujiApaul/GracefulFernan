package io.github.grace.ni.fernan.effects;

import io.github.grace.ni.fernan.CardSystem.Card;
import io.github.grace.ni.fernan.SkillEffect;
import io.github.grace.ni.fernan.status.Status;

public class CleanseEffect implements SkillEffect {
    /** JSON needs a public no-arg constructor */
    public CleanseEffect() {
    }

    @Override
    public void apply(Card user, Card target) {
        // Remove any status that your design marks as negative:
        // Here we assume each Status has an isNegative() helper.
        target.getStatuses().removeIf(Status::isNegative);
    }
}
