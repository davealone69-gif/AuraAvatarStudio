package com.aura.avatarstudio

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.avatarstudio.ui.theme.AppTheme

data class Avatar(val id: String, val name: String, val style: String, val notes: String = "")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(Modifier = Modifier.fillMaxSize()) {
                    AuraApp(onAbout = { showAbout() })
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_new_avatar -> { Toast.makeText(this, "New Avatar", Toast.LENGTH_SHORT).show(); true }
        R.id.action_swarm -> { Toast.makeText(this, "Swarm Builder", Toast.LENGTH_SHORT).show(); true }
        R.id.action_gallery -> { Toast.makeText(this, "Gallery", Toast.LENGTH_SHORT).show(); true }
        R.id.action_video -> { Toast.makeText(this, "Video Tools", Toast.LENGTH_SHORT).show(); true }
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
fun AuraApp(onAbout: () -> Unit) {
    var avatars by remember {
        mutableStateOf(
            listOf(
                Avatar("1", "Nova", "Cyber", "First design"),
                Avatar("2", "Lyra", "Anime", "Soft look"),
                Avatar("3", "Vex", "Realistic", "Portrait focus")
            )
        )
    }
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newStyle by remember { mutableStateOf("Cyber") }

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
                onClick = { showCreate = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New Avatar") }
            )
        }
    ) { padding ->
        if (avatars.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(avatars, key = { it.id }) { avatar ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(avatar.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(avatar.style, style = MaterialTheme.typography.bodySmall)
                                if (avatar.notes.isNotBlank()) Text(avatar.notes, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { avatars = avatars.filter { it.id != avatar.id } }) {
                                Icon(Icons.Default.Delete, "Delete")
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
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
                    }
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } }
        )
    }
}
