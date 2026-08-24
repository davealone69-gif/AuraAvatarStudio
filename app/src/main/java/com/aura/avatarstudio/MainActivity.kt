package com.aura.avatarstudio

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.avatarstudio.ui.theme.AppTheme

data class Avatar(val id: String, val name: String, val style: String, val notes: String = "")
data class GalleryItem(val id: String, val title: String, val type: String)
data class SwarmAgent(val id: String, val name: String, val role: String, val active: Boolean)

enum class Screen { Home, Swarm, Gallery, Video }

class MainActivity : ComponentActivity() {
    private var navCallback: ((Screen) -> Unit)? = null
    private var createCallback: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(Modifier = Modifier.fillMaxSize()) {
                    AuraApp(
                        onAbout = { showAbout() },
                        onRegisterNav = { nav -> navCallback = nav },
                        onRegisterCreate = { create -> createCallback = create }
                    )
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_new_avatar -> { createCallback?.invoke(); true }
        R.id.action_swarm -> { navCallback?.invoke(Screen.Swarm); true }
        R.id.action_gallery -> { navCallback?.invoke(Screen.Gallery); true }
        R.id.action_video -> { navCallback?.invoke(Screen.Video); true }
        R.id.action_about -> { showAbout(); true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showAbout() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Aura Avatar Studio")
            .setMessage("Version 1.0.0\n\nAvatar design studio with swarm builder\nDesign • Pictures • Videos • AI Generate\n\nBuilt by REDRUM Studios")
            .setPositiveButton("OK", null).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraApp(
    onAbout: () -> Unit,
    onRegisterNav: ((Screen) -> Unit) -> Unit,
    onRegisterCreate: (() -> Unit) -> Unit
) {
    var screen by remember { mutableStateOf(Screen.Home) }
    var avatars by remember {
        mutableStateOf(
            listOf(
                Avatar("1", "Nova", "Cyber", "First design"),
                Avatar("2", "Lyra", "Anime", "Soft look"),
                Avatar("3", "Vex", "Realistic", "Portrait focus")
            )
        )
    }
    var gallery by remember {
        mutableStateOf(
            listOf(
                GalleryItem("g1", "Nova portrait", "Image"),
                GalleryItem("g2", "Lyra turnaround", "Image"),
                GalleryItem("g3", "Vex walk cycle", "Video"),
                GalleryItem("g4", "Studio lighting test", "Image")
            )
        )
    }
    var agents by remember {
        mutableStateOf(
            listOf(
                SwarmAgent("a1", "Designer", "Style & proportions", true),
                SwarmAgent("a2", "Prompt Crafter", "Text-to-image prompts", true),
                SwarmAgent("a3", "Refiner", "Detail pass", false),
                SwarmAgent("a4", "Video Director", "Motion & timing", false)
            )
        )
    }
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newStyle by remember { mutableStateOf("Cyber") }

    // Register callbacks so menu can drive Compose state
    LaunchedEffect(Unit) {
        onRegisterNav { screen = it }
        onRegisterCreate { showCreate = true }
    }

    when (screen) {
        Screen.Home -> HomeScreen(
            avatars = avatars,
            onDelete = { id -> avatars = avatars.filter { it.id != id } },
            onCreate = { showCreate = true },
            onOpenSwarm = { screen = Screen.Swarm },
            onOpenGallery = { screen = Screen.Gallery },
            onOpenVideo = { screen = Screen.Video }
        )
        Screen.Swarm -> SwarmScreen(
            agents = agents,
            onToggle = { id ->
                agents = agents.map { if (it.id == id) it.copy(active = !it.active) else it }
            },
            onBack = { screen = Screen.Home }
        )
        Screen.Gallery -> GalleryScreen(
            items = gallery,
            onDelete = { id -> gallery = gallery.filter { it.id != id } },
            onBack = { screen = Screen.Home }
        )
        Screen.Video -> VideoToolsScreen(onBack = { screen = Screen.Home })
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Create Avatar") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Name") }, singleLine = true)
                    OutlinedTextField(value = newStyle, onValueChange = { newStyle = it }, label = { Text("Style") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        avatars = avatars + Avatar(System.currentTimeMillis().toString(), newName, newStyle)
                        newName = ""
                        showCreate = false
                        screen = Screen.Home
                    }
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    avatars: List<Avatar>,
    onDelete: (String) -> Unit,
    onCreate: () -> Unit,
    onOpenSwarm: () -> Unit,
    onOpenGallery: () -> Unit,
    onOpenVideo: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Aura Avatar Studio", fontWeight = FontWeight.Bold)
                        Text("${avatars.size} avatars • Built by REDRUM Studios", style = MaterialTheme.typography.bodySmall)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New Avatar") }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Quick feature chips
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(onClick = onOpenSwarm, label = { Text("Swarm") }, leadingIcon = { Icon(Icons.Default.Hub, null, Modifier.size(18.dp)) })
                AssistChip(onClick = onOpenGallery, label = { Text("Gallery") }, leadingIcon = { Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp)) })
                AssistChip(onClick = onOpenVideo, label = { Text("Video") }, leadingIcon = { Icon(Icons.Default.Videocam, null, Modifier.size(18.dp)) })
            }

            if (avatars.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No avatars yet")
                        Text("Create your first design")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(avatars, key = { it.id }) { avatar ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(avatar.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text(avatar.style, style = MaterialTheme.typography.bodySmall)
                                    if (avatar.notes.isNotBlank()) Text(avatar.notes, style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { onDelete(avatar.id) }) {
                                    Icon(Icons.Default.Delete, "Delete")
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwarmScreen(
    agents: List<SwarmAgent>,
    onToggle: (String) -> Unit,
    onBack: () -> Unit
) {
    val activeCount = agents.count { it.active }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Swarm Builder") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("$activeCount agents active", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(agents, key = { it.id }) { agent ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(agent.name, fontWeight = FontWeight.SemiBold)
                                Text(agent.role, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(checked = agent.active, onCheckedChange = { onToggle(agent.id) })
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { /* run swarm */ },
                modifier = Modifier.fillMaxWidth(),
                enabled = activeCount > 0
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("Run Swarm ($activeCount agents)")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    items: List<GalleryItem>,
    onDelete: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gallery (${items.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Gallery empty")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(140.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(items, key = { it.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Icon(
                                if (item.type == "Video") Icons.Default.Videocam else Icons.Default.Image,
                                null,
                                modifier = Modifier.size(40.dp).align(Alignment.CenterHorizontally)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(item.type, style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = { onDelete(item.id) }) { Text("Remove") }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoToolsScreen(onBack: () -> Unit) {
    var duration by remember { mutableStateOf("4") }
    var fps by remember { mutableStateOf("24") }
    var generating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Tools") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Generate short clips from avatars", style = MaterialTheme.typography.bodyLarge)
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it.filter { c -> c.isDigit() } },
                label = { Text("Duration (seconds)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = fps,
                onValueChange = { fps = it.filter { c -> c.isDigit() } },
                label = { Text("FPS") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (generating) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Rendering…")
            }
            Button(
                onClick = { generating = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !generating
            ) {
                Icon(Icons.Default.Videocam, null)
                Spacer(Modifier.width(8.dp))
                Text("Generate Clip")
            }
            if (generating) {
                TextButton(onClick = { generating = false }) { Text("Cancel") }
            }
        }
    }
}
