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
                    ATAKCacheScreen(
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
fun ATAKCacheScreen(
    modifier: Modifier = Modifier,
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

    Column(
        modifier = modifier
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
                    text = "ATAK Cache:\n• /sdcard/atak/Databases/statesaver2.sqlite\n\nATOS Cache:\n• /sdcard/atak/tools/atos/atos_history.sqlite",
                    fontSize = 12.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
        
        // ATAK Cache Operations
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ATAK Cache Operations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Button(
                    onClick = { showOffloadDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Offload ATAK Cache",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = "Moves ATAK cache to backup location with timestamp (removes current data)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { showRestoreDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(
                        text = "Restore ATAK Cache",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = "Restores most recent backup (overwrites current data)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Delete ATAK Cache",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = "Permanently deletes ATAK cache file (use with caution)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // ATOS Cache Operations
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ATOS Cache Operations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Button(
                    onClick = onClearATOSCache,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = "Clear ATOS Cache",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = "Moves ATOS cache to archive location with timestamp (safe operation)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Permissions
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Permissions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = "Grant Permissions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = "Request storage permissions if operations fail",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Important Notes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "• Ensure ATAK is closed before performing cache operations\n• Offload removes current planning, icons, and COT data\n• Restore overwrites current data with backup\n• ATAK cache can be offloaded, restored, or deleted\n• ATOS cache can only be cleared (moved to archive)\n• ATOS operations are skipped if directory doesn't exist\n• Grant 'All files access' permission if operations fail",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    // Offload warning dialog
    if (showOffloadDialog) {
        AlertDialog(
            onDismissRequest = { showOffloadDialog = false },
            title = {
                Text(
                    text = "⚠️ Warning: Data Loss",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "Offloading ATAK cache will remove all planning, icons, and COT (Cursor on Target) data from the map. This action will clear your current mission data.\n\nAre you sure you want to continue?",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOffloadDialog = false
                        onOffloadATAKCache()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Yes, Offload Cache")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showOffloadDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    // Restore warning dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = {
                Text(
                    text = "⚠️ Warning: Data Overwrite",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "Restoring ATAK cache will overwrite all current planning, icons, and COT data on the map with the backup data. This action will replace your current mission data.\n\nAre you sure you want to continue?",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreDialog = false
                        onRestoreATAKCache()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Yes, Restore Cache")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showRestoreDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    // Delete warning dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "⚠️ Warning: Permanent Deletion",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "Deleting the ATAK cache cannot be undone! This will permanently remove all planning, icons, and COT data from the map.\n\nAre you sure you want to continue?",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteATAKCache()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Yes, Delete Cache")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}