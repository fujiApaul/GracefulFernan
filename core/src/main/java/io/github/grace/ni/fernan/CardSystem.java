package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import io.github.grace.ni.fernan.SkillEffect;
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

        public Skill() {
        }

        public Skill(String name, int cost, List<SkillEffect> effects) {
            this.name    = name;
            this.cost    = cost;
            this.effects = effects;
        }

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

        // ADD THIS GETTER METHOD
        public List<SkillEffect> getEffects() { return effects; }

        public void apply(Card user, Card target) {
            if (this.effects != null) {
                for (SkillEffect e : this.effects) {
                    if (e != null) {
                        e.apply(user, target);
                    }
                }
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

        public void consume() {
            graveyard.add(this);
            hand.remove(this);
        }

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

        public String getUniqueId() {
            return name + "_" + (pantheon != null ? pantheon.name() : "NONE") + "_" + (type != null ? type.name() : "UNKNOWN");
        }

        /**
         * Copy constructor to create a new instance from an existing card.
         * Ensures deep copy of mutable fields like skills list.
         */
        public Card(Card other) {
            this.name = other.name;
            this.health = other.health;
            this.skills = new ArrayList<>();
            if (other.skills != null) {
                for (Skill originalSkill : other.skills) {
                    List<SkillEffect> newEffects = new ArrayList<>();
                    // Check if originalSkill.getEffects() is not null before using it
                    if (originalSkill.getEffects() != null) {
                        // Assuming SkillEffect instances are shareable/stateless or re-created by EffectFactory
                        // For a true deep copy where SkillEffect instances are also new,
                        // you'd need to recreate them, possibly using original EffectData if available.
                        // This current approach shares SkillEffect instances if they are directly copied.
                        newEffects.addAll(originalSkill.getEffects());
                    }
                    this.skills.add(new Skill(originalSkill.getName(), originalSkill.getCost(), newEffects));
                }
            }
            this.type = other.type;
            this.pantheon = other.pantheon;
            this.imagePath = other.imagePath;
            // statuses, usedOnceThisBattle, usedThisTurn are battle-specific and start fresh for the new instance
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

        public Card() {
        }


        public void addStatus(io.github.grace.ni.fernan.status.Status s) {
            statuses.add(s);
        }

        public void applyStartOfTurnStatuses() {
            Iterator<Status> it = statuses.iterator();
            while (it.hasNext()) {
                Status st = it.next();
                st.onTurnStart(this);
                if (st.isExpired()) it.remove();
            }
        }

        public void applyEndOfTurnStatuses() {
            Iterator<Status> it = statuses.iterator();
            while (it.hasNext()) {
                Status st = it.next();
                st.onTurnEnd(this);
                if (st.isExpired()) it.remove();
            }
        }
        public void takeDamage(int amount) {
            this.health = Math.max(0, this.health - amount);
        }
        public void heal(int amount) {
            this.health += amount;
        }

        private final List<Card> bench = new ArrayList<>();
        private final List<Card> hand = new ArrayList<>();
        private final List<Card> graveyard = new ArrayList<>();

        public Card drawFromGraveyard() {
            if (graveyard.isEmpty()) return null;
            return graveyard.remove((int)(Math.random() * graveyard.size()));
        }
        public void addToHand(Card c) {
            if (c != null) {
                hand.add(c);
            }
        }
        public List<Card> getBench() {
            return bench;
        }
        public void swapWithBench(Card benchCard) {
            int idx = bench.indexOf(benchCard);
            if (idx >= 0) {
                bench.set(idx, this);
            }
        }
    }

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
            public int    amount;
            public int    duration;
            public float  percent;
        }
    }

    public static List<Card> loadCardsFromJson() {
        List<Card> cards = new ArrayList<>();
        Json json = new Json();
        json.setIgnoreUnknownFields(true); // Good practice for robustness
        Array<CardData> dataArray = json.fromJson(Array.class, CardData.class, Gdx.files.internal("cards.json"));

        if (dataArray == null) { // Check if dataArray itself is null
            Gdx.app.error("CardSystem", "Failed to load or parse cards.json. dataArray is null.");
            return cards; // Return empty list
        }

        for (CardData cd : dataArray) {
            if (cd == null) continue; // Skip if a card data entry is null

            CardType type = null;
            try {
                if (cd.type != null) type = CardType.valueOf(cd.type.toUpperCase());
            } catch (IllegalArgumentException e) {
                Gdx.app.error("CardSystem", "Invalid CardType in cards.json: " + cd.type + " for card " + cd.name);
                continue; // Skip this card
            }

            CardPantheon pantheon = null;
            try {
                if (cd.pantheon != null) pantheon = CardPantheon.valueOf(cd.pantheon.toUpperCase());
                else pantheon = CardPantheon.NONE; // Default if null
            } catch (IllegalArgumentException e) {
                Gdx.app.error("CardSystem", "Invalid CardPantheon in cards.json: " + cd.pantheon + " for card " + cd.name);
                pantheon = CardPantheon.NONE; // Default to NONE or skip
            }


            List<Skill> skills = new ArrayList<>();
            if (cd.skills != null) {
                for (CardData.SkillData sd : cd.skills) {
                    if (sd == null) continue;
                    List<SkillEffect> effects = new ArrayList<>();
                    if (sd.effects != null) {
                        for (CardData.EffectData ed : sd.effects) {
                            if (ed == null) continue;
                            try {
                                effects.add(EffectFactory.create(ed));
                            } catch (Exception e) {
                                Gdx.app.error("CardSystem", "Failed to create effect for card " + cd.name + ", skill " + sd.name + ", effect type " + ed.type + ": " + e.getMessage());
                            }
                        }
                    }
                    skills.add(new Skill(sd.name, sd.cost, effects));
                }
            }
            cards.add(new Card(cd.name, cd.health, skills, type, pantheon, cd.imagePath));
        }
        return cards;
    }

    public static List<Card> getAllCards() {
        return loadCardsFromJson();
    }
}
