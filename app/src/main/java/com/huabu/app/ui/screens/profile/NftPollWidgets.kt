package com.huabu.app.ui.screens.profile

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
    val chainColor = nftChainColor(nft.chain)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(HuabuCardBg2)
            .border(1.dp, chainColor.copy(0.4f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        // Art / image
        if (nft.imageUrl.isNotBlank()) {
            AsyncImage(
                model = nft.imageUrl,
                contentDescription = nft.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.radialGradient(listOf(chainColor.copy(0.3f), HuabuDeepPurple))),
                contentAlignment = Alignment.Center
            ) {
                Text("🖼️", fontSize = 40.sp)
            }
        }

        // Dark scrim at bottom for text legibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.75f))))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column {
                Text(nft.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(nft.collection, color = Color.White.copy(0.7f), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    if (nft.priceEth > 0f) {
                        Text("Ξ${nft.priceEth}", color = chainColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Chain badge top-left
        Box(
            modifier = Modifier
                .padding(6.dp)
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(4.dp))
                .background(chainColor.copy(0.85f))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(nft.chain.name.take(4), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
        }

        if (isCurrentUser) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(26.dp)
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = Color.White.copy(0.8f), modifier = Modifier.size(14.dp))
            }
        }
    }
}

private fun nftChainColor(chain: NftChain) = when (chain) {
    NftChain.ETHEREUM -> Color(0xFF627EEA)
    NftChain.POLYGON  -> Color(0xFF8247E5)
    NftChain.SOLANA   -> Color(0xFF14F195)
    NftChain.BASE     -> Color(0xFF0052FF)
    NftChain.ARBITRUM -> Color(0xFF28A0F0)
}

@Composable
private fun AddNftDialog(
    onAdd: (NftItem) -> Unit,
    onDismiss: () -> Unit
) {
    val accent = Color(0xFF8B5CF6)
    var name          by remember { mutableStateOf("") }
    var collection    by remember { mutableStateOf("") }
    var price         by remember { mutableStateOf("") }
    var imageUrl      by remember { mutableStateOf("") }
    var openseaUrl    by remember { mutableStateOf("") }
    var selectedChain by remember { mutableStateOf(NftChain.ETHEREUM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("🖼️ Add NFT", color = accent, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("NFT Name", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = collection, onValueChange = { collection = it },
                    label = { Text("Collection", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = imageUrl, onValueChange = { imageUrl = it },
                    label = { Text("Image URL (optional)", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = openseaUrl, onValueChange = { openseaUrl = it },
                    label = { Text("OpenSea / Marketplace URL (optional)", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price, onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Price in ETH (optional)", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Blockchain", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NftChain.entries.forEach { chain ->
                        val cc = nftChainColor(chain)
                        FilterChip(
                            selected = selectedChain == chain,
                            onClick = { selectedChain = chain },
                            label = { Text(chain.name, fontSize = 9.sp, maxLines = 1) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cc.copy(0.25f),
                                selectedLabelColor = cc
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
                            priceEth = price.toFloatOrNull() ?: 0f,
                            imageUrl = imageUrl.trim(),
                            openseaUrl = openseaUrl.trim()
                        ))
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Add", color = Color.White) }
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
    val chainColor = nftChainColor(nft.chain)
    val context = LocalContext.current

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
                // Art
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.radialGradient(listOf(chainColor.copy(0.3f), HuabuDeepPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    if (nft.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = nft.imageUrl,
                            contentDescription = nft.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                        )
                    } else {
                        Text("🖼️", fontSize = 64.sp)
                    }
                }

                // Chain + price badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(chainColor.copy(0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(nft.chain.name, color = chainColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    if (nft.priceEth > 0f) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(chainColor.copy(0.2f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Ξ${nft.priceEth}", color = chainColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    if (nft.lastSaleEth > 0f) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(HuabuCardBg2)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Last: Ξ${nft.lastSaleEth}", color = HuabuSilver, fontSize = 12.sp)
                        }
                    }
                }

                // View on marketplace button
                if (nft.openseaUrl.isNotBlank()) {
                    Button(
                        onClick = {
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(nft.openseaUrl))) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = chainColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("View on Marketplace", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Fallback: search OpenSea
                    OutlinedButton(
                        onClick = {
                            val q = Uri.encode("${nft.name} ${nft.collection}")
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://opensea.io/assets?search[query]=$q"))) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = chainColor)
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Search on OpenSea", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            if (isCurrentUser) {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(20.dp)
                ) { Text("Remove", color = Color.White) }
            } else {
                TextButton(onClick = onDismiss) { Text("Close", color = HuabuSilver) }
            }
        },
        dismissButton = {
            if (isCurrentUser) {
                TextButton(onClick = onDismiss) { Text("Close", color = HuabuSilver) }
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
    votedPollOptions: Map<String, Char> = emptyMap(),
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
                        votedOption = votedPollOptions[poll.id],
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
    votedOption: Char?,
    onVote: (String, Char) -> Unit,
    onDelete: () -> Unit
) {
    val hasVoted = votedOption != null
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
            if (hasVoted) {
                Text("✓ Voted", color = HuabuGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
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
            isSelected = votedOption == 'A',
            onClick = { if (!hasVoted) onVote(poll.id, 'A') }
        )

        // Option B
        PollOptionBar(
            label = poll.optionB,
            percent = poll.percent('B'),
            votes = poll.votesB,
            total = totalVotes,
            color = Color(0xFFEC4899),
            isSelected = votedOption == 'B',
            onClick = { if (!hasVoted) onVote(poll.id, 'B') }
        )

        // Option C (if provided)
        if (poll.optionC.isNotBlank()) {
            PollOptionBar(
                label = poll.optionC,
                percent = poll.percent('C'),
                votes = poll.votesC,
                total = totalVotes,
                color = Color(0xFF06B6D4),
                isSelected = votedOption == 'C',
                onClick = { if (!hasVoted) onVote(poll.id, 'C') }
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
                isSelected = votedOption == 'D',
                onClick = { if (!hasVoted) onVote(poll.id, 'D') }
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

    val borderMod = if (isSelected) Modifier.border(1.5.dp, color, RoundedCornerShape(8.dp)) else Modifier

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color.copy(0.08f) else HuabuSurface)
            .then(borderMod)
            .clickable { onClick() }
    ) {
        // Progress fill
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedPercent)
                .background(color.copy(if (isSelected) 0.4f else 0.3f))
        )

        // Content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (isSelected) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
                }
                Text(label, color = if (isSelected) color else HuabuOnSurface, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
            }
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
