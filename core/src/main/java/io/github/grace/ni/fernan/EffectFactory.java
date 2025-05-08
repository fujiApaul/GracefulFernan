package io.github.grace.ni.fernan;

import io.github.grace.ni.fernan.SkillEffect;

public class EffectFactory {
    public static SkillEffect createEffect(String type, int amount) {
        switch (type.toUpperCase()) {
            case "DAMAGE":
                return new SkillEffect.DamageEffect(amount);
            case "HEAL":
                return new SkillEffect.HealEffect(amount);
            default:
                return null;
        }
    }
}
