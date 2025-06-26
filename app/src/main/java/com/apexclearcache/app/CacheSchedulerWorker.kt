package com.apexclearcache.app

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CacheSchedulerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "CacheSchedulerWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting scheduled cache operation")
        
        try {
            val config = ScheduleManager.getScheduleConfig(applicationContext)
            when (config.cacheType) {
                CacheType.ATAK -> CacheManager.offloadATAKCache(applicationContext)
                CacheType.ATOS -> CacheManager.clearATOSCache(applicationContext)
                CacheType.BOTH -> {
                    CacheManager.offloadATAKCache(applicationContext)
                    CacheManager.clearATOSCache(applicationContext)
                }
            }
            
            // Update the schedule status
            ScheduleManager.updateLastRunTime(applicationContext)
            ScheduleManager.incrementRunsCompleted(applicationContext)
            
            Log.d(TAG, "Scheduled cache operation completed successfully")
            return Result.success()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during scheduled cache operation: ${e.message}", e)
            return Result.retry()
        }
    }
} 

