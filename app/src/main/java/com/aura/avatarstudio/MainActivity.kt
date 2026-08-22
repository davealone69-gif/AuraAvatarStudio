package com.aura.avatarstudio

import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                MainScreen()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_new_avatar -> {
                Toast.makeText(this, "New Avatar", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_swarm -> {
                Toast.makeText(this, "Swarm Builder", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_gallery -> {
                Toast.makeText(this, "Gallery", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_video -> {
                Toast.makeText(this, "Video Tools", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.ctx_edit -> { Toast.makeText(this, "Edit Avatar", Toast.LENGTH_SHORT).show(); true }
            R.id.ctx_duplicate -> { Toast.makeText(this, "Duplicate", Toast.LENGTH_SHORT).show(); true }
            R.id.ctx_export -> { Toast.makeText(this, "Export", Toast.LENGTH_SHORT).show(); true }
            R.id.ctx_delete -> { Toast.makeText(this, "Delete", Toast.LENGTH_SHORT).show(); true }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Aura Avatar Studio")
            .setMessage(
                "Version 1.0.0\n\n" +
                "Avatar design studio with swarm builder\n" +
                "Design • Pictures • Videos • AI Generate\n\n" +
                "Built by REDRUM Studios"
            )
            .setPositiveButton("OK", null)
            .show()
    }
}

@Composable
fun MainScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Aura Avatar Studio", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Built by REDRUM Studios", style = MaterialTheme.typography.bodyMedium)
    }
}
