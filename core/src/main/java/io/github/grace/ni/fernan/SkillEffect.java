package io.github.grace.ni.fernan;

import io.github.grace.ni.fernan.CardSystem.Card;

public interface SkillEffect {
    void apply(Card user, Card target);

    class DamageEffect implements SkillEffect {
        private final int amount;

        public DamageEffect(int amount) {
            this.amount = amount;
        }

        @Override
        public void apply(Card user, Card target) {
            target.health -= amount;
            System.out.println(user.getName() + " dealt " + amount + " damage to " + target.getName());
        }
    }

    class HealEffect implements SkillEffect {
        private final int amount;

        public HealEffect(int amount) {
            this.amount = amount;
        }

        @Override
        public void apply(Card user, Card target) {
            target.health += amount;
            System.out.println(user.getName() + " healed " + target.getName() + " for " + amount);
        }
    }
}
