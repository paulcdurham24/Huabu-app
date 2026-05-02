package com.huabu.app.ui.screens.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.huabu.app.data.model.*
import com.huabu.app.ui.theme.*

// ─────────────────────────────────────────────
// CODE SNIPPETS WIDGET
// ─────────────────────────────────────────────

private val LANGUAGE_COLORS = mapOf(
    Language.KOTLIN to Color(0xFF7F52FF),
    Language.JAVA to Color(0xFFB07219),
    Language.PYTHON to Color(0xFF3572A5),
    Language.JAVASCRIPT to Color(0xFFF1E05A),
    Language.TYPESCRIPT to Color(0xFF3178C6),
    Language.SWIFT to Color(0xFFFFAC45),
    Language.GO to Color(0xFF00ADD8),
    Language.RUST to Color(0xFFDEA584),
    Language.CPP to Color(0xFFF34B7D),
    Language.CSHARP to Color(0xFF239120),
    Language.HTML to Color(0xFFE34C26),
    Language.CSS to Color(0xFF563D7C),
    Language.SQL to Color(0xFFFFD700),
    Language.BASH to Color(0xFF89E051),
    Language.OTHER to HuabuSilver
)

@Composable
fun CodeSnippetsWidget(
    snippets: List<CodeSnippet>,
    isCurrentUser: Boolean,
    onAdd: (CodeSnippet) -> Unit,
    onDelete: (CodeSnippet) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSnippet by remember { mutableStateOf<CodeSnippet?>(null) }

    WidgetCard(
        title = "💻 Code Snippets",
        titleColor = Color(0xFF00D4AA),
        action = if (isCurrentUser) {{
            IconButton(onClick = { showAddDialog = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Add snippet", tint = Color(0xFF00D4AA), modifier = Modifier.size(20.dp))
            }
        }} else null
    ) {
        if (snippets.isEmpty()) {
            Text(
                text = if (isCurrentUser) "Tap + to share your best code!" else "No code snippets yet",
                color = HuabuSilver,
                fontSize = 13.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                snippets.take(3).forEach { snippet ->
                    SnippetCard(
                        snippet = snippet,
                        isCurrentUser = isCurrentUser,
                        onClick = { selectedSnippet = snippet },
                        onDelete = { onDelete(snippet) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddSnippetDialog(
            onAdd = { snippet ->
                showAddDialog = false
                onAdd(snippet)
            },
            onDismiss = { showAddDialog = false }
        )
    }

    selectedSnippet?.let { snippet ->
        SnippetDetailDialog(
            snippet = snippet,
            isCurrentUser = isCurrentUser,
            onDelete = { onDelete(snippet); selectedSnippet = null },
            onDismiss = { selectedSnippet = null }
        )
    }
}

@Composable
private fun SnippetCard(
    snippet: CodeSnippet,
    isCurrentUser: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val langColor = LANGUAGE_COLORS[snippet.language] ?: HuabuSilver

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HuabuCardBg2)
            .clickable { onClick() }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(langColor.copy(0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        snippet.language.name,
                        color = langColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    snippet.title,
                    color = HuabuOnSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isCurrentUser) {
                IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Delete", tint = HuabuSilver.copy(0.5f), modifier = Modifier.size(14.dp))
                }
            }
        }

        // Code preview
        Text(
            snippet.code.take(80) + if (snippet.code.length > 80) "..." else "",
            color = HuabuSilver,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF1A1A2E))
                .padding(8.dp)
        )
    }
}

@Composable
private fun AddSnippetDialog(
    onAdd: (CodeSnippet) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedLang by remember { mutableStateOf(Language.KOTLIN) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("💻 Add Code Snippet", color = Color(0xFF00D4AA), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Title", color = HuabuSilver) },
                    singleLine = true,
                    colors = snippetFieldColors(Color(0xFF00D4AA)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Language selector
                Text("Language", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(Language.KOTLIN, Language.JAVA, Language.PYTHON, Language.JAVASCRIPT, Language.TYPESCRIPT).forEach { lang ->
                        val color = LANGUAGE_COLORS[lang] ?: HuabuSilver
                        FilterChip(
                            selected = selectedLang == lang,
                            onClick = { selectedLang = lang },
                            label = { Text(lang.name, fontSize = 9.sp, maxLines = 1) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(0.25f),
                                selectedLabelColor = color
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = code, onValueChange = { code = it },
                    label = { Text("Code", color = HuabuSilver) },
                    minLines = 4,
                    maxLines = 6,
                    colors = snippetFieldColors(Color(0xFF00D4AA)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = HuabuOnSurface
                    )
                )

                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Description (optional)", color = HuabuSilver) },
                    singleLine = true,
                    colors = snippetFieldColors(Color(0xFF00D4AA)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && code.isNotBlank()) {
                        onAdd(CodeSnippet(
                            id = "snippet_${System.currentTimeMillis()}",
                            userId = "",
                            title = title.trim(),
                            code = code.trim(),
                            language = selectedLang,
                            description = description.trim()
                        ))
                    }
                },
                enabled = title.isNotBlank() && code.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4AA)),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Add", color = Color.Black) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) } }
    )
}

@Composable
private fun SnippetDetailDialog(
    snippet: CodeSnippet,
    isCurrentUser: Boolean,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val langColor = LANGUAGE_COLORS[snippet.language] ?: HuabuSilver

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(langColor.copy(0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(snippet.language.name, color = langColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text(snippet.title, color = HuabuOnSurface, fontWeight = FontWeight.ExtraBold)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (snippet.description.isNotBlank()) {
                    Text(snippet.description, color = HuabuSilver, fontSize = 13.sp)
                }

                // Full code
                Text(
                    snippet.code,
                    color = Color(0xFF00D4AA),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A1A2E))
                        .padding(12.dp)
                        .horizontalScroll(rememberScrollState())
                )
            }
        },
        confirmButton = {
            if (isCurrentUser) {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(20.dp)
                ) { Text("Delete") }
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
// TECH STACK WIDGET
// ─────────────────────────────────────────────

@Composable
fun TechStackWidget(
    items: List<TechStackItem>,
    isCurrentUser: Boolean,
    onAdd: (TechStackItem) -> Unit,
    onDelete: (TechStackItem) -> Unit,
    onReorder: (TechStackItem, Boolean) -> Unit = { _, _ -> }
) {
    var showAddDialog by remember { mutableStateOf(false) }

    WidgetCard(
        title = "🛠️ Tech Stack",
        titleColor = Color(0xFF3B82F6),
        action = if (isCurrentUser) {{
            IconButton(onClick = { showAddDialog = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Add skill", tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
            }
        }} else null
    ) {
        if (items.isEmpty()) {
            Text(
                text = if (isCurrentUser) "Tap + to show off your skills!" else "No tech stack listed",
                color = HuabuSilver,
                fontSize = 13.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items.groupBy { it.category }.forEach { (category, categoryItems) ->
                    TechCategorySection(
                        category = category,
                        items = categoryItems,
                        isCurrentUser = isCurrentUser,
                        onDelete = onDelete,
                        onReorder = onReorder
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTechItemDialog(
            onAdd = { item ->
                showAddDialog = false
                onAdd(item)
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun TechCategorySection(
    category: String,
    items: List<TechStackItem>,
    isCurrentUser: Boolean,
    onDelete: (TechStackItem) -> Unit,
    onReorder: (TechStackItem, Boolean) -> Unit = { _, _ -> }
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            category.uppercase(),
            color = HuabuSilver,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        items.forEachIndexed { index, item ->
            TechItemRow(
                item = item,
                isCurrentUser = isCurrentUser,
                canMoveUp = index > 0,
                canMoveDown = index < items.size - 1,
                onDelete = { onDelete(item) },
                onMoveUp = { onReorder(item, true) },
                onMoveDown = { onReorder(item, false) }
            )
        }
    }
}

@Composable
private fun TechItemRow(
    item: TechStackItem,
    isCurrentUser: Boolean,
    canMoveUp: Boolean = false,
    canMoveDown: Boolean = false,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(targetValue = item.proficiency / 100f, label = "tech")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            item.name,
            color = HuabuOnSurface,
            fontSize = 13.sp,
            modifier = Modifier.width(100.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(HuabuDivider)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        when {
                            item.proficiency >= 80 -> Color(0xFF22C55E)
                            item.proficiency >= 60 -> Color(0xFF3B82F6)
                            item.proficiency >= 40 -> Color(0xFFEAB308)
                            else -> Color(0xFFF97316)
                        }
                    )
            )
        }

        Text(
            "${item.proficiency}%",
            color = HuabuSilver,
            fontSize = 11.sp,
            modifier = Modifier.width(30.dp)
        )

        if (isCurrentUser) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(18.dp)) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up", tint = if (canMoveUp) HuabuSilver.copy(0.7f) else androidx.compose.ui.graphics.Color.Transparent, modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(18.dp)) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down", tint = if (canMoveDown) HuabuSilver.copy(0.7f) else androidx.compose.ui.graphics.Color.Transparent, modifier = Modifier.size(14.dp))
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = HuabuSilver.copy(0.5f), modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun AddTechItemDialog(
    onAdd: (TechStackItem) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Mobile") }
    var proficiency by remember { mutableStateOf(50) }
    val categories = listOf("Mobile", "Frontend", "Backend", "DevOps", "Database", "Design", "AI/ML", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("🛠️ Add Skill", color = Color(0xFF3B82F6), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Skill name (e.g. Kotlin)", color = HuabuSilver) },
                    singleLine = true,
                    colors = snippetFieldColors(Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category chips
                Text("Category", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                val rows = categories.chunked(3)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    rows.forEach { rowCats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowCats.forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF3B82F6).copy(0.25f),
                                        selectedLabelColor = Color(0xFF3B82F6)
                                    )
                                )
                            }
                            // Fill empty slots in last row
                            repeat(3 - rowCats.size) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Proficiency slider
                Text("Proficiency: $proficiency%", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = proficiency.toFloat(),
                    onValueChange = { proficiency = it.toInt() },
                    valueRange = 0f..100f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF3B82F6),
                        activeTrackColor = Color(0xFF3B82F6),
                        inactiveTrackColor = HuabuDivider
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(TechStackItem(
                            id = "tech_${System.currentTimeMillis()}",
                            userId = "",
                            name = name.trim(),
                            category = category,
                            proficiency = proficiency,
                            sortOrder = 0
                        ))
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) } }
    )
}

// ─────────────────────────────────────────────
// GIF SHOWCASE WIDGET
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GifShowcaseWidget(
    gifs: List<GifItem>,
    isCurrentUser: Boolean,
    onAdd: (GifItem) -> Unit,
    onDelete: (GifItem) -> Unit,
    onToggleRepeat: (GifItem) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedGif by remember { mutableStateOf<GifItem?>(null) }

    WidgetCard(
        title = "🎬 GIF Showcase",
        titleColor = Color(0xFFFF6B6B),
        action = if (isCurrentUser) {{
            IconButton(onClick = { showAddDialog = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Add GIF", tint = Color(0xFFFF6B6B), modifier = Modifier.size(20.dp))
            }
        }} else null
    ) {
        if (gifs.isEmpty()) {
            Text(
                text = if (isCurrentUser) "Tap + to upload your favourite GIFs!" else "No GIFs yet",
                color = HuabuSilver,
                fontSize = 13.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                gifs.forEach { gif ->
                    GifCard(
                        gif = gif,
                        isCurrentUser = isCurrentUser,
                        onClick = { selectedGif = gif },
                        onDelete = { onDelete(gif) },
                        onToggleRepeat = { onToggleRepeat(gif) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddGifDialog(
            onAdd = { gif ->
                showAddDialog = false
                onAdd(gif)
            },
            onDismiss = { showAddDialog = false }
        )
    }

    selectedGif?.let { gif ->
        GifDetailDialog(
            gif = gif,
            isCurrentUser = isCurrentUser,
            onDelete = { onDelete(gif); selectedGif = null },
            onToggleRepeat = { onToggleRepeat(gif) },
            onDismiss = { selectedGif = null }
        )
    }
}

@Composable
private fun GifCard(
    gif: GifItem,
    isCurrentUser: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleRepeat: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HuabuCardBg2)
            .clickable { onClick() }
    ) {
        // Animated GIF
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Color(0xFF2A2A3E)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = if (gif.isLocal) "file://${gif.url}" else gif.url,
                contentDescription = gif.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Controls row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    gif.title,
                    color = HuabuOnSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (gif.caption.isNotBlank()) {
                    Text(gif.caption, color = HuabuSilver, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Repeat toggle
                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (gif.repeat) Icons.Filled.Repeat else Icons.Filled.RepeatOne,
                        contentDescription = if (gif.repeat) "Loop on" else "Loop off",
                        tint = if (gif.repeat) Color(0xFFFF6B6B) else HuabuSilver,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isCurrentUser) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Delete", tint = HuabuSilver.copy(0.6f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddGifDialog(
    onAdd: (GifItem) -> Unit,
    onDismiss: () -> Unit,
    searchVm: GifSearchViewModel = hiltViewModel()
) {
    val accent = Color(0xFFFF6B6B)
    val searchState by searchVm.state.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var manualUrl by remember { mutableStateOf("") }
    var manualTitle by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var repeat by remember { mutableStateOf(true) }
    var selectedGifUrl by remember { mutableStateOf("") }
    var selectedGifTitle by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("🎬 Add GIF", color = accent, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = HuabuCardBg2,
                    contentColor = accent
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                        text = { Text("Search Giphy", fontSize = 12.sp) })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                        text = { Text("Paste URL", fontSize = 12.sp) })
                }

                if (selectedTab == 0) {
                    // Search box
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            searchVm.search(it)
                        },
                        label = { Text("Search Giphy…", color = HuabuSilver) },
                        singleLine = true,
                        trailingIcon = {
                            if (searchState.isLoading)
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = accent)
                            else if (query.isNotEmpty())
                                IconButton(onClick = { query = ""; searchVm.loadTrending() }) {
                                    Icon(Icons.Filled.Clear, contentDescription = null, tint = HuabuSilver, modifier = Modifier.size(16.dp))
                                }
                        },
                        colors = snippetFieldColors(accent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Selection preview
                    if (selectedGifUrl.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(accent.copy(0.12f))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AsyncImage(
                                model = selectedGifUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp))
                            )
                            Text(
                                "Selected: $selectedGifTitle",
                                color = accent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { selectedGifUrl = ""; selectedGifTitle = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = null, tint = HuabuSilver, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    // Error state
                    if (searchState.error != null) {
                        Text("Could not load GIFs. Check your connection.", color = Color(0xFFDC2626), fontSize = 12.sp)
                    }

                    // GIF grid
                    if (searchState.results.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(searchState.results) { result ->
                                val isSelected = selectedGifUrl == result.gifUrl
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) accent else Color.Transparent,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            selectedGifUrl = result.gifUrl
                                            selectedGifTitle = result.title.ifBlank { query.ifBlank { "GIF" } }
                                        }
                                ) {
                                    AsyncImage(
                                        model = result.previewUrl,
                                        contentDescription = result.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(accent.copy(0.25f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Caption + repeat for search tab
                    if (selectedGifUrl.isNotBlank()) {
                        OutlinedTextField(
                            value = caption, onValueChange = { caption = it },
                            label = { Text("Caption (optional)", color = HuabuSilver) },
                            singleLine = true,
                            colors = snippetFieldColors(accent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Loop/repeat", color = HuabuSilver, fontSize = 13.sp)
                            Switch(checked = repeat, onCheckedChange = { repeat = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = accent, checkedTrackColor = accent.copy(0.5f)))
                        }
                    }
                } else {
                    // Manual URL tab
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = manualTitle, onValueChange = { manualTitle = it },
                            label = { Text("Title", color = HuabuSilver) },
                            singleLine = true,
                            colors = snippetFieldColors(accent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = manualUrl, onValueChange = { manualUrl = it },
                            label = { Text("GIF URL or local path", color = HuabuSilver) },
                            singleLine = true,
                            colors = snippetFieldColors(accent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = caption, onValueChange = { caption = it },
                            label = { Text("Caption (optional)", color = HuabuSilver) },
                            singleLine = true,
                            colors = snippetFieldColors(accent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Loop/repeat", color = HuabuSilver, fontSize = 13.sp)
                            Switch(checked = repeat, onCheckedChange = { repeat = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = accent, checkedTrackColor = accent.copy(0.5f)))
                        }
                    }
                }
            }
        },
        confirmButton = {
            val canAdd = if (selectedTab == 0) selectedGifUrl.isNotBlank()
                         else manualTitle.isNotBlank() && manualUrl.isNotBlank()
            Button(
                onClick = {
                    if (selectedTab == 0 && selectedGifUrl.isNotBlank()) {
                        onAdd(GifItem(
                            id = "gif_${System.currentTimeMillis()}",
                            userId = "",
                            title = selectedGifTitle,
                            url = selectedGifUrl,
                            isLocal = false,
                            repeat = repeat,
                            caption = caption.trim()
                        ))
                    } else if (selectedTab == 1 && manualTitle.isNotBlank() && manualUrl.isNotBlank()) {
                        onAdd(GifItem(
                            id = "gif_${System.currentTimeMillis()}",
                            userId = "",
                            title = manualTitle.trim(),
                            url = manualUrl.trim(),
                            isLocal = !manualUrl.startsWith("http"),
                            repeat = repeat,
                            caption = caption.trim()
                        ))
                    }
                },
                enabled = canAdd,
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Add", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) } }
    )
}

@Composable
private fun GifDetailDialog(
    gif: GifItem,
    isCurrentUser: Boolean,
    onDelete: () -> Unit,
    onToggleRepeat: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(gif.title, color = HuabuOnSurface, fontWeight = FontWeight.ExtraBold)
                IconButton(onClick = onToggleRepeat) {
                    Icon(
                        if (gif.repeat) Icons.Filled.Repeat else Icons.Filled.RepeatOne,
                        contentDescription = "Toggle repeat",
                        tint = if (gif.repeat) Color(0xFFFF6B6B) else HuabuSilver
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2A2A3E)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = if (gif.isLocal) "file://${gif.url}" else gif.url,
                        contentDescription = gif.title,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (gif.caption.isNotBlank()) {
                    Text(gif.caption, color = HuabuSilver, fontSize = 14.sp)
                }

                Text(
                    if (gif.repeat) "🔁 Loop enabled" else "▶️ Play once",
                    color = if (gif.repeat) Color(0xFFFF6B6B) else HuabuSilver,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            if (isCurrentUser) {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(20.dp)
                ) { Text("Delete") }
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

@Composable
private fun snippetFieldColors(accent: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = accent,
    unfocusedBorderColor = HuabuDivider,
    focusedTextColor = HuabuOnSurface,
    unfocusedTextColor = HuabuOnSurface,
    cursorColor = accent
)
