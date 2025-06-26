package com.apexclearcache.app

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CacheManager {
    companion object {
        private const val TAG = "CacheManager"
        
        private fun isAtakRunning(context: Context): Boolean {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val runningProcesses = activityManager.runningAppProcesses
            return runningProcesses?.any { it.processName.contains("atakmap", ignoreCase = true) } == true
        }
        
        fun offloadATAKCache(context: Context) {
            if (isAtakRunning(context)) {
                Toast.makeText(context, "Please close ATAK before clearing the cache.", Toast.LENGTH_LONG).show()
                Log.w(TAG, "ATAK is running. Prompted user to close it before cache operation.")
                return
            }
            try {
                val sdcard = Environment.getExternalStorageDirectory()
                val atakDir = File(sdcard, "atak")
                
                // Handle statesaver2.sqlite
                val statesaverFile = File(atakDir, "Databases/statesaver2.sqlite")
                if (statesaverFile.exists()) {
                    val backupDir = File(atakDir, "Databases/backup")
                    if (!backupDir.exists()) {
                        backupDir.mkdirs()
                    }
                    
                    val timestamp = SimpleDateFormat("dd_MMM_yyyy_HH_mm", Locale.US).format(Date())
                    val backupFile = File(backupDir, "statesaver2_$timestamp.sqlite")
                    
                    if (statesaverFile.renameTo(backupFile)) {
                        Log.d(TAG, "ATAK cache offloaded successfully")
                        // Toast.makeText(context, "ATAK cache offloaded successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "Failed to offload ATAK cache")
                        // Toast.makeText(context, "Failed to offload ATAK cache", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.w(TAG, "ATAK cache file not found")
                    // Toast.makeText(context, "ATAK cache file not found", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error offloading ATAK cache: ${e.message}", e)
                // Toast.makeText(context, "Error offloading ATAK cache: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        fun restoreATAKCache(context: Context) {
            if (isAtakRunning(context)) {
                Toast.makeText(context, "Please close ATAK before restoring the cache.", Toast.LENGTH_LONG).show()
                Log.w(TAG, "ATAK is running. Prompted user to close it before cache operation.")
                return
            }
            try {
                val sdcard = Environment.getExternalStorageDirectory()
                val atakDir = File(sdcard, "atak")
                val backupDir = File(atakDir, "Databases/backup")
                
                if (!backupDir.exists()) {
                    Log.w(TAG, "No backup directory found")
                    // Toast.makeText(context, "No backup directory found", Toast.LENGTH_SHORT).show()
                    return
                }
                
                // Find the most recent backup file
                val backupFiles = backupDir.listFiles { file ->
                    file.name.startsWith("statesaver2_") && file.name.endsWith(".sqlite")
                }
                
                if (backupFiles.isNullOrEmpty()) {
                    Log.w(TAG, "No backup files found")
                    // Toast.makeText(context, "No backup files found", Toast.LENGTH_SHORT).show()
                    return
                }
                
                // Sort by modification time (most recent first)
                val mostRecentBackup = backupFiles.maxByOrNull { it.lastModified() }
                
                if (mostRecentBackup != null) {
                    val targetFile = File(atakDir, "Databases/statesaver2.sqlite")
                    
                    // If current file exists, delete it first
                    if (targetFile.exists()) {
                        targetFile.delete()
                    }
                    
                    // Ensure target directory exists
                    val targetDir = targetFile.parentFile
                    if (targetDir != null && !targetDir.exists()) {
                        targetDir.mkdirs()
                    }
                    
                    if (mostRecentBackup.renameTo(targetFile)) {
                        Log.d(TAG, "ATAK cache restored successfully")
                        // Toast.makeText(context, "ATAK cache restored successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "Failed to restore ATAK cache")
                        // Toast.makeText(context, "Failed to restore ATAK cache", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.w(TAG, "No valid backup files found")
                    // Toast.makeText(context, "No valid backup files found", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring ATAK cache: ${e.message}", e)
                // Toast.makeText(context, "Error restoring ATAK cache: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        fun deleteATAKCache(context: Context) {
            if (isAtakRunning(context)) {
                Toast.makeText(context, "Please close ATAK before deleting the cache.", Toast.LENGTH_LONG).show()
                Log.w(TAG, "ATAK is running. Prompted user to close it before cache operation.")
                return
            }
            try {
                val sdcard = Environment.getExternalStorageDirectory()
                val atakDir = File(sdcard, "atak")
                
                // Delete statesaver2.sqlite
                val statesaverFile = File(atakDir, "Databases/statesaver2.sqlite")
                if (statesaverFile.exists()) {
                    if (statesaverFile.delete()) {
                        Log.d(TAG, "ATAK cache deleted successfully")
                        // Toast.makeText(context, "ATAK cache deleted successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "Failed to delete ATAK cache")
                        // Toast.makeText(context, "Failed to delete ATAK cache", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.w(TAG, "ATAK cache file not found")
                    // Toast.makeText(context, "ATAK cache file not found", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting ATAK cache: ${e.message}", e)
                // Toast.makeText(context, "Error deleting ATAK cache: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        fun clearATOSCache(context: Context) {
            try {
                val sdcard = Environment.getExternalStorageDirectory()
                val atakDir = File(sdcard, "atak")
                
                // Handle atos_history.sqlite
                val atosDir = File(atakDir, "tools/atos")
                if (atosDir.exists()) {
                    val atosHistoryFile = File(atosDir, "atos_history.sqlite")
                    if (atosHistoryFile.exists()) {
                        val archiveDir = File(atosDir, "archive")
                        if (!archiveDir.exists()) {
                            archiveDir.mkdirs()
                        }
                        
                        val timestamp = SimpleDateFormat("dd_MMM_yyyy_HH_mm", Locale.US).format(Date())
                        val archiveFile = File(archiveDir, "atos_history_$timestamp.sqlite")
                        
                        if (atosHistoryFile.renameTo(archiveFile)) {
                            Log.d(TAG, "ATOS cache cleared successfully")
                            // Toast.makeText(context, "ATOS cache cleared successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e(TAG, "Failed to clear ATOS cache")
                            // Toast.makeText(context, "Failed to clear ATOS cache", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.w(TAG, "ATOS cache file not found")
                        // Toast.makeText(context, "ATOS cache file not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.w(TAG, "ATOS directory not found - cannot clear ATOS cache")
                    // Toast.makeText(context, "ATOS directory not found - cannot clear ATOS cache", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing ATOS cache: ${e.message}", e)
                // Toast.makeText(context, "Error clearing ATOS cache: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
} 

