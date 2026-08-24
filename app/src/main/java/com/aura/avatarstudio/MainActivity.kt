package com.aura.avatarstudio

import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aura.avatarstudio.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppTheme { Surface(Modifier = Modifier.fillMaxSize()) { MainScreen() } } }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_new_avatar -> { toast("New Avatar"); true }
        R.id.action_swarm -> { toast("Swarm Builder"); true }
        R.id.action_gallery -> { toast("Gallery"); true }
        R.id.action_video -> { toast("Video Tools"); true }
        R.id.action_about -> { showAbout(); true }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.ctx_edit -> { toast("Edit Avatar"); true }
        R.id.ctx_duplicate -> { toast("Duplicate"); true }
        R.id.ctx_export -> { toast("Export"); true }
        R.id.ctx_delete -> { toast("Delete"); true }
        else -> super.onContextItemSelected(item)
    }

    private fun showAbout() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Aura Avatar Studio")
            .setMessage("Version 1.0.0\n\nAvatar design studio with swarm builder\nDesign • Pictures • Videos • AI Generate\n\nBuilt by REDRUM Studios")
            .setPositiveButton("OK", null).show()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

@Composable
fun MainScreen() {
    Column(Modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Aura Avatar Studio", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier = Modifier.height(8.dp))
        Text("Built by REDRUM Studios", style = MaterialTheme.typography.bodyMedium)
    }
}
