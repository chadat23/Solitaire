package com.example.solitaire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solitaire.model.Card
import com.example.solitaire.model.Suit
import com.example.solitaire.ui.components.CardStack
import com.example.solitaire.ui.components.CardView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF4CAF50) // Medium green
            ) {
                GameApp()
            }
        }
    }
}

@Composable
fun GameApp() {
    var currentScreen by remember { mutableStateOf("menu") }
    
    when (currentScreen) {
        "menu" -> GameSelectionScreen(
            onFreeCellSelected = { currentScreen = "freecell" },
            onSolitaireSelected = { currentScreen = "solitaire" }
        )
        "freecell" -> FreeCellScreen(
            onBackPressed = { currentScreen = "menu" }
        )
        "solitaire" -> SolitaireScreen(
            onBackPressed = { currentScreen = "menu" }
        )
    }
}

@Composable
fun GameSelectionScreen(
    onFreeCellSelected: () -> Unit,
    onSolitaireSelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Select Game",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Button(
            onClick = onFreeCellSelected,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("FreeCell")
        }
        
        Button(
            onClick = onSolitaireSelected,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Solitaire")
        }
    }
}

@Composable
fun FreeCellScreen(onBackPressed: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = onBackPressed,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Back to Menu")
        }
        
        // Two sets of card placeholder spots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // First set of 4 cards
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(end = 32.dp)
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .width(35.dp)
                            .height(52.dp)
                            .border(2.dp, Color.Gray)
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    )
                }
            }
            
            // Second set of 4 cards
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .width(35.dp)
                            .height(52.dp)
                            .border(2.dp, Color.Gray)
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

@Composable
fun SolitaireScreen(onBackPressed: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = onBackPressed,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Back to Menu")
        }
        
        Text("Solitaire Game Coming Soon")
    }
} 