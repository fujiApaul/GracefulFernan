package io.github.grace.ni.fernan.effects;

import io.github.grace.ni.fernan.SkillEffect;
import io.github.grace.ni.fernan.CardSystem.Card;
import io.github.grace.ni.fernan.status.FlatDamageBuffStatus;
import io.github.grace.ni.fernan.status.DamageModifierStatus;
import io.github.grace.ni.fernan.status.Status;

public class DamageEffect implements SkillEffect {
    private int baseAmount;

    /** JSON needs this */
    public DamageEffect() {
    }

    /**
     * @param amount base damage before buffs/debuffs
     */
    public DamageEffect(int amount) {
        this.baseAmount = amount;
    }

    /** Setter for JSON deserialization */
    public void setBaseAmount(int baseAmount) {
        this.baseAmount = baseAmount;
    }

    @Override
    public void apply(Card user, Card target) {
        // 1) collect flat bonuses
        int flatBonus = 0;
        for (Status st : user.getStatuses()) {
            if (st instanceof FlatDamageBuffStatus) {
                flatBonus += ((FlatDamageBuffStatus) st).getBonusDamage();
            }
        }

        // 2) collect percent modifiers
        float percentMod = 0f;
        for (Status st : user.getStatuses()) {
            if (st instanceof DamageModifierStatus) {
                percentMod += ((DamageModifierStatus) st).getPercent();
            }
        }

        // 3) compute final damage
        float adjusted = (baseAmount + flatBonus) * (1 + percentMod);
        int finalDamage = Math.round(adjusted);

        // 4) apply to target
        target.takeDamage(finalDamage);
    }
}
