package com.example.solitaire.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solitaire.model.Card

@Composable
fun CardView(
    card: Card,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(80.dp, 120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (card.isFaceUp) Color.White else Color.Blue)
            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
    ) {
        if (card.isFaceUp) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ) {
                // Top left corner
                Row {
                    Text(
                        text = card.displayValue,
                        color = if (card.color == com.example.solitaire.model.Color.RED) Color.Red else Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = card.displaySuit,
                        color = if (card.color == com.example.solitaire.model.Color.RED) Color.Red else Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Center suit
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = card.displaySuit,
                        color = if (card.color == com.example.solitaire.model.Color.RED) Color.Red else Color.Black,
                        fontSize = 32.sp
                    )
                }
            }
        }
    }
} 