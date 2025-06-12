package com.example.solitaire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.example.solitaire.model.Deck
import com.example.solitaire.ui.components.CardStack
import com.example.solitaire.ui.components.CardView

sealed class Screen {
    object Menu : Screen()
    object FreeCell : Screen()
    object Solitaire : Screen()
}

// Data class to represent a move
data class Move(
    val fromType: MoveType,
    val toType: MoveType,
    val fromIndex: Int,
    val toIndex: Int,
    val card: Card
)

enum class MoveType {
    MAIN_STACK,
    TOP_LEFT,
    TOP_RIGHT
}

data class HighlightedCardLocation(
    val stackType: StackType,
    val column: Int
)

enum class StackType {
    SECOND_ROW,
    TOP_RIGHT,
    TOP_LEFT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF4CAF50) // Medium green
            ) {
                FreeCellGameApp()
            }
        }
    }
}

@Composable
fun FreeCellGameApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Menu) }
    var deck by remember { mutableStateOf(Deck()) }
    var secondRowStacks by remember { mutableStateOf(List(8) { mutableListOf<Card>() }) }
    var topRightStacks by remember { mutableStateOf(List(4) { mutableListOf<Card>() }) }
    var topLeftStacks by remember { mutableStateOf(List(4) { mutableListOf<Card>() }) }
    var highlightedLocation by remember { mutableStateOf<HighlightedCardLocation?>(null) }
    
    // Function to deal cards
    fun dealNewGame() {
        deck = Deck()
        val newStacks = List(8) { mutableListOf<Card>() }
        var currentColumn = 0
        while (true) {
            val card = deck.dealCard() ?: break
            newStacks[currentColumn].add(card)
            currentColumn = (currentColumn + 1) % 8
        }
        secondRowStacks = newStacks
        topRightStacks = List(4) { mutableListOf() }
        topLeftStacks = List(4) { mutableListOf() }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF2E7D32)
    ) {
        when (currentScreen) {
            Screen.Menu -> GameSelectionScreen(
                onFreeCellSelected = {
                    currentScreen = Screen.FreeCell
                    dealNewGame()
                },
                onSolitaireSelected = { currentScreen = Screen.Solitaire }
            )
            Screen.FreeCell -> FreeCellScreen(
                onBackPressed = { currentScreen = Screen.Menu },
                secondRowStacks = secondRowStacks,
                topRightStacks = topRightStacks,
                topLeftStacks = topLeftStacks,
                highlightedLocation = highlightedLocation,
                setHighlightedLocation = { highlightedLocation = it },
                onDealNewGame = { dealNewGame() }
            )
            Screen.Solitaire -> SolitaireScreen(
                onBackPressed = { currentScreen = Screen.Menu }
            )
        }
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
fun FreeCellScreen(
    onBackPressed: () -> Unit,
    secondRowStacks: List<MutableList<Card>>,
    topRightStacks: List<MutableList<Card>>,
    topLeftStacks: List<MutableList<Card>>,
    highlightedLocation: HighlightedCardLocation?,
    setHighlightedLocation: (HighlightedCardLocation?) -> Unit,
    onDealNewGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { setHighlightedLocation(null) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    setHighlightedLocation(null)
                    onBackPressed()
                },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Back to Menu")
            }
            Button(
                onClick = {
                    setHighlightedLocation(null)
                    onDealNewGame()
                }
            ) {
                Text("New Game")
            }
        }
        // First row - two sets of 4 placeholders
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // Top left placeholders
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(end = 32.dp)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .width(35.dp)
                            .height(52.dp)
                            .border(2.dp, Color.Gray)
                            .background(Color.LightGray.copy(alpha = 0.3f))
                            .clickable {
                                if (highlightedLocation != null) {
                                    // A card is already selected, move it to this stack
                                    val selectedStackType = highlightedLocation!!.stackType
                                    val selectedColumn = highlightedLocation!!.column
                                    val selectedCard = when (selectedStackType) {
                                        StackType.SECOND_ROW -> secondRowStacks[selectedColumn].lastOrNull()
                                        StackType.TOP_LEFT -> topLeftStacks[selectedColumn].firstOrNull()
                                        StackType.TOP_RIGHT -> topRightStacks[selectedColumn].lastOrNull()
                                    }
                                    if (selectedCard != null) {
                                        // Remove the card from its original stack
                                        when (selectedStackType) {
                                            StackType.SECOND_ROW -> {
                                                secondRowStacks[selectedColumn].removeAt(secondRowStacks[selectedColumn].lastIndex)
                                            }
                                            StackType.TOP_LEFT -> {
                                                topLeftStacks[selectedColumn].clear()
                                            }
                                            StackType.TOP_RIGHT -> {
                                                topRightStacks[selectedColumn].removeAt(topRightStacks[selectedColumn].lastIndex)
                                            }
                                        }
                                        // Add the card to the new stack
                                        topLeftStacks[index].add(selectedCard)
                                    }
                                    setHighlightedLocation(null)
                                } else {
                                    // No card is selected, select the card of this stack
                                    if (topLeftStacks[index].isEmpty()) {
                                        setHighlightedLocation(null)
                                    } else {
                                        setHighlightedLocation(HighlightedCardLocation(StackType.TOP_LEFT, index))
                                    }
                                }
                            }
                    ) {
                        // Display any cards in this stack
                        topLeftStacks[index].forEachIndexed { cardIndex, card ->
                            CardView(
                                card = card,
                                modifier = Modifier
                                    .width(35.dp)
                                    .height(52.dp)
                            )
                        }
                    }
                }
            }
            // Top right placeholders
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .width(35.dp)
                            .height(52.dp)
                            .border(2.dp, Color.Gray)
                            .background(Color.LightGray.copy(alpha = 0.3f))
                            .clickable {
                                if (highlightedLocation != null) {
                                    // A card is already selected, move it to this stack
                                    val selectedStackType = highlightedLocation!!.stackType
                                    val selectedColumn = highlightedLocation!!.column
                                    val selectedCard = when (selectedStackType) {
                                        StackType.SECOND_ROW -> secondRowStacks[selectedColumn].lastOrNull()
                                        StackType.TOP_LEFT -> topLeftStacks[selectedColumn].firstOrNull()
                                        StackType.TOP_RIGHT -> topRightStacks[selectedColumn].lastOrNull()
                                    }
                                    if (selectedCard != null) {
                                        // Remove the card from its original stack
                                        when (selectedStackType) {
                                            StackType.SECOND_ROW -> {
                                                secondRowStacks[selectedColumn].removeAt(secondRowStacks[selectedColumn].lastIndex)
                                            }
                                            StackType.TOP_LEFT -> {
                                                topLeftStacks[selectedColumn].clear()
                                            }
                                            StackType.TOP_RIGHT -> {
                                                topRightStacks[selectedColumn].removeAt(topRightStacks[selectedColumn].lastIndex)
                                            }
                                        }
                                        // Add the card to the new stack
                                        topRightStacks[index].add(selectedCard)
                                    }
                                    setHighlightedLocation(null)
                                } else {
                                    // No card is selected, select the card of this stack
                                    if (topRightStacks[index].isEmpty()) {
                                        setHighlightedLocation(null)
                                    } else {
                                        setHighlightedLocation(HighlightedCardLocation(StackType.TOP_RIGHT, index))
                                    }
                                }
                            }
                    ) {
                        // Display only the top card of the stack
                        topRightStacks[index].lastOrNull()?.let { card ->
                            CardView(
                                card = card,
                                modifier = Modifier
                                    .width(35.dp)
                                    .height(52.dp)
                            )
                        }
                    }
                }
            }
        }
        // Second row - 8 stacks of cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(8) { stackIndex ->
                val stackSize = secondRowStacks[stackIndex].size
                val stackHeight = if (stackSize > 0) 52.dp + (20.dp * (stackSize - 1)) else 52.dp
                Box(
                    modifier = Modifier
                        .width(35.dp)
                        .height(stackHeight)
                        .clickable {
                            if (highlightedLocation != null) {
                                // A card is already selected, move it to this stack
                                val selectedStackType = highlightedLocation!!.stackType
                                val selectedColumn = highlightedLocation!!.column
                                val selectedCard = when (selectedStackType) {
                                    StackType.SECOND_ROW -> secondRowStacks[selectedColumn].lastOrNull()
                                    StackType.TOP_LEFT -> topLeftStacks[selectedColumn].firstOrNull()
                                    StackType.TOP_RIGHT -> topRightStacks[selectedColumn].lastOrNull()
                                }
                                if (selectedCard != null) {
                                    // Remove the card from its original stack
                                    when (selectedStackType) {
                                        StackType.SECOND_ROW -> {
                                            secondRowStacks[selectedColumn].removeAt(secondRowStacks[selectedColumn].lastIndex)
                                        }
                                        StackType.TOP_LEFT -> {
                                            topLeftStacks[selectedColumn].clear()
                                        }
                                        StackType.TOP_RIGHT -> {
                                            topRightStacks[selectedColumn].removeAt(topRightStacks[selectedColumn].lastIndex)
                                        }
                                    }
                                    // Add the card to the new stack
                                    secondRowStacks[stackIndex].add(selectedCard)
                                }
                                setHighlightedLocation(null)
                            } else {
                                // No card is selected, select the bottom card of this stack
                                if (secondRowStacks[stackIndex].isEmpty()) {
                                    setHighlightedLocation(null)
                                } else {
                                    setHighlightedLocation(HighlightedCardLocation(StackType.SECOND_ROW, stackIndex))
                                }
                            }
                        }
                ) {
                    if (secondRowStacks[stackIndex].isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(2.dp, Color.Gray)
                                .background(Color.LightGray.copy(alpha = 0.3f))
                        ) {}
                    } else {
                        secondRowStacks[stackIndex].forEachIndexed { cardIndex, card ->
                            val isBottomCard = cardIndex == secondRowStacks[stackIndex].lastIndex
                            CardView(
                                card = card,
                                modifier = Modifier
                                    .width(35.dp)
                                    .height(52.dp)
                                    .offset(y = (cardIndex * 20).dp),
                                isSelected = isBottomCard && highlightedLocation?.stackType == StackType.SECOND_ROW && highlightedLocation?.column == stackIndex
                            )
                        }
                    }
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