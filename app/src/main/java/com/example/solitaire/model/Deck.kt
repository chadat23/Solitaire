package com.example.solitaire.model

class Deck {
    private val cards = mutableListOf<Card>()
    private var currentIndex = 0

    init {
        // Create a standard deck of 52 cards
        for (suit in Suit.values()) {
            for (value in 1..13) {
                cards.add(Card(suit, value, true))
            }
        }
        shuffle()
    }

    fun shuffle() {
        cards.shuffle()
        currentIndex = 0
    }

    fun dealCard(): Card? {
        return if (currentIndex < cards.size) {
            cards[currentIndex++]
        } else {
            null
        }
    }

    fun remainingCards(): Int {
        return cards.size - currentIndex
    }

    fun reset() {
        currentIndex = 0
    }
} 