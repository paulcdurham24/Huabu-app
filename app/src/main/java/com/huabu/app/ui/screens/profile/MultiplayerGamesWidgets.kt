package com.huabu.app.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huabu.app.data.model.*
import com.huabu.app.ui.theme.*
import java.util.UUID

// ─────────────────────────────────────────────
// MULTIPLAYER GAMES WIDGET (Profile Display)
// ─────────────────────────────────────────────

@Composable
fun MultiplayerGamesWidget(
    ticTacToeGames: List<TicTacToeGame>,
    minesweeperGames: List<MinesweeperGame>,
    pendingInvites: List<GameInvite>,
    friends: List<Friend>,
    isCurrentUser: Boolean,
    userId: String,
    userName: String,
    onCreateTicTacToe: (opponentId: String, opponentName: String) -> Unit,
    onCreateMinesweeper: (opponentId: String, opponentName: String) -> Unit,
    onMakeMove: (gameId: String, row: Int, col: Int) -> Unit,
    onAcceptInvite: (invite: GameInvite) -> Unit,
    onDeclineInvite: (invite: GameInvite) -> Unit,
    onPlayGame: (gameId: String, gameType: GameType2P) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedGame by remember { mutableStateOf<TicTacToeGame?>(null) }

    WidgetCard(
        title = "🎮 Multiplayer Games",
        titleColor = Color(0xFF9B59B6),
        action = if (isCurrentUser) {{
            IconButton(onClick = { showCreateDialog = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "New game", tint = Color(0xFF9B59B6), modifier = Modifier.size(20.dp))
            }
        }} else null
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Pending Invites
            if (pendingInvites.isNotEmpty()) {
                Text(
                    "📨 Invitations (${pendingInvites.size})",
                    color = HuabuGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                pendingInvites.take(2).forEach { invite ->
                    InviteCard(
                        invite = invite,
                        onAccept = { onAcceptInvite(invite) },
                        onDecline = { onDeclineInvite(invite) }
                    )
                }
            }

            // Active Tic Tac Toe Games
            val myTurnGames = ticTacToeGames.filter { it.status == GameStatus.ACTIVE && it.currentTurn == if (it.playerXId == userId) "X" else "O" }
            val waitingGames = ticTacToeGames.filter { it.status == GameStatus.ACTIVE && it.currentTurn != if (it.playerXId == userId) "X" else "O" }

            if (myTurnGames.isNotEmpty()) {
                Text(
                    "⏰ Your Turn (${myTurnGames.size})",
                    color = Color(0xFF2ECC71),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                myTurnGames.take(2).forEach { game ->
                    TicTacToeGameCard(
                        game = game,
                        userId = userId,
                        onClick = { selectedGame = game }
                    )
                }
            }

            if (waitingGames.isNotEmpty()) {
                Text(
                    "🕐 Waiting (${waitingGames.size})",
                    color = HuabuSilver,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                waitingGames.take(2).forEach { game ->
                    TicTacToeGameCard(
                        game = game,
                        userId = userId,
                        onClick = { selectedGame = game }
                    )
                }
            }

            if (ticTacToeGames.isEmpty() && pendingInvites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(HuabuCardBg2)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isCurrentUser) "Tap + to challenge a friend!" else "No active games",
                        color = HuabuSilver,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Create Game Dialog
    if (showCreateDialog) {
        CreateGameDialog(
            friends = friends,
            onCreateTicTacToe = { friendId, friendName ->
                showCreateDialog = false
                onCreateTicTacToe(friendId, friendName)
            },
            onCreateMinesweeper = { friendId, friendName ->
                showCreateDialog = false
                onCreateMinesweeper(friendId, friendName)
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    // Play Game Dialog
    selectedGame?.let { game ->
        TicTacToePlayDialog(
            game = game,
            userId = userId,
            onMakeMove = { row, col -> onMakeMove(game.id, row, col) },
            onDismiss = { selectedGame = null }
        )
    }
}

@Composable
private fun InviteCard(
    invite: GameInvite,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(HuabuCardBg2)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(invite.fromUserName, color = HuabuOnSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("invited you to", color = HuabuSilver, fontSize = 12.sp)
                Text(
                    if (invite.gameType == GameType2P.TICTACTOE) "⭕ Tic Tac Toe" else "💣 Minesweeper",
                    color = if (invite.gameType == GameType2P.TICTACTOE) Color(0xFF3498DB) else Color(0xFFE74C3C),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
            if (invite.message.isNotBlank()) {
                Text("\"${invite.message}\"", color = HuabuSilver.copy(0.8f), fontSize = 11.sp, maxLines = 1)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) { Text("Play", fontSize = 12.sp) }

            OutlinedButton(
                onClick = onDecline,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = HuabuSilver),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(HuabuDivider))
            ) { Text("Skip", fontSize = 12.sp) }
        }
    }
}

@Composable
private fun TicTacToeGameCard(
    game: TicTacToeGame,
    userId: String,
    onClick: () -> Unit
) {
    val isMyTurn = game.currentTurn == if (game.playerXId == userId) "X" else "O"
    val opponentName = if (game.playerXId == userId) game.playerOName else game.playerXName
    val mySymbol = if (game.playerXId == userId) "X" else "O"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(HuabuCardBg2)
            .clickable { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            // Symbol indicator
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (mySymbol == "X") Color(0xFF3498DB).copy(0.2f) else Color(0xFFE74C3C).copy(0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    mySymbol,
                    color = if (mySymbol == "X") Color(0xFF3498DB) else Color(0xFFE74C3C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Column {
                Text("vs $opponentName", color = HuabuOnSurface, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(
                    if (isMyTurn) "⏰ Your turn!" else "🕐 Waiting...",
                    color = if (isMyTurn) Color(0xFF2ECC71) else HuabuSilver,
                    fontSize = 11.sp
                )
            }
        }

        // Mini board preview
        MiniBoard(game.board)
    }
}

@Composable
private fun MiniBoard(board: String) {
    Column(
        modifier = Modifier
            .size(48.dp)
            .border(1.dp, HuabuDivider, RoundedCornerShape(4.dp))
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(3) { row ->
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(3) { col ->
                    val cell = board[row * 3 + col]
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                when (cell) {
                                    'X' -> Color(0xFF3498DB).copy(0.3f)
                                    'O' -> Color(0xFFE74C3C).copy(0.3f)
                                    else -> Color.Transparent
                                },
                                RoundedCornerShape(2.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            when (cell) {
                                'X' -> "✕"
                                'O' -> "◯"
                                else -> ""
                            },
                            fontSize = 8.sp,
                            color = when (cell) {
                                'X' -> Color(0xFF3498DB)
                                'O' -> Color(0xFFE74C3C)
                                else -> Color.Transparent
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// CREATE GAME DIALOG
// ─────────────────────────────────────────────

@Composable
private fun CreateGameDialog(
    friends: List<Friend>,
    onCreateTicTacToe: (String, String) -> Unit,
    onCreateMinesweeper: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedGameType by remember { mutableStateOf(GameType2P.TICTACTOE) }
    var selectedFriend by remember { mutableStateOf<Friend?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("🎮 New Game", color = Color(0xFF9B59B6), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Game type selector
                Text("Choose Game", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameTypeButton(
                        selected = selectedGameType == GameType2P.TICTACTOE,
                        onClick = { selectedGameType = GameType2P.TICTACTOE },
                        icon = "⭕",
                        label = "Tic Tac Toe",
                        color = Color(0xFF3498DB),
                        modifier = Modifier.weight(1f)
                    )
                    GameTypeButton(
                        selected = selectedGameType == GameType2P.MINESWEEPER,
                        onClick = { selectedGameType = GameType2P.MINESWEEPER },
                        icon = "💣",
                        label = "Minesweeper",
                        color = Color(0xFFE74C3C),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Friend selector
                Text("Challenge Friend", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                if (friends.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HuabuCardBg2)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No friends yet! Add friends to play.", color = HuabuSilver, fontSize = 13.sp)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        friends.take(5).forEach { friend ->
                            FriendSelectButton(
                                friend = friend,
                                selected = selectedFriend?.id == friend.id,
                                onClick = { selectedFriend = friend }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedFriend?.let { friend ->
                        if (selectedGameType == GameType2P.TICTACTOE) {
                            onCreateTicTacToe(friend.friendId, friend.friendName)
                        } else {
                            onCreateMinesweeper(friend.friendId, friend.friendName)
                        }
                    }
                },
                enabled = selectedFriend != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B59B6)),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Send Invite") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) } }
    )
}

@Composable
private fun GameTypeButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) color.copy(0.2f) else HuabuCardBg2)
            .border(2.dp, if (selected) color else HuabuDivider, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(icon, fontSize = 24.sp)
        Text(label, color = if (selected) color else HuabuSilver, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FriendSelectButton(
    friend: Friend,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) HuabuViolet.copy(0.2f) else HuabuCardBg2)
            .border(1.dp, if (selected) HuabuViolet else HuabuDivider, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(HuabuViolet),
            contentAlignment = Alignment.Center
        ) {
            Text(friend.friendName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Text(friend.friendName, color = HuabuOnSurface, fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.weight(1f))

        if (selected) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = HuabuViolet, modifier = Modifier.size(20.dp))
        }
    }
}

// ─────────────────────────────────────────────
// TIC TAC TOE PLAY DIALOG
// ─────────────────────────────────────────────

@Composable
private fun TicTacToePlayDialog(
    game: TicTacToeGame,
    userId: String,
    onMakeMove: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val board = game.getBoardArray()
    val isMyTurn = game.currentTurn == if (game.playerXId == userId) "X" else "O"
    val mySymbol = if (game.playerXId == userId) "X" else "O"
    val opponentName = if (game.playerXId == userId) game.playerOName else game.playerXName

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("⭕ Tic Tac Toe", color = Color(0xFF3498DB), fontWeight = FontWeight.ExtraBold)
                    Text("You ($mySymbol) vs $opponentName", color = HuabuSilver, fontSize = 12.sp)
                }
                if (game.status == GameStatus.FINISHED) {
                    val won = game.winner == mySymbol
                    Text(
                        if (won) "🏆 WON!" else if (game.winner == "DRAW") "🤝 DRAW" else "😔 LOST",
                        color = if (won) Color(0xFF2ECC71) else if (game.winner == "DRAW") HuabuGold else Color(0xFFE74C3C),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else if (isMyTurn) {
                    Text("⏰ YOUR TURN", color = Color(0xFF2ECC71), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Game board
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HuabuCardBg2)
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(3) { row ->
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(3) { col ->
                                    val cell = board[row][col]
                                    val canMove = isMyTurn && cell == TicTacToeCell.EMPTY && game.status == GameStatus.ACTIVE

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when (cell) {
                                                    TicTacToeCell.X -> Color(0xFF3498DB).copy(0.2f)
                                                    TicTacToeCell.O -> Color(0xFFE74C3C).copy(0.2f)
                                                    else -> Color(0xFF2A2A3E)
                                                }
                                            )
                                            .clickable(enabled = canMove) { onMakeMove(row, col) }
                                            .border(
                                                1.dp,
                                                when (cell) {
                                                    TicTacToeCell.X -> Color(0xFF3498DB).copy(0.5f)
                                                    TicTacToeCell.O -> Color(0xFFE74C3C).copy(0.5f)
                                                    else -> HuabuDivider
                                                },
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            when (cell) {
                                                TicTacToeCell.X -> "✕"
                                                TicTacToeCell.O -> "◯"
                                                else -> ""
                                            },
                                            fontSize = 36.sp,
                                            color = when (cell) {
                                                TicTacToeCell.X -> Color(0xFF3498DB)
                                                TicTacToeCell.O -> Color(0xFFE74C3C)
                                                else -> Color.Transparent
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status
                if (game.status == GameStatus.ACTIVE) {
                    Text(
                        if (isMyTurn) "Tap an empty square to play!" else "Waiting for $opponentName...",
                        color = if (isMyTurn) Color(0xFF2ECC71) else HuabuSilver,
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (game.status == GameStatus.FINISHED) "Close" else "Exit Game", color = HuabuSilver)
            }
        }
    )
}
