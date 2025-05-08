package io.github.grace.ni.fernan;

import java.util.List;

public class CardData {
    public String name;
    public int health;
    public String type;
    public String pantheon;
    public String imagePath;
    public List<SkillData> skills;

    public static class SkillData {
        public String name;
        public int cost;
        public String effectType;  // "DAMAGE" or "HEAL"
        public int amount;
    }
}
