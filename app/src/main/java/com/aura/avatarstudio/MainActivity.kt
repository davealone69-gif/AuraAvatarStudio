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

data class Avatar(
    val id: String,
    val name: String,
    val style: String,
    val notes: String = "",
    val hd: Boolean = false,
)

data class GalleryItem(
    val id: String,
    val title: String,
    val type: String,
    val fromAvatar: String = "",
    val path: String = "",
)

enum class Screen { Home, Gallery, Video }

class MainActivity : ComponentActivity() {
    private var navCallback: ((Screen) -> Unit)? = null
    private var createCallback: (() -> Unit)? = null
    private var aboutCallback: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AuraApp(
                        onRegisterNav = { navCallback = it },
                        onRegisterCreate = { createCallback = it },
                        onRegisterAbout = { aboutCallback = it },
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
        R.id.action_new_avatar -> {
            createCallback?.invoke()
            true
        }
        R.id.action_gallery -> {
            navCallback?.invoke(Screen.Gallery)
            true
        }
        R.id.action_video -> {
            navCallback?.invoke(Screen.Video)
            true
        }
        R.id.action_about -> {
            aboutCallback?.invoke()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraApp(
    onRegisterNav: ((Screen) -> Unit) -> Unit,
    onRegisterCreate: (() -> Unit) -> Unit,
    onRegisterAbout: (() -> Unit) -> Unit,
) {
    var screen by remember { mutableStateOf(Screen.Home) }
    var avatars by remember {
        mutableStateOf(
            listOf(
                Avatar("1", "Nova", "Cyber", "First design"),
                Avatar("2", "Lyra", "Anime", "Soft look"),
                Avatar("3", "Vex", "Realistic", "Portrait focus"),
            )
        )
    }
    var gallery by remember { mutableStateOf(listOf<GalleryItem>()) }
    var showCreate by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newStyle by remember { mutableStateOf("Cyber") }

    LaunchedEffect(Unit) {
        onRegisterNav { screen = it }
        onRegisterCreate { showCreate = true }
        onRegisterAbout { showAbout = true }
    }

    when (screen) {
        Screen.Home -> HomeScreen(
            avatars = avatars,
            onDelete = { id -> avatars = avatars.filter { it.id != id } },
            onCreate = { showCreate = true },
            onOpenGallery = { screen = Screen.Gallery },
            onOpenVideo = { screen = Screen.Video },
            onAbout = { showAbout = true },
        )
        Screen.Gallery -> GalleryScreen(
            items = gallery,
            onDelete = { id -> gallery = gallery.filter { it.id != id } },
            onBack = { screen = Screen.Home },
        )
        Screen.Video -> VideoToolsScreen(
            avatars = avatars,
            onGenerated = { name, path ->
                gallery = listOf(
                    GalleryItem(
                        id = System.currentTimeMillis().toString(),
                        title = "$name export",
                        type = "Video",
                        fromAvatar = name,
                        path = path,
                    )
                ) + gallery
            },
            onBack = { screen = Screen.Home },
        )
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Create Avatar") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Name") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = newStyle,
                        onValueChange = { newStyle = it },
                        label = { Text("Style") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            avatars = avatars + Avatar(
                                id = System.currentTimeMillis().toString(),
                                name = newName.trim(),
                                style = newStyle.trim().ifBlank { "Cyber" },
                            )
                            newName = ""
                            showCreate = false
                            screen = Screen.Home
                        }
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreate = false }) { Text("Cancel") }
            },
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("Aura Avatar Studio") },
            text = {
                Text(
                    "Version 1.1.0\n\n" +
                        "Media3 Transformer\n" +
                        "Pick video → trim / scale / rotate → export\n\n" +
                        "Built by REDRUM Studios"
                )
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("OK") }
            },
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
    onOpenVideo: () -> Unit,
    onAbout: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Aura Avatar Studio", fontWeight = FontWeight.Bold)
                        Text(
                            "${avatars.size} avatars • Media3",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAbout) {
                        Icon(Icons.Default.Info, contentDescription = "About")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Avatar") },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = onOpenGallery,
                    label = { Text("Gallery") },
                    leadingIcon = {
                        Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp))
                    },
                )
                AssistChip(
                    onClick = onOpenVideo,
                    label = { Text("Video") },
                    leadingIcon = {
                        Icon(Icons.Default.Videocam, null, Modifier.size(18.dp))
                    },
                )
            }

            if (avatars.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No avatars yet")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(avatars, key = { it.id }) { avatar ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        avatar.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(avatar.style, style = MaterialTheme.typography.bodySmall)
                                    if (avatar.notes.isNotBlank()) {
                                        Text(avatar.notes, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                IconButton(onClick = { onDelete(avatar.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
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
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gallery (${items.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Empty — export a video first")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(140.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(padding),
            ) {
                items(items, key = { it.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Icon(
                                Icons.Default.Videocam,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.CenterHorizontally),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                buildString {
                                    append(item.type)
                                    if (item.fromAvatar.isNotBlank()) append(" • ${item.fromAvatar}")
                                },
                                style = MaterialTheme.typography.bodySmall,
                            )
                            if (item.path.isNotBlank()) {
                                Text(
                                    item.path.substringAfterLast('/'),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                            IconButton(
                                onClick = { onDelete(item.id) },
                                modifier = Modifier.align(Alignment.End),
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
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
    onBack: () -> Unit,
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

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        videoUri = uri
        status = if (uri != null) "Video selected" else ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Media3 Transformer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Pick video → trim / scale / rotate → hardware export",
                style = MaterialTheme.typography.bodyMedium,
            )

            Button(
                onClick = { picker.launch("video/*") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (videoUri != null) "Change video" else "Pick video")
            }

            if (avatars.isNotEmpty()) {
                Text("Tag avatar", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    avatars.forEach { a ->
                        FilterChip(
                            selected = selected == a.name,
                            onClick = { selected = a.name },
                            label = { Text(a.name) },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = trimStartSec,
                onValueChange = { v -> trimStartSec = v.filter { it.isDigit() || it == '.' } },
                label = { Text("Trim start (sec)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = trimEndSec,
                onValueChange = { v -> trimEndSec = v.filter { it.isDigit() || it == '.' } },
                label = { Text("Trim end (sec, empty = full)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = scale,
                onValueChange = { scale = it },
                label = { Text("Scale (e.g. 0.5)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = rotation,
                onValueChange = { v ->
                    rotation = v.filter { it.isDigit() || it == '.' || it == '-' }
                },
                label = { Text("Rotation degrees") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
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
                            val startMs =
                                ((trimStartSec.toFloatOrNull() ?: 0f) * 1000f).toLong()
                                    .coerceAtLeast(0L)
                            val endMs = trimEndSec.toFloatOrNull()?.let {
                                (it * 1000f).toLong()
                            }
                            val s = scale.toFloatOrNull()?.coerceIn(0.1f, 4f) ?: 1f
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
                                ),
                            )
                            status = "Saved: ${out.absolutePath}"
                            val tag = selected.ifBlank { "clip" }
                            onGenerated(tag, out.absolutePath)
                        } catch (e: Exception) {
                            status = "Error: ${e.message ?: e.javaClass.simpleName}"
                        } finally {
                            generating = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = videoUri != null && !generating,
            ) {
                Icon(Icons.Default.Videocam, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Export")
            }
        }
    }
}
