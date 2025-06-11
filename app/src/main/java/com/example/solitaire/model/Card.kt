package com.example.solitaire.model

enum class Suit {
    HEARTS, DIAMONDS, CLUBS, SPADES
}

enum class Color {
    RED, BLACK
}

data class Card(
    val suit: Suit,
    val value: Int, // 1-13 (Ace through King)
    val isFaceUp: Boolean = false
) {
    val color: Color
        get() = when (suit) {
            Suit.HEARTS, Suit.DIAMONDS -> Color.RED
            Suit.CLUBS, Suit.SPADES -> Color.BLACK
        }

    val displayValue: String
        get() = when (value) {
            1 -> "A"
            11 -> "J"
            12 -> "Q"
            13 -> "K"
            else -> value.toString()
        }

    val displaySuit: String
        get() = when (suit) {
            Suit.HEARTS -> "♥"
            Suit.DIAMONDS -> "♦"
            Suit.CLUBS -> "♣"
            Suit.SPADES -> "♠"
        }

    /**
     * Checks if this card and all cards below it in a stack follow the alternating color
     * and decreasing value pattern required for moving cards.
     * 
     * @param stack The list of cards in the stack, with the top card at index 0
     * @param cardIndex The index of this card in the stack
     * @return true if this card and all cards below it can be moved together
     */
    fun isMovableWithStack(stack: List<Card>, cardIndex: Int): Boolean {
        // Can't move face-down cards
        if (!isFaceUp) return false
        
        // Check each pair of adjacent cards in the sequence
        for (i in cardIndex until stack.size - 1) {
            val currentCard = stack[i]
            val nextCard = stack[i + 1]
            
            // Check if colors alternate
            if (currentCard.color == nextCard.color) return false
            
            // Check if values decrease by 1
            if (currentCard.value != nextCard.value + 1) return false
        }
        
        return true
    }
} 