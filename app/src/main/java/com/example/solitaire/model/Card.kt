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
} 