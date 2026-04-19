package com.huabu.app.ui.screens.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huabu.app.data.model.*
import com.huabu.app.ui.theme.*

// ─────────────────────────────────────────────
// NFT SHOWCASE WIDGET
// ─────────────────────────────────────────────

@Composable
fun NftShowcaseWidget(
    nfts: List<NftItem>,
    isCurrentUser: Boolean,
    onAdd: (NftItem) -> Unit,
    onDelete: (NftItem) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedNft by remember { mutableStateOf<NftItem?>(null) }

    WidgetCard(
        title = "🖼️ NFT Showcase",
        titleColor = Color(0xFF8B5CF6),
        action = if (isCurrentUser) {{
            IconButton(onClick = { showAddDialog = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Add NFT", tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
            }
        }} else null
    ) {
        if (nfts.isEmpty()) {
            Text(
                text = if (isCurrentUser) "Tap + to flex your NFT collection!" else "No NFTs showcased",
                color = HuabuSilver,
                fontSize = 13.sp
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(nfts.size) { index ->
                    val nft = nfts[index]
                    NftCard(
                        nft = nft,
                        isCurrentUser = isCurrentUser,
                        onClick = { selectedNft = nft },
                        onDelete = { onDelete(nft) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddNftDialog(
            onAdd = { item ->
                showAddDialog = false
                onAdd(item)
            },
            onDismiss = { showAddDialog = false }
        )
    }

    selectedNft?.let { nft ->
        NftDetailDialog(
            nft = nft,
            isCurrentUser = isCurrentUser,
            onDelete = { onDelete(nft); selectedNft = null },
            onDismiss = { selectedNft = null }
        )
    }
}

@Composable
private fun NftCard(
    nft: NftItem,
    isCurrentUser: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val chainColor = when (nft.chain) {
        NftChain.ETHEREUM -> Color(0xFF627EEA)
        NftChain.POLYGON -> Color(0xFF8247E5)
        NftChain.SOLANA -> Color(0xFF14F195)
        NftChain.BASE -> Color(0xFF0052FF)
        NftChain.ARBITRUM -> Color(0xFF28A0F0)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(HuabuCardBg2)
            .border(1.dp, chainColor.copy(0.3f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(chainColor.copy(0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(nft.chain.name.take(4), color = chainColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            Column {
                Text(nft.name, color = HuabuOnSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(nft.collection, color = HuabuSilver, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            if (nft.priceEth > 0f) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(Icons.Filled.Diamond, contentDescription = null, tint = chainColor, modifier = Modifier.size(10.dp))
                    Text("${nft.priceEth} Ξ", color = chainColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isCurrentUser) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = HuabuSilver.copy(0.6f), modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun AddNftDialog(
    onAdd: (NftItem) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var collection by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedChain by remember { mutableStateOf(NftChain.ETHEREUM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("🖼️ Add NFT", color = Color(0xFF8B5CF6), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("NFT Name", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(Color(0xFF8B5CF6)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = collection, onValueChange = { collection = it },
                    label = { Text("Collection", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(Color(0xFF8B5CF6)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price, onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Price (ETH)", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(Color(0xFF8B5CF6)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Blockchain", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NftChain.entries.forEach { chain ->
                        val chainColor = when (chain) {
                            NftChain.ETHEREUM -> Color(0xFF627EEA)
                            NftChain.POLYGON -> Color(0xFF8247E5)
                            NftChain.SOLANA -> Color(0xFF14F195)
                            NftChain.BASE -> Color(0xFF0052FF)
                            NftChain.ARBITRUM -> Color(0xFF28A0F0)
                        }
                        FilterChip(
                            selected = selectedChain == chain,
                            onClick = { selectedChain = chain },
                            label = { Text(chain.name, fontSize = 9.sp, maxLines = 1) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chainColor.copy(0.25f),
                                selectedLabelColor = chainColor
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(NftItem(
                            id = "nft_${System.currentTimeMillis()}",
                            userId = "",
                            name = name.trim(),
                            collection = collection.trim().ifEmpty { "Unknown Collection" },
                            chain = selectedChain,
                            priceEth = price.toFloatOrNull() ?: 0f
                        ))
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) } }
    )
}

@Composable
private fun NftDetailDialog(
    nft: NftItem,
    isCurrentUser: Boolean,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val chainColor = when (nft.chain) {
        NftChain.ETHEREUM -> Color(0xFF627EEA)
        NftChain.POLYGON -> Color(0xFF8247E5)
        NftChain.SOLANA -> Color(0xFF14F195)
        NftChain.BASE -> Color(0xFF0052FF)
        NftChain.ARBITRUM -> Color(0xFF28A0F0)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = {
            Column {
                Text(nft.name, color = HuabuOnSurface, fontWeight = FontWeight.ExtraBold)
                Text(nft.collection, color = HuabuSilver, fontSize = 13.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.radialGradient(listOf(chainColor.copy(0.3f), chainColor.copy(0.1f)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🖼️", fontSize = 64.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(chainColor.copy(0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(nft.chain.name, color = chainColor, fontWeight = FontWeight.Bold)
                    }
                    if (nft.priceEth > 0f) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(chainColor.copy(0.2f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("${nft.priceEth} Ξ", color = chainColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (nft.lastSaleEth > 0f) {
                    Text("Last sale: ${nft.lastSaleEth} Ξ", color = HuabuSilver, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            if (isCurrentUser) {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(20.dp)
                ) { Text("Remove") }
            } else {
                TextButton(onClick = onDismiss) { Text("Close", color = HuabuSilver) }
            }
        },
        dismissButton = {
            if (isCurrentUser) {
                TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) }
            } else null
        }
    )
}

// ─────────────────────────────────────────────
// POLLS WIDGET
// ─────────────────────────────────────────────

@Composable
fun PollsWidget(
    polls: List<ProfilePoll>,
    isCurrentUser: Boolean,
    userId: String,
    onCreatePoll: (ProfilePoll) -> Unit,
    onDeletePoll: (ProfilePoll) -> Unit,
    onVote: (pollId: String, option: Char) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    WidgetCard(
        title = "📊 Polls",
        titleColor = HuabuGold,
        action = if (isCurrentUser) {{
            IconButton(onClick = { showCreateDialog = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Create poll", tint = HuabuGold, modifier = Modifier.size(20.dp))
            }
        }} else null
    ) {
        if (polls.isEmpty()) {
            Text(
                text = if (isCurrentUser) "Tap + to ask your friends anything!" else "No active polls",
                color = HuabuSilver,
                fontSize = 13.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                polls.forEach { poll ->
                    PollCard(
                        poll = poll,
                        isCurrentUser = isCurrentUser,
                        userId = userId,
                        onVote = onVote,
                        onDelete = { onDeletePoll(poll) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePollDialog(
            onCreate = { poll ->
                showCreateDialog = false
                onCreatePoll(poll)
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
private fun PollCard(
    poll: ProfilePoll,
    isCurrentUser: Boolean,
    userId: String,
    onVote: (String, Char) -> Unit,
    onDelete: () -> Unit
) {
    var hasVoted by remember { mutableStateOf(false) } // In real app, check PollVote table
    val totalVotes = poll.totalVotes()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HuabuCardBg2)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                poll.question,
                color = HuabuOnSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            if (isCurrentUser) {
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Delete", tint = HuabuSilver.copy(0.5f), modifier = Modifier.size(16.dp))
                }
            }
        }

        // Option A
        PollOptionBar(
            label = poll.optionA,
            percent = poll.percent('A'),
            votes = poll.votesA,
            total = totalVotes,
            color = Color(0xFF8B5CF6),
            isSelected = false,
            onClick = { if (!hasVoted) { onVote(poll.id, 'A'); hasVoted = true } }
        )

        // Option B
        PollOptionBar(
            label = poll.optionB,
            percent = poll.percent('B'),
            votes = poll.votesB,
            total = totalVotes,
            color = Color(0xFFEC4899),
            isSelected = false,
            onClick = { if (!hasVoted) { onVote(poll.id, 'B'); hasVoted = true } }
        )

        // Option C (if provided)
        if (poll.optionC.isNotBlank()) {
            PollOptionBar(
                label = poll.optionC,
                percent = poll.percent('C'),
                votes = poll.votesC,
                total = totalVotes,
                color = Color(0xFF06B6D4),
                isSelected = false,
                onClick = { if (!hasVoted) { onVote(poll.id, 'C'); hasVoted = true } }
            )
        }

        // Option D (if provided)
        if (poll.optionD.isNotBlank()) {
            PollOptionBar(
                label = poll.optionD,
                percent = poll.percent('D'),
                votes = poll.votesD,
                total = totalVotes,
                color = Color(0xFFEAB308),
                isSelected = false,
                onClick = { if (!hasVoted) { onVote(poll.id, 'D'); hasVoted = true } }
            )
        }

        Text("$totalVotes votes", color = HuabuSilver, fontSize = 11.sp)
    }
}

@Composable
private fun PollOptionBar(
    label: String,
    percent: Float,
    votes: Int,
    total: Int,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedPercent by animateFloatAsState(targetValue = percent / 100f, label = "poll")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(HuabuSurface)
            .clickable { onClick() }
    ) {
        // Progress fill
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedPercent)
                .background(color.copy(0.3f))
        )

        // Content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = HuabuOnSurface, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Text("${percent.toInt()}%", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CreatePollDialog(
    onCreate: (ProfilePoll) -> Unit,
    onDismiss: () -> Unit
) {
    var question by remember { mutableStateOf("") }
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("📊 Create Poll", color = HuabuGold, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = question, onValueChange = { question = it },
                    label = { Text("Question", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(HuabuGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Options", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)

                OutlinedTextField(
                    value = optionA, onValueChange = { optionA = it },
                    label = { Text("Option A", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(Color(0xFF8B5CF6)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optionB, onValueChange = { optionB = it },
                    label = { Text("Option B", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(Color(0xFFEC4899)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optionC, onValueChange = { optionC = it },
                    label = { Text("Option C (optional)", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(Color(0xFF06B6D4)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optionD, onValueChange = { optionD = it },
                    label = { Text("Option D (optional)", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(Color(0xFFEAB308)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (question.isNotBlank() && optionA.isNotBlank() && optionB.isNotBlank()) {
                        onCreate(ProfilePoll(
                            id = "poll_${System.currentTimeMillis()}",
                            userId = "",
                            question = question.trim(),
                            optionA = optionA.trim(),
                            optionB = optionB.trim(),
                            optionC = optionC.trim(),
                            optionD = optionD.trim()
                        ))
                    }
                },
                enabled = question.isNotBlank() && optionA.isNotBlank() && optionB.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = HuabuGold),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Create", color = Color.Black) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) } }
    )
}

@Composable
private fun outlinedFieldColors(accent: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = accent,
    unfocusedBorderColor = HuabuDivider,
    focusedTextColor = HuabuOnSurface,
    unfocusedTextColor = HuabuOnSurface,
    cursorColor = accent
)
