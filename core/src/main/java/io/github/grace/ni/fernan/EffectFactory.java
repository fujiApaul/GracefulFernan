package io.github.grace.ni.fernan;

import io.github.grace.ni.fernan.effects.*;
import io.github.grace.ni.fernan.status.Status;

public class EffectFactory {
    public static SkillEffect create(CardSystem.CardData.EffectData ed) {
        switch (ed.type) {
            case "Damage":
                return new DamageEffect(ed.amount);
            case "Heal":
                return new HealEffect(ed.amount);
            case "DrawGrave":
                return new DrawFromGraveEffect(ed.amount);
            case "Poison":
                return new PoisonEffect(ed.amount, ed.duration);
            case "Burn": // ADD THIS NEW CASE
                return new BurnEffect(ed.amount, ed.duration); // Use ed.amount for damage per turn
            case "Stun":
                return new StunEffect(ed.duration);
            case "Confuse":
                return new ConfuseEffect(ed.percent, ed.duration);
            case "DodgeBuff":
                return new DodgeBuffEffect(ed.percent, ed.duration);
            case "DamageMod":
                return new DamageModifierEffect(ed.percent, ed.duration);
            case "Collateral":
                return new CollateralDamageEffect(ed.amount);
            case "AoE":
                // pass amount and radius (use ed.duration as radius)
                return new AoEDamageEffect(ed.amount);
            case "Retreat":
                return new RetreatEffect();
            case "FlatDamageBuff":
                return new FlatDamageBuffEffect(ed.amount, ed.duration);
            case "Cleanse":
                return new CleanseEffect();
            default:
                throw new IllegalArgumentException("Unknown effect: " + ed.type);
        }
    }
}
