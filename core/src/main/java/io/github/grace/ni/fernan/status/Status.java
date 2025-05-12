package io.github.grace.ni.fernan.status;

import io.github.grace.ni.fernan.CardSystem.Card;

/** A reusable effect on a card that persists across turns. */
public interface Status {
    /** Called at the start of the card’s turn. */
    void onTurnStart(Card owner);
    /** Called at the end of the card’s turn. */
    void onTurnEnd(Card owner);
    /** Whether this status is done and should be removed. */
    boolean isExpired();

    public default boolean isNegative() {
        return false;
    }
}
