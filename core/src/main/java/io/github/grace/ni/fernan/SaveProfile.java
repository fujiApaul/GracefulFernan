package io.github.grace.ni.fernan;

import com.badlogic.gdx.Gdx; // Added for potential logging
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.math.MathUtils; // For random selection

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SaveProfile {
    public String saveName;
    public String currentMapId;
    public int gachaCurrency;

    // Pack counts
    public int godDivinePacks;
    public int divinePacks;
    public int artifactItemPacks;

    public List<DeckSelectionScreen.Deck> decks = new ArrayList<>();
    public List<String> ownedCardIds = new ArrayList<>(); // Will store unique IDs

    public SaveProfile() {}

    public SaveProfile(String saveName) {
        this.saveName = saveName;
        this.currentMapId = "n0";
        this.gachaCurrency = 100; // Starting coins

        this.godDivinePacks = 0;
        this.divinePacks = 0;
        this.artifactItemPacks = 0;

        List<CardSystem.Card> allCardsMasterList = CardSystem.loadCardsFromJson();
        List<CardSystem.Card> godCards = allCardsMasterList.stream()
            .filter(c -> c.getType() == CardSystem.CardType.GOD)
            .collect(Collectors.toList());

        CardSystem.Card starterCard = null;
        if (!godCards.isEmpty()) {
            starterCard = godCards.get(MathUtils.random(godCards.size() - 1));
        } else {
            // Fallback: if no GOD cards, pick a random DIVINE or any card
            Gdx.app.log("SaveProfile", "Warning: No GOD type cards found for starter deck. Trying DIVINE.");
            List<CardSystem.Card> divineCards = allCardsMasterList.stream()
                .filter(c -> c.getType() == CardSystem.CardType.DIVINE)
                .collect(Collectors.toList());
            if (!divineCards.isEmpty()) {
                starterCard = divineCards.get(MathUtils.random(divineCards.size() - 1));
            } else if (!allCardsMasterList.isEmpty()) {
                Gdx.app.log("SaveProfile", "Warning: No DIVINE type cards found. Picking a random card for starter deck.");
                starterCard = allCardsMasterList.get(MathUtils.random(allCardsMasterList.size() - 1));
            } else {
                Gdx.app.error("SaveProfile", "CRITICAL: No cards available in cards.json to create a starter deck.");
                // Handle this critical error, maybe by creating a dummy card or throwing an exception
            }
        }

        if (starterCard != null) {
            DeckSelectionScreen.Deck starterDeck = new DeckSelectionScreen.Deck(
                "Starter Deck",
                new ArrayList<>(Collections.singletonList(starterCard)) // Deck with one card
            );
            this.decks.add(starterDeck);
            this.addToCollection(starterCard); // Add its unique ID to owned cards
        } else {
            // If still no starter card (critical error), create an empty starter deck
            this.decks.add(new DeckSelectionScreen.Deck("Starter Deck", new ArrayList<>()));
            Gdx.app.log("SaveProfile", "Created an empty starter deck due to lack of suitable cards.");
        }
    }

    public String toJson() {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        return json.toJson(this);
    }

    public static SaveProfile fromJson(String jsonString) {
        return new Json().fromJson(SaveProfile.class, jsonString);
    }

    public List<CardSystem.Card> getOwnedCards() {
        List<CardSystem.Card> master = CardSystem.loadCardsFromJson();
        return master.stream()
            .filter(c -> ownedCardIds.contains(c.getUniqueId())) // Filter by unique ID
            .collect(Collectors.toList());
    }

    public void addToCollection(CardSystem.Card card) {
        if (card == null) return;
        String uniqueId = card.getUniqueId(); // Use the new unique ID method
        if (!ownedCardIds.contains(uniqueId)) {
            ownedCardIds.add(uniqueId);
        }
    }

    // Methods to manage pack counts
    public void addGodDivinePack(int count) {
        this.godDivinePacks += count;
    }
    public void addDivinePack(int count) {
        this.divinePacks += count;
    }
    public void addArtifactItemPack(int count) {
        this.artifactItemPacks += count;
    }

    public boolean useGodDivinePack() {
        if (this.godDivinePacks > 0) {
            this.godDivinePacks--;
            return true;
        }
        return false;
    }
    public boolean useDivinePack() {
        if (this.divinePacks > 0) {
            this.divinePacks--;
            return true;
        }
        return false;
    }
    public boolean useArtifactItemPack() {
        if (this.artifactItemPacks > 0) {
            this.artifactItemPacks--;
            return true;
        }
        return false;
    }
}
