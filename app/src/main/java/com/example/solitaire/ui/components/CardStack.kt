package com.example.solitaire.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.solitaire.model.Card

@Composable
fun CardStack(
    cards: List<Card>,
    modifier: Modifier = Modifier,
    cardOffset: Int = 20
) {
    Box(modifier = modifier) {
        cards.forEachIndexed { index, card ->
            CardView(
                card = card,
                modifier = Modifier
                    .offset(y = (index * cardOffset).dp)
                    .padding(4.dp)
            )
        }
    }
} 