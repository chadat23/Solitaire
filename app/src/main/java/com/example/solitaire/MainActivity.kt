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
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Menu) }
    var deck by remember { mutableStateOf(Deck()) }
    var secondRowStacks by remember { mutableStateOf(List(8) { mutableListOf<Card>() }) }
    var topRightStacks by remember { mutableStateOf(List(4) { mutableListOf<Card>() }) }
    var topLeftStacks by remember { mutableStateOf(List(4) { mutableListOf<Card>() }) }
    var selectedCardIndex by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedTopLeftIndex by remember { mutableStateOf<Int?>(null) }
    var moveHistory by remember { mutableStateOf<List<Move>>(emptyList()) }
    
    // Function to deal cards
    fun dealNewGame() {
        // Create a new deck
        deck = Deck()
        
        // Create new mutable lists for each stack
        val newStacks = List(8) { mutableListOf<Card>() }
        
        // Deal initial two cards to each stack
        repeat(2) {
            for (i in 0..7) {
                deck.dealCard()?.let { card ->
                    newStacks[i].add(card)
                }
            }
        }
        
        // Deal remaining cards
        var currentColumn = 0
        while (true) {
            val card = deck.dealCard() ?: break
            newStacks[currentColumn].add(card)
            currentColumn = (currentColumn + 1) % 8
        }
        
        // Update the state with the new stacks
        secondRowStacks = newStacks
        topRightStacks = List(4) { mutableListOf() }
        topLeftStacks = List(4) { mutableListOf() }
        selectedCardIndex = null
        selectedTopLeftIndex = null
        moveHistory = emptyList()
    }
    
    // Function to undo the last move
    fun undoLastMove() {
        val lastMove = moveHistory.lastOrNull() ?: return
        
        when (lastMove.toType) {
            MoveType.MAIN_STACK -> {
                val newStacks = secondRowStacks.toMutableList()
                newStacks[lastMove.toIndex] = newStacks[lastMove.toIndex].toMutableList().apply {
                    removeAt(size - 1)
                }
                secondRowStacks = newStacks
            }
            MoveType.TOP_LEFT -> {
                val newTopLeftStacks = topLeftStacks.toMutableList()
                newTopLeftStacks[lastMove.toIndex] = mutableListOf()
                topLeftStacks = newTopLeftStacks
            }
            MoveType.TOP_RIGHT -> {
                val newTopRightStacks = topRightStacks.toMutableList()
                newTopRightStacks[lastMove.toIndex] = mutableListOf()
                topRightStacks = newTopRightStacks
            }
        }
        
        when (lastMove.fromType) {
            MoveType.MAIN_STACK -> {
                val newStacks = secondRowStacks.toMutableList()
                newStacks[lastMove.fromIndex] = newStacks[lastMove.fromIndex].toMutableList().apply {
                    add(lastMove.card)
                }
                secondRowStacks = newStacks
            }
            MoveType.TOP_LEFT -> {
                val newTopLeftStacks = topLeftStacks.toMutableList()
                newTopLeftStacks[lastMove.fromIndex] = mutableListOf(lastMove.card)
                topLeftStacks = newTopLeftStacks
            }
            MoveType.TOP_RIGHT -> {
                val newTopRightStacks = topRightStacks.toMutableList()
                newTopRightStacks[lastMove.fromIndex] = mutableListOf(lastMove.card)
                topRightStacks = newTopRightStacks
            }
        }
        
        moveHistory = moveHistory.dropLast(1)
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF2E7D32) // Medium green
    ) {
        when (currentScreen) {
            Screen.Menu -> GameSelectionScreen(
                onFreeCellSelected = {
                    currentScreen = Screen.FreeCell
                    dealNewGame() // Deal a new game when switching to FreeCell
                },
                onSolitaireSelected = { currentScreen = Screen.Solitaire }
            )
            Screen.FreeCell -> FreeCellScreen(
                onBackPressed = { currentScreen = Screen.Menu },
                deck = deck,
                secondRowStacks = secondRowStacks,
                topRightStacks = topRightStacks,
                topLeftStacks = topLeftStacks,
                selectedCardIndex = selectedCardIndex,
                selectedTopLeftIndex = selectedTopLeftIndex,
                moveHistory = moveHistory,
                onDealNewGame = { dealNewGame() },
                onUndoMove = { undoLastMove() },
                onSecondRowStacksChanged = { secondRowStacks = it },
                onTopRightStacksChanged = { topRightStacks = it },
                onTopLeftStacksChanged = { topLeftStacks = it },
                onSelectedCardIndexChanged = { selectedCardIndex = it },
                onSelectedTopLeftIndexChanged = { selectedTopLeftIndex = it },
                onMoveHistoryChanged = { moveHistory = it }
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
    deck: Deck,
    secondRowStacks: List<MutableList<Card>>,
    topRightStacks: List<MutableList<Card>>,
    topLeftStacks: List<MutableList<Card>>,
    selectedCardIndex: Pair<Int, Int>?,
    selectedTopLeftIndex: Int?,
    moveHistory: List<Move>,
    onDealNewGame: () -> Unit,
    onUndoMove: () -> Unit,
    onSecondRowStacksChanged: (List<MutableList<Card>>) -> Unit,
    onTopRightStacksChanged: (List<MutableList<Card>>) -> Unit,
    onTopLeftStacksChanged: (List<MutableList<Card>>) -> Unit,
    onSelectedCardIndexChanged: (Pair<Int, Int>?) -> Unit,
    onSelectedTopLeftIndexChanged: (Int?) -> Unit,
    onMoveHistoryChanged: (List<Move>) -> Unit
) {
    // Helper functions for card movement and validation
    fun isValidMove(card: Card, targetStack: List<Card>): Boolean {
        // Allow moving to empty stacks
        if (targetStack.isEmpty()) {
            return true
        }
        // For non-empty stacks, check alternating colors and decreasing values
        return card.color != targetStack.last().color && 
               card.value == targetStack.last().value - 1
    }
    
    fun isValidTopRightMove(card: Card, targetStack: List<Card>): Boolean {
        return if (targetStack.isEmpty()) {
            println("Stack is empty, checking if card is ace: ${card.value == 1}")
            card.value == 1 // Only aces can start a new stack
        } else {
            val topCard = targetStack.last()
            println("Checking move: Card ${card.value} of ${card.suit} onto ${topCard.value} of ${topCard.suit}")
            println("Same suit: ${card.suit == topCard.suit}")
            println("Next value: ${card.value == topCard.value + 1}")
            card.suit == topCard.suit && // Same suit
            card.value == topCard.value + 1 // Next value up
        }
    }
    
    fun moveCardToStack(
        card: Card,
        fromStackIndex: Int,
        toStackIndex: Int,
        stacks: List<MutableList<Card>>
    ): List<MutableList<Card>> {
        val newStacks = stacks.toMutableList()
        newStacks[fromStackIndex] = newStacks[fromStackIndex].toMutableList().apply {
            removeAt(size - 1) // Remove the last card
        }
        newStacks[toStackIndex] = newStacks[toStackIndex].toMutableList().apply {
            add(card)
        }
        return newStacks
    }
    
    fun moveCardToTopLeft(
        card: Card,
        fromStackIndex: Int,
        toTopLeftIndex: Int
    ) {
        // Remove from main stack
        val newStacks = secondRowStacks.toMutableList()
        newStacks[fromStackIndex] = newStacks[fromStackIndex].toMutableList().apply {
            removeAt(size - 1)
        }
        onSecondRowStacksChanged(newStacks)
        
        // Add to top left stack
        val newTopLeftStacks = topLeftStacks.toMutableList()
        newTopLeftStacks[toTopLeftIndex] = mutableListOf(card)
        onTopLeftStacksChanged(newTopLeftStacks)
        
        // Record the move
        onMoveHistoryChanged(moveHistory + Move(
            MoveType.MAIN_STACK,
            MoveType.TOP_LEFT,
            fromStackIndex,
            toTopLeftIndex,
            card
        ))
    }
    
    fun moveCardToTopRight(
        card: Card,
        fromStackIndex: Int,
        toTopRightIndex: Int
    ) {
        println("Moving card ${card.value} of ${card.suit} to top right stack $toTopRightIndex")
        
        // Remove from main stack
        val newStacks = secondRowStacks.toMutableList()
        newStacks[fromStackIndex] = newStacks[fromStackIndex].toMutableList().apply {
            removeAt(size - 1)
        }
        onSecondRowStacksChanged(newStacks)
        
        // Add to top right stack
        val newTopRightStacks = topRightStacks.toMutableList()
        newTopRightStacks[toTopRightIndex] = newTopRightStacks[toTopRightIndex].toMutableList().apply {
            add(card)
        }
        onTopRightStacksChanged(newTopRightStacks)
        
        // Record the move
        onMoveHistoryChanged(moveHistory + Move(
            MoveType.MAIN_STACK,
            MoveType.TOP_RIGHT,
            fromStackIndex,
            toTopRightIndex,
            card
        ))
    }
    
    fun moveTopLeftCardToStack(
        fromTopLeftIndex: Int,
        toStackIndex: Int
    ) {
        val card = topLeftStacks[fromTopLeftIndex].firstOrNull() ?: return
        
        // Remove from top left stack
        val newTopLeftStacks = topLeftStacks.toMutableList()
        newTopLeftStacks[fromTopLeftIndex] = mutableListOf()
        onTopLeftStacksChanged(newTopLeftStacks)
        
        // Add to main stack
        val newStacks = secondRowStacks.toMutableList()
        newStacks[toStackIndex] = newStacks[toStackIndex].toMutableList().apply {
            add(card)
        }
        onSecondRowStacksChanged(newStacks)
        
        // Record the move
        onMoveHistoryChanged(moveHistory + Move(
            MoveType.TOP_LEFT,
            MoveType.MAIN_STACK,
            fromTopLeftIndex,
            toStackIndex,
            card
        ))
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Clear any selection when clicking the background
                onSelectedCardIndexChanged(null)
                onSelectedTopLeftIndexChanged(null)
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBackPressed,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Back to Menu")
            }
            
            Button(
                onClick = onDealNewGame
            ) {
                Text("New Game")
            }
        }
        
        // First row - two sets of 4 cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // First set of 4 cards (top left stacks)
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
                                // If a bottom card is selected, move it to this stack
                                selectedCardIndex?.let { (stackIndex, cardIndex) ->
                                    val selectedCard = secondRowStacks[stackIndex][cardIndex]
                                    if (cardIndex == secondRowStacks[stackIndex].size - 1) {
                                        moveCardToTopLeft(selectedCard, stackIndex, index)
                                        onSelectedCardIndexChanged(null)
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
                                    .offset(y = (cardIndex * 20).dp)
                                    .clickable {
                                        // If a card is selected from the main stacks, try to move it here
                                        selectedCardIndex?.let { (selectedStackIndex, selectedCardIndex) ->
                                            val selectedCard = secondRowStacks[selectedStackIndex][selectedCardIndex]
                                            if (selectedCardIndex == secondRowStacks[selectedStackIndex].size - 1) {
                                                moveCardToTopLeft(selectedCard, selectedStackIndex, index)
                                                onSelectedCardIndexChanged(null)
                                            }
                                        } ?: run {
                                            // If no card is selected, select this card
                                            onSelectedTopLeftIndexChanged(
                                                if (selectedTopLeftIndex == index) null else index
                                            )
                                            // Clear any other selection
                                            onSelectedCardIndexChanged(null)
                                        }
                                    },
                                isSelected = selectedTopLeftIndex == index
                            )
                        }
                    }
                }
            }
            
            // Second set of 4 cards (top right stacks)
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
                                // If a card is selected, try to move it to this stack
                                selectedCardIndex?.let { (stackIndex, cardIndex) ->
                                    val selectedCard = secondRowStacks[stackIndex][cardIndex]
                                    println("Selected card: ${selectedCard.value} of ${selectedCard.suit}")
                                    println("Target stack: ${topRightStacks[index].map { "${it.value} of ${it.suit}" }}")
                                    if (isValidTopRightMove(selectedCard, topRightStacks[index])) {
                                        moveCardToTopRight(selectedCard, stackIndex, index)
                                    }
                                    // Always clear selection after attempting move
                                    onSelectedCardIndexChanged(null)
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
                Box(
                    modifier = Modifier
                        .width(35.dp)
                        .height(52.dp)
                        .then(
                            if (secondRowStacks[stackIndex].isEmpty()) {
                                Modifier
                                    .border(2.dp, Color.Gray)
                                    .background(Color.LightGray.copy(alpha = 0.3f))
                                    .clickable {
                                        // Handle clicks on empty stacks
                                        selectedCardIndex?.let { (selectedStackIndex, selectedCardIndex) ->
                                            val selectedCard = secondRowStacks[selectedStackIndex][selectedCardIndex]
                                            val newStacks = moveCardToStack(
                                                selectedCard,
                                                selectedStackIndex,
                                                stackIndex,
                                                secondRowStacks
                                            )
                                            onSecondRowStacksChanged(newStacks)
                                            
                                            // Record the move
                                            onMoveHistoryChanged(moveHistory + Move(
                                                MoveType.MAIN_STACK,
                                                MoveType.MAIN_STACK,
                                                selectedStackIndex,
                                                stackIndex,
                                                selectedCard
                                            ))
                                            onSelectedCardIndexChanged(null)
                                        } ?: selectedTopLeftIndex?.let { selectedIndex ->
                                            val selectedCard = topLeftStacks[selectedIndex].firstOrNull()
                                            if (selectedCard != null) {
                                                moveTopLeftCardToStack(selectedIndex, stackIndex)
                                            }
                                            onSelectedTopLeftIndexChanged(null)
                                        }
                                    }
                            } else {
                                Modifier
                            }
                        )
                ) {
                    // Display all cards in the stack
                    secondRowStacks[stackIndex].forEachIndexed { cardIndex, card ->
                        CardView(
                            card = card,
                            modifier = Modifier
                                .width(35.dp)
                                .height(52.dp)
                                .offset(y = (cardIndex * 20).dp)
                                .clickable {
                                    // If a card is selected, try to move it to this stack
                                    selectedCardIndex?.let { (selectedStackIndex, selectedCardIndex) ->
                                        val selectedCard = secondRowStacks[selectedStackIndex][selectedCardIndex]
                                        val targetStack = secondRowStacks[stackIndex]
                                        
                                        // Check if the move is valid
                                        if (isValidMove(selectedCard, targetStack)) {
                                            val newStacks = moveCardToStack(
                                                selectedCard,
                                                selectedStackIndex,
                                                stackIndex,
                                                secondRowStacks
                                            )
                                            onSecondRowStacksChanged(newStacks)
                                            
                                            // Record the move
                                            onMoveHistoryChanged(moveHistory + Move(
                                                MoveType.MAIN_STACK,
                                                MoveType.MAIN_STACK,
                                                selectedStackIndex,
                                                stackIndex,
                                                selectedCard
                                            ))
                                        }
                                        // Always clear selection after attempting move
                                        onSelectedCardIndexChanged(null)
                                    } ?: selectedTopLeftIndex?.let { selectedIndex ->
                                        // If a top left card is selected, try to move it to this stack
                                        val selectedCard = topLeftStacks[selectedIndex].firstOrNull()
                                        val targetStack = secondRowStacks[stackIndex]
                                        
                                        // Check if the move is valid
                                        if (selectedCard != null && isValidMove(selectedCard, targetStack)) {
                                            moveTopLeftCardToStack(selectedIndex, stackIndex)
                                        }
                                        // Always clear selection after attempting move
                                        onSelectedTopLeftIndexChanged(null)
                                    } ?: run {
                                        // If no card is selected, check if this card is movable
                                        if (card.isMovableWithStack(secondRowStacks[stackIndex], cardIndex)) {
                                            // Toggle selection
                                            onSelectedCardIndexChanged(
                                                if (selectedCardIndex == Pair(stackIndex, cardIndex)) {
                                                    null
                                                } else {
                                                    Pair(stackIndex, cardIndex)
                                                }
                                            )
                                            // Clear any top left selection
                                            onSelectedTopLeftIndexChanged(null)
                                        }
                                    }
                                },
                            isSelected = selectedCardIndex == Pair(stackIndex, cardIndex)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Undo button at the bottom
        Button(
            onClick = onUndoMove,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
            enabled = moveHistory.isNotEmpty()
        ) {
            Text("Undo")
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