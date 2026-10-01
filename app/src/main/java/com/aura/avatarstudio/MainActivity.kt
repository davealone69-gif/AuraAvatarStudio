package com.aura.avatarstudio

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.aura.avatarstudio.ui.theme.AppTheme
import kotlinx.coroutines.launch

data class Avatar(val id: String, val name: String, val style: String, val notes: String = "", val hd: Boolean = false)
data class GalleryItem(val id: String, val title: String, val type: String, val fromAvatar: String = "")

enum class Screen { Home, Gallery, Video }

class MainActivity : ComponentActivity() {
    private var navCallback: ((Screen) -> Unit)? = null
    private var createCallback: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
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
        R.id.action_gallery -> { navCallback?.invoke(Screen.Gallery); true }
        R.id.action_video -> { navCallback?.invoke(Screen.Video); true }
        R.id.action_about -> { showAbout(); true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showAbout() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Aura Avatar Studio")
            .setMessage("Version 1.0.0\n\nMedia3 Transformer\nPick video → trim / scale / rotate → export\n\nBuilt by REDRUM Studios")
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
    var gallery by remember { mutableStateOf(listOf<GalleryItem>()) }
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newStyle by remember { mutableStateOf("Cyber") }

    LaunchedEffect(Unit) {
        onRegisterNav { screen = it }
        onRegisterCreate { showCreate = true }
    }

    when (screen) {
        Screen.Home -> HomeScreen(
            avatars = avatars,
            onDelete = { id -> avatars = avatars.filter { it.id != id } },
            onCreate = { showCreate = true },
            onOpenGallery = { screen = Screen.Gallery },
            onOpenVideo = { screen = Screen.Video }
        )
        Screen.Gallery -> GalleryScreen(
            items = gallery,
            onDelete = { id -> gallery = gallery.filter { it.id != id } },
            onBack = { screen = Screen.Home }
        )
        Screen.Video -> VideoToolsScreen(
            avatars = avatars,
            onGenerated = { name, path ->
                gallery = listOf(
                    GalleryItem(System.currentTimeMillis().toString(), "$name export", "Video", name)
                ) + gallery
            },
            onBack = { screen = Screen.Home }
        )
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
    onOpenGallery: () -> Unit,
    onOpenVideo: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Aura Avatar Studio", fontWeight = FontWeight.Bold)
                        Text("${avatars.size} avatars • Media3", style = MaterialTheme.typography.bodySmall)
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
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(onClick = onOpenGallery, label = { Text("Gallery") }, leadingIcon = { Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp)) })
                AssistChip(onClick = onOpenVideo, label = { Text("Video") }, leadingIcon = { Icon(Icons.Default.Videocam, null, Modifier.size(18.dp)) })
            }
            if (avatars.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No avatars yet")
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
                Text("Empty — export a video first")
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
                            Icon(Icons.Default.Videocam, null, modifier = Modifier.size(40.dp).align(Alignment.CenterHorizontally))
                            Spacer(Modifier.height(8.dp))
                            Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(item.type, style = MaterialTheme.typography.bodySmall)
                            IconButton(onClick = { onDelete(item.id) }, modifier = Modifier.align(Alignment.End)) {
                                Icon(Icons.Default.Delete, "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
fun VideoToolsScreen(
    avatars: List<Avatar>,
    onGenerated: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(avatars.firstOrNull()?.name ?: "") }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var trimStartSec by remember { mutableStateOf("0") }
    var trimEndSec by remember { mutableStateOf("") }
    var scale by remember { mutableStateOf("1.0") }
    var rotation by remember { mutableStateOf("0") }
    var generating by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        videoUri = uri
        status = if (uri != null) "Video selected" else ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Media3 Transformer") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Pick video → trim / scale / rotate → hardware export", style = MaterialTheme.typography.bodyMedium)

            Button(onClick = { picker.launch("video/*") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.FolderOpen, null)
                Spacer(Modifier.width(8.dp))
                Text(if (videoUri != null) "Change video" else "Pick video")
            }

            Text("Tag avatar", fontWeight = FontWeight.SemiBold)
            avatars.forEach { a ->
                FilterChip(
                    selected = selected == a.name,
                    onClick = { selected = a.name },
                    label = { Text(a.name) }
                )
            }

            OutlinedTextField(
                value = trimStartSec,
                onValueChange = { trimStartSec = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Trim start (sec)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = trimEndSec,
                onValueChange = { trimEndSec = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Trim end (sec, empty = full)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = scale,
                onValueChange = { scale = it },
                label = { Text("Scale (e.g. 0.5)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = rotation,
                onValueChange = { rotation = it.filter { c -> c.isDigit() || c == '.' || c == '-' } },
                label = { Text("Rotation degrees") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (generating) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (status.isNotBlank()) {
                Text(status, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    val uri = videoUri ?: return@Button
                    if (generating) return@Button
                    generating = true
                    status = "Exporting…"
                    scope.launch {
                        try {
                            val startMs = ((trimStartSec.toFloatOrNull() ?: 0f) * 1000).toLong()
                            val endMs = trimEndSec.toFloatOrNull()?.let { (it * 1000).toLong() }
                            val s = scale.toFloatOrNull() ?: 1f
                            val rot = rotation.toFloatOrNull() ?: 0f
                            val out = VideoTransformer.export(
                                context = context,
                                inputUri = uri,
                                options = VideoTransformer.Options(
                                    trimStartMs = startMs,
                                    trimEndMs = endMs,
                                    scaleX = s,
                                    scaleY = s,
                                    rotationDegrees = rot,
                                )
                            )
                            status = "Saved: ${out.absolutePath}"
                            if (selected.isNotBlank()) onGenerated(selected, out.absolutePath)
                        } catch (e: Exception) {
                            status = "Error: ${e.message}"
                        } finally {
                            generating = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = videoUri != null && !generating
            ) {
                Icon(Icons.Default.Videocam, null)
                Spacer(Modifier.width(8.dp))
                Text("Export")
            }
        }
    }
}
