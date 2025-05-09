package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import io.github.grace.ni.fernan.SkillEffect;

public class CardSystem {

    public enum CardType {
        DIVINE, GOD, ITEM, ARTIFACT
    }

    public enum CardPantheon {
        GREEK, ROMAN, NONE
    }


    public static class Skill {
        public String name;
        public int cost;
        public SkillEffect effect;

        public Skill(String name, int cost) {
            this.name = name;
            this.cost = cost;
            this.effect = null;
        }

        public Skill(String name, int cost, SkillEffect effect) {
            this.name = name;
            this.cost = cost;
            this.effect = effect;
        }

        public void apply(Card user, Card target) {
            if (effect != null) {
                effect.apply(user, target);
            }
        }

        public String getName() { return name; }
        public int getCost() { return cost; }
    }


    public static class Card {
        private final String name;
        int health;
        private final List<Skill> skills;
        private final CardType type;
        private final CardPantheon pantheon;
        private final String imagePath;

        public Card(String name, int health, List<Skill> skills, CardType type, CardPantheon pantheon, String imagePath) {
            this.name = name;
            this.health = health;
            this.skills = skills;
            this.type = type;
            this.pantheon = pantheon;
            this.imagePath = imagePath;
        }

        public String getImagePath() {
            return imagePath;
        }

        public String getName() { return name; }
        public int getHealth() { return health; }
        public List<Skill> getSkills() { return skills; }
        public CardType getType() { return type; }
        public CardPantheon getPantheon() { return pantheon; }
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
                SkillEffect effect = EffectFactory.createEffect(sd.effectType, sd.amount);
                skills.add(new Skill(sd.name, sd.cost, effect));
            }


            cards.add(new Card(cd.name, cd.health, skills, type, pantheon, cd.imagePath));
        }

        return cards;
    }



    public static List<Card> getAllCards() {
        List<Card> cards = new ArrayList<>();

        cards.add(new Card(
            "Zeus",
            100,
            Arrays.asList(new Skill("Thunderbolt", 5)),
            CardType.GOD,
            CardPantheon.GREEK,
            "cards/zeus.png"
        ));


        return cards;
    }


}


