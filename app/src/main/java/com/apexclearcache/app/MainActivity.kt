package com.apexclearcache.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.apexclearcache.app.ui.theme.ApexClearCacheTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Storage permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Storage permissions required for cache operations", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate() called")
        enableEdgeToEdge()

        setContent {
            ApexClearCacheTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onOffloadATAKCache = {
                            if (checkAndRequestPermissions()) {
                                CacheManager.offloadATAKCache(this@MainActivity)
                            }
                        },
                        onDeleteATAKCache = {
                            if (checkAndRequestPermissions()) {
                                CacheManager.deleteATAKCache(this@MainActivity)
                            }
                        },
                        onRestoreATAKCache = {
                            if (checkAndRequestPermissions()) {
                                CacheManager.restoreATAKCache(this@MainActivity)
                            }
                        },
                        onClearATOSCache = {
                            if (checkAndRequestPermissions()) {
                                CacheManager.clearATOSCache(this@MainActivity)
                            }
                        },
                        onRequestPermissions = { requestStoragePermissions() }
                    )
                }
            }
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+ we need MANAGE_EXTERNAL_STORAGE
            if (Environment.isExternalStorageManager()) {
                true
            } else {
                requestStoragePermissions()
                false
            }
        } else {
            // For older versions, check standard storage permissions
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            
            val allGranted = permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
            
            if (allGranted) {
                true
            } else {
                requestStoragePermissions()
                false
            }
        }
    }

    private fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+ we need to open system settings
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
                Toast.makeText(this, "Please grant 'All files access' permission and return to the app", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                // Fallback to general storage settings
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
                Toast.makeText(this, "Please grant 'All files access' permission and return to the app", Toast.LENGTH_LONG).show()
            }
        } else {
            // For older versions, request standard storage permissions
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            
            val permissionsToRequest = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()
            
            if (permissionsToRequest.isNotEmpty()) {
                requestPermissionLauncher.launch(permissionsToRequest)
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onOffloadATAKCache: () -> Unit,
    onDeleteATAKCache: () -> Unit,
    onRestoreATAKCache: () -> Unit,
    onClearATOSCache: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    // Only show the Manual tab
    Column(modifier = modifier.fillMaxSize()) {
        ManualCacheScreen(
            onOffloadATAKCache = onOffloadATAKCache,
            onDeleteATAKCache = onDeleteATAKCache,
            onRestoreATAKCache = onRestoreATAKCache,
            onClearATOSCache = onClearATOSCache,
            onRequestPermissions = onRequestPermissions
        )
    }
}

@Composable
fun ManualCacheScreen(
    onOffloadATAKCache: () -> Unit,
    onDeleteATAKCache: () -> Unit,
    onRestoreATAKCache: () -> Unit,
    onClearATOSCache: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    val context = LocalContext.current
    var showOffloadDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAtakWarningDialog by remember { mutableStateOf<String?>(null) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun showAtakWarningAndThen(action: () -> Unit) {
        showAtakWarningDialog = "ATAK must be closed before performing this operation. Please ensure ATAK is not running, then continue."
        pendingAction = action
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ATAK Cache Manager",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Cache Operations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "This app helps manage ATAK cache files:",
                    fontSize = 14.sp
                )
                
                Text(
                    text = "• Offload: Creates backup before clearing\n• Delete: Permanently removes cache\n• Restore: Brings back most recent backup\n• Clear ATOS: Archives ATOS cache",
                    fontSize = 12.sp
                )
            }
        }
        
        // Manual operation buttons
        Button(
            onClick = { showAtakWarningAndThen { showOffloadDialog = true } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Offload ATAK Cache")
        }
        
        Button(
            onClick = { showAtakWarningAndThen { showDeleteDialog = true } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete ATAK Cache")
        }
        
        Button(
            onClick = { showAtakWarningAndThen { showRestoreDialog = true } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Restore ATAK Cache")
        }
        
        Button(
            onClick = onClearATOSCache,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear ATOS Cache")
        }
        
        // Permission request button
        Button(
            onClick = onRequestPermissions,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Request Permissions")
        }
    }
    
    // ATAK must be closed warning dialog
    if (showAtakWarningDialog != null) {
        AlertDialog(
            onDismissRequest = { showAtakWarningDialog = null; pendingAction = null },
            title = { Text("Warning") },
            text = { Text(showAtakWarningDialog!!) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAtakWarningDialog = null
                        pendingAction?.invoke()
                        pendingAction = null
                    }
                ) { Text("Continue") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAtakWarningDialog = null
                        pendingAction = null
                    }
                ) { Text("Cancel") }
            }
        )
    }
    
    // Dialogs
    if (showOffloadDialog) {
        AlertDialog(
            onDismissRequest = { showOffloadDialog = false },
            title = { Text("Offload ATAK Cache") },
            text = { Text("This will move the current cache to a backup location with a timestamp. Continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onOffloadATAKCache()
                        showOffloadDialog = false
                    }
                ) {
                    Text("Offload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOffloadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ATAK Cache") },
            text = { Text("This will permanently delete the current cache file. This action cannot be undone. Continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteATAKCache()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore ATAK Cache") },
            text = { Text("This will restore the most recent backup to the active location. Continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRestoreATAKCache()
                        showRestoreDialog = false
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}