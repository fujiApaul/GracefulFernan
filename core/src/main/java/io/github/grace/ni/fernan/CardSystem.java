package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import io.github.grace.ni.fernan.SkillEffect;
import java.util.List;
import java.util.Iterator;
import io.github.grace.ni.fernan.status.Status;


public class CardSystem {

    public enum CardType {
        DIVINE, GOD, ITEM, ARTIFACT
    }

    public enum CardPantheon {
        GREEK, ROMAN, NONE
    }


    public static class Skill {
        private String name;
        private int cost;
        private List<SkillEffect> effects;

        /** JSON needs a public no-arg constructor */
        public Skill() {
        }

        public Skill(String name, int cost, List<SkillEffect> effects) {
            this.name    = name;
            this.cost    = cost;
            this.effects = effects;
        }

        // ← ADD THESE SETTERS FOR JSON
        public void setName(String name) {
            this.name = name;
        }
        public void setCost(int cost) {
            this.cost = cost;
        }
        public void setEffects(List<SkillEffect> effects) {
            this.effects = effects;
        }

        public String getName() { return name; }
        public int    getCost() { return cost; }

        public void apply(Card user, Card target) {
            for (SkillEffect e : effects) {
                e.apply(user, target);
            }
        }
    }



    public static class Card {
        private String name;
        int health;
        private List<Skill> skills;
        private CardType type;
        private CardPantheon pantheon;
        private String imagePath;
        private final List<io.github.grace.ni.fernan.status.Status> statuses = new ArrayList<>();
        private boolean usedOnceThisBattle = false;
        private boolean usedThisTurn        = false;

        /** Can this card be used right now? */
        public boolean canUse() {
            switch(type) {
                case ITEM:
                    return !usedOnceThisBattle;
                case ARTIFACT:
                    return !usedThisTurn;
                default:
                    return true;
            }
        }

        /** Mark it as having been used. */
        public void markUsed() {
            switch(type) {
                case ITEM:
                    usedOnceThisBattle = true;
                    break;
                case ARTIFACT:
                    usedThisTurn = true;
                    break;
            }
        }

        /** Call this when you actually “consume” the card */
        public void consume() {
            // Move itself into its own graveyard
            graveyard.add(this);
            // If it was in hand, drop it
            hand.remove(this);
        }

        /** Reset artifact usage at the start of each player turn */
        public void resetTurnUsage() {
            usedThisTurn = false;
        }


        public Card(String name, int health, List<Skill> skills, CardType type, CardPantheon pantheon, String imagePath) {
            this.name = name;
            this.health = health;
            this.skills = skills;
            this.type = type;
            this.pantheon = pantheon;
            this.imagePath = imagePath;
        }

        public void setName(String name) {
            this.name = name;
        }
        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }
        public void setPantheon(CardPantheon pantheon) {
            this.pantheon = pantheon;
        }
        public void setType(CardType type) {
            this.type = type;
        }
        public void setHealth(int health) {
            this.health = health;
        }
        public void setSkills(List<Skill> skills) {
            this.skills = skills;
        }


        /** Returns all active status effects on this card. */
        public List<io.github.grace.ni.fernan.status.Status> getStatuses() {
            return statuses;
        }

        public String getImagePath() {
            return imagePath;
        }

        public String getName() { return name; }
        public int getHealth() { return health; }
        public List<Skill> getSkills() { return skills; }
        public CardType getType() { return type; }
        public CardPantheon getPantheon() { return pantheon; }

        /** JSON deserializer needs this */
        public Card() {
        }


        /** Add a status effect to this card. */
        public void addStatus(io.github.grace.ni.fernan.status.Status s) {
            statuses.add(s);
        }

        /** Called by battle logic to advance all statuses. */
        public void applyStartOfTurnStatuses() {
            Iterator<Status> it = statuses.iterator();
            while (it.hasNext()) {
                Status st = it.next();
                st.onTurnStart(this);
                if (st.isExpired()) it.remove();
            }
        }

        /** (Optional) if you need end-of-turn effects */
        public void applyEndOfTurnStatuses() {
            Iterator<Status> it = statuses.iterator();
            while (it.hasNext()) {
                Status st = it.next();
                st.onTurnEnd(this);
                if (st.isExpired()) it.remove();
            }
        }

        // ——————————————————————————————————————
        // Status & damage utilities

        /** Subtracts health, clamped at 0. */
        public void takeDamage(int amount) {
            this.health = Math.max(0, this.health - amount);
        }

        /** Heals up to whatever max you want (or unlimited). */
        public void heal(int amount) {
            this.health += amount;
        }

        // ——————————————————————————————————————
        // Bench slots for multi‐card effects

        /** Cards sitting on the bench beside this one. */
        private final List<Card> bench = new ArrayList<>();
        // for DrawFromGraveEffect
        private final List<Card> hand = new ArrayList<>();
        private final List<Card> graveyard = new ArrayList<>();

        /** Remove and return one random card from the graveyard (or null if empty). */
        public Card drawFromGraveyard() {
            if (graveyard.isEmpty()) return null;
            // pick a random one:
            return graveyard.remove((int)(Math.random() * graveyard.size()));
        }

        /** Put a card into this card’s hand. */
        public void addToHand(Card c) {
            if (c != null) {
                hand.add(c);
            }
        }


        /** Returns the bench group (empty by default). */
        public List<Card> getBench() {
            return bench;
        }

        /** Swap this card with a bench card (for RetreatEffect). */
        public void swapWithBench(Card benchCard) {
            int idx = bench.indexOf(benchCard);
            if (idx >= 0) {
                bench.set(idx, this);
                // wherever you track the “active” card,
                // replace it with benchCard instead.
                // This is just the placeholder:
                // (you’ll need to wire it up in your battle logic)
            }
        }

    }

    /** JSON‐binding helper classes for cards.json */
    public static class CardData {
        public String name;
        public int    health;
        public String type;
        public String pantheon;
        public String imagePath;
        public List<SkillData> skills;

        public static class SkillData {
            public String name;
            public int    cost;
            public List<EffectData> effects;
        }

        public static class EffectData {
            public String type;
            public int    amount;    // for flat effects
            public int    duration;  // for Poisons/Stuns/etc
            public float  percent;   // for buffs/debuffs
        }
    }

    public static List<Card> loadCardsFromJson() {
        List<Card> cards = new ArrayList<>();
        Json json = new Json();
        Array<CardData> data = json.fromJson(Array.class, CardData.class, Gdx.files.internal("cards.json"));

        for (CardData cd : data) {
            CardType type = CardType.valueOf(cd.type.toUpperCase());
            CardPantheon pantheon = CardPantheon.valueOf(cd.pantheon.toUpperCase());

            List<Skill> skills = new ArrayList<>();
            for (CardData.SkillData sd : cd.skills) {
                List<SkillEffect> effects = new ArrayList<>();
                for (CardData.EffectData ed : sd.effects) {
                    effects.add(EffectFactory.create(ed));
                }
                skills.add(new Skill(sd.name, sd.cost, effects));
            }



            cards.add(new Card(cd.name, cd.health, skills, type, pantheon, cd.imagePath));
        }

        return cards;
    }



    public static List<Card> getAllCards() {
        List<Card> cards = new ArrayList<>();

        return cards;
    }


}


