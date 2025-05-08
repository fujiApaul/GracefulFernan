package io.github.grace.ni.fernan;

public class PlayerCardInstance {
    private final CardSystem.Card card;
    private int ownedCopies;

    public PlayerCardInstance(CardSystem.Card card, int ownedCopies) {
        this.card = card;
        this.ownedCopies = ownedCopies;
    }

    public CardSystem.Card getCard() {
        return card;
    }

    public int getOwnedCopies() {
        return ownedCopies;
    }

    public void addCopy() {
        ownedCopies++;
    }

    public boolean isOwned() {
        return ownedCopies > 0;
    }
}
