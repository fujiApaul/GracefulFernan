package io.github.grace.ni.fernan;

import java.util.ArrayList;
import java.util.List;

public class PlayerCollection {
    private final List<PlayerCardInstance> allCardInstances = new ArrayList<>();

    public PlayerCollection(List<CardSystem.Card> allGameCards) {
        for (CardSystem.Card card : allGameCards) {
            allCardInstances.add(new PlayerCardInstance(card, 0)); // Start with 0 copies
        }
    }

    // Add a copy of a card to player's owned collection
    public void addCard(CardSystem.Card card) {
        for (PlayerCardInstance pci : allCardInstances) {
            if (pci.getCard().getName().equals(card.getName())) {
                pci.addCopy();
                return;
            }
        }

        // If card isn't in the list (shouldn't happen unless card wasn't loaded), add it
        allCardInstances.add(new PlayerCardInstance(card, 1));
    }

    // Get only owned cards
    public List<PlayerCardInstance> getOwnedCards() {
        List<PlayerCardInstance> owned = new ArrayList<>();
        for (PlayerCardInstance pci : allCardInstances) {
            if (pci.isOwned()) {
                owned.add(pci);
            }
        }
        return owned;
    }

    // Get unowned cards (could be used for shops or discovery tracking)
    public List<PlayerCardInstance> getUnownedCards() {
        List<PlayerCardInstance> unowned = new ArrayList<>();
        for (PlayerCardInstance pci : allCardInstances) {
            if (!pci.isOwned()) {
                unowned.add(pci);
            }
        }
        return unowned;
    }

    // Optional: Get full list of cards and owned copy counts
    public List<PlayerCardInstance> getAllCardInstances() {
        return allCardInstances;
    }
}
