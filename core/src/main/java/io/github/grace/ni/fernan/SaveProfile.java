package io.github.grace.ni.fernan;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import io.github.grace.ni.fernan.CardSystem;
import io.github.grace.ni.fernan.DeckSelectionScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SaveProfile {
    public String saveName;
    public String currentMapId;
    public int    gachaCurrency;

    /** The user’s decks (max 3) */
    public List<DeckSelectionScreen.Deck> decks = new ArrayList<>();

    /**
     * We store each owned card by its unique ID (e.g. name).
     * When you need the actual Card objects, call getOwnedCards().
     */
    public List<String> ownedCardIds = new ArrayList<>();

    // No-arg constructor for JSON
    public SaveProfile() {}

    // New-game constructor
    public SaveProfile(String saveName) {
        this.saveName      = saveName;
        this.currentMapId  = "n0";
        this.gachaCurrency = 0;

        // 1) Starter DECK of 5 cards:
        List<CardSystem.Card> all = CardSystem.loadCardsFromJson();
        List<CardSystem.Card> starterCards = all.subList(0, Math.min(5, all.size()));
        DeckSelectionScreen.Deck starter = new DeckSelectionScreen.Deck(
            "Starter Deck",
            new ArrayList<>(starterCards)
        );
        this.decks.add(starter);

        // 2) Starter COLLECTION = exactly those same 5 cards:
        for (CardSystem.Card c : starterCards) {
            ownedCardIds.add(c.getName());
        }
    }

    /** Serialize this profile to JSON string */
    public String toJson() {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        return json.toJson(this);
    }

    /** Deserialize from JSON */
    public static SaveProfile fromJson(String jsonString) {
        return new Json().fromJson(SaveProfile.class, jsonString);
    }

    /**
     * Returns the actual Card objects this player owns,
     * by matching IDs in ownedCardIds against the master list.
     */
    public List<CardSystem.Card> getOwnedCards() {
        List<CardSystem.Card> master = CardSystem.loadCardsFromJson();
        return master.stream()
            .filter(c -> ownedCardIds.contains(c.getName()))
            .collect(Collectors.toList());
    }

    /**
     * Adds a card to the player's collection (if not already owned).
     */
    public void addToCollection(CardSystem.Card card) {
        String id = card.getName();
        if (!ownedCardIds.contains(id)) {
            ownedCardIds.add(id);
        }
    }
}
