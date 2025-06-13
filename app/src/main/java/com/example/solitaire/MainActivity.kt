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
    var moveHistory by remember { mutableStateOf(mutableListOf<Move>()) }
    
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
    
    fun undoLastMove() {
        if (moveHistory.isNotEmpty()) {
            val lastMove = moveHistory.removeAt(moveHistory.lastIndex)
            when (lastMove.toType) {
                MoveType.TOP_LEFT -> topLeftStacks[lastMove.toIndex].removeAt(topLeftStacks[lastMove.toIndex].lastIndex)
                MoveType.TOP_RIGHT -> topRightStacks[lastMove.toIndex].removeAt(topRightStacks[lastMove.toIndex].lastIndex)
                MoveType.MAIN_STACK -> secondRowStacks[lastMove.toIndex].removeAt(secondRowStacks[lastMove.toIndex].lastIndex)
            }
            when (lastMove.fromType) {
                MoveType.TOP_LEFT -> topLeftStacks[lastMove.fromIndex].add(lastMove.card)
                MoveType.TOP_RIGHT -> topRightStacks[lastMove.fromIndex].add(lastMove.card)
                MoveType.MAIN_STACK -> secondRowStacks[lastMove.fromIndex].add(lastMove.card)
            }
        }
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
                onDealNewGame = { dealNewGame() },
                undoLastMove = { undoLastMove() },
                moveHistory = moveHistory
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
    onDealNewGame: () -> Unit,
    undoLastMove: () -> Unit,
    moveHistory: MutableList<Move>
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
                                    val selectedStackType = highlightedLocation!!.stackType
                                    val selectedColumn = highlightedLocation!!.column
                                    val selectedCard = when (selectedStackType) {
                                        StackType.SECOND_ROW -> secondRowStacks[selectedColumn].lastOrNull()
                                        StackType.TOP_LEFT -> topLeftStacks[selectedColumn].firstOrNull()
                                        StackType.TOP_RIGHT -> topRightStacks[selectedColumn].lastOrNull()
                                    }
                                    if (selectedCard != null) {
                                        val destinationCards = topLeftStacks[index]
                                        if (isValidMove(selectedCard, StackType.TOP_LEFT, index, destinationCards, topLeftStacks)) {
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
                                            topLeftStacks[index].add(selectedCard)
                                            // Record the move
                                            moveHistory.add(Move(
                                                fromType = when (selectedStackType) {
                                                    StackType.SECOND_ROW -> MoveType.MAIN_STACK
                                                    StackType.TOP_LEFT -> MoveType.TOP_LEFT
                                                    StackType.TOP_RIGHT -> MoveType.TOP_RIGHT
                                                },
                                                toType = MoveType.TOP_LEFT,
                                                fromIndex = selectedColumn,
                                                toIndex = index,
                                                card = selectedCard
                                            ))
                                        }
                                    }
                                    setHighlightedLocation(null)
                                } else {
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
                                    .height(52.dp),
                                isSelected = cardIndex == topLeftStacks[index].lastIndex && highlightedLocation?.stackType == StackType.TOP_LEFT && highlightedLocation?.column == index
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
                                    val selectedStackType = highlightedLocation!!.stackType
                                    val selectedColumn = highlightedLocation!!.column
                                    val selectedCard = when (selectedStackType) {
                                        StackType.SECOND_ROW -> secondRowStacks[selectedColumn].lastOrNull()
                                        StackType.TOP_LEFT -> topLeftStacks[selectedColumn].firstOrNull()
                                        StackType.TOP_RIGHT -> topRightStacks[selectedColumn].lastOrNull()
                                    }
                                    if (selectedCard != null) {
                                        val destinationCards = topRightStacks[index]
                                        if (isValidMove(selectedCard, StackType.TOP_RIGHT, index, destinationCards, topLeftStacks)) {
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
                                            topRightStacks[index].add(selectedCard)
                                            // Record the move
                                            moveHistory.add(Move(
                                                fromType = when (selectedStackType) {
                                                    StackType.SECOND_ROW -> MoveType.MAIN_STACK
                                                    StackType.TOP_LEFT -> MoveType.TOP_LEFT
                                                    StackType.TOP_RIGHT -> MoveType.TOP_RIGHT
                                                },
                                                toType = MoveType.TOP_RIGHT,
                                                fromIndex = selectedColumn,
                                                toIndex = index,
                                                card = selectedCard
                                            ))
                                        }
                                    }
                                    setHighlightedLocation(null)
                                } else {
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
                                    .height(52.dp),
                                isSelected = highlightedLocation?.stackType == StackType.TOP_RIGHT && highlightedLocation?.column == index
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
                                val selectedStackType = highlightedLocation!!.stackType
                                val selectedColumn = highlightedLocation!!.column
                                val selectedCard = when (selectedStackType) {
                                    StackType.SECOND_ROW -> secondRowStacks[selectedColumn].lastOrNull()
                                    StackType.TOP_LEFT -> topLeftStacks[selectedColumn].firstOrNull()
                                    StackType.TOP_RIGHT -> topRightStacks[selectedColumn].lastOrNull()
                                }
                                if (selectedCard != null) {
                                    val destinationCards = secondRowStacks[stackIndex]
                                    if (isValidMove(selectedCard, StackType.SECOND_ROW, stackIndex, destinationCards, topLeftStacks)) {
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
                                        secondRowStacks[stackIndex].add(selectedCard)
                                        // Record the move
                                        moveHistory.add(Move(
                                            fromType = when (selectedStackType) {
                                                StackType.SECOND_ROW -> MoveType.MAIN_STACK
                                                StackType.TOP_LEFT -> MoveType.TOP_LEFT
                                                StackType.TOP_RIGHT -> MoveType.TOP_RIGHT
                                            },
                                            toType = MoveType.MAIN_STACK,
                                            fromIndex = selectedColumn,
                                            toIndex = stackIndex,
                                            card = selectedCard
                                        ))
                                    }
                                }
                                setHighlightedLocation(null)
                            } else {
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
        // Undo button at the bottom
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = { undoLastMove() },
                modifier = Modifier
                    .width(100.dp)
                    .padding(bottom = 16.dp)
            ) {
                Text("Undo")
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

fun isValidMove(card: Card, destinationStackType: StackType, destinationIndex: Int, destinationCards: MutableList<Card>, topLeftStacks: List<MutableList<Card>>): Boolean {
    return when (destinationStackType) {
        StackType.TOP_LEFT -> destinationCards.isEmpty()
        StackType.TOP_RIGHT -> {
            val destinationCard = destinationCards.lastOrNull()
            if (destinationCard == null) {
                card.value == 1
            } else {
                card.suit == destinationCard.suit && card.value == destinationCard.value + 1
            }
        }
        StackType.SECOND_ROW -> {
            val destinationCard = destinationCards.lastOrNull()
            if (destinationCard == null) {
                true
            } else if (card.suit != destinationCard.suit && card.value == destinationCard.value - 1) {
                true
            } else {
                var nullCount = 0
                topLeftStacks.forEach { stack -> 
                    if (stack.isEmpty()) {
                        nullCount += 1
                    }
                }

                var count = destinationCards.size - 1
                var canMove = true
                while (count > 0) {
                    var lowerCard = destinationCards[count]
                    var upperCard = destinationCards[count - 1]
                    if (lowerCard.suit != upperCard.suit && lowerCard.value == upperCard.value - 1) {
                        // the correct card is preceded by one that follows stacking rules
                        if (upperCard.suit != card.suit && upperCard.value == card.value - 1) {
                            // this card can go on the destination
                            break
                        } else {
                            count -= 1
                        }
                    } else {
                        canMove = false
                        break
                    }
                }
                canMove
            }
        }
    }
} 