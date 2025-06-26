package com.apexclearcache.app

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class ScheduleManager {
    companion object {
        private const val TAG = "ScheduleManager"
        private const val PREFS_NAME = "schedule_prefs"
        private const val KEY_IS_ACTIVE = "is_active"
        private const val KEY_FREQUENCY = "frequency"
        private const val KEY_FREQUENCY_VALUE = "frequency_value"
        private const val KEY_TIME_HOUR = "time_hour"
        private const val KEY_TIME_MINUTE = "time_minute"
        private const val KEY_DURATION = "duration"
        private const val KEY_MAX_RUNS = "max_runs"
        private const val KEY_RUNS_COMPLETED = "runs_completed"
        private const val KEY_LAST_RUN = "last_run"
        private const val KEY_CACHE_TYPE = "cache_type"
        
        private const val WORK_NAME = "cache_scheduler_work"
        
        fun getScheduleConfig(context: Context): ScheduleConfig {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return ScheduleConfig(
                isActive = prefs.getBoolean(KEY_IS_ACTIVE, false),
                frequency = Frequency.valueOf(prefs.getString(KEY_FREQUENCY, Frequency.DAYS.name)!!),
                frequencyValue = prefs.getInt(KEY_FREQUENCY_VALUE, 1),
                time = LocalTime.of(
                    prefs.getInt(KEY_TIME_HOUR, 2),
                    prefs.getInt(KEY_TIME_MINUTE, 0)
                ),
                duration = Duration.valueOf(prefs.getString(KEY_DURATION, Duration.INDEFINITE.name)!!),
                maxRuns = if (prefs.contains(KEY_MAX_RUNS)) prefs.getInt(KEY_MAX_RUNS, 0) else null,
                runsCompleted = prefs.getInt(KEY_RUNS_COMPLETED, 0),
                cacheType = CacheType.valueOf(prefs.getString(KEY_CACHE_TYPE, CacheType.BOTH.name)!!)
            )
        }
        
        fun saveScheduleConfig(context: Context, config: ScheduleConfig) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean(KEY_IS_ACTIVE, config.isActive)
                putString(KEY_FREQUENCY, config.frequency.name)
                putInt(KEY_FREQUENCY_VALUE, config.frequencyValue)
                putInt(KEY_TIME_HOUR, config.time.hour)
                putInt(KEY_TIME_MINUTE, config.time.minute)
                putString(KEY_DURATION, config.duration.name)
                if (config.maxRuns != null) {
                    putInt(KEY_MAX_RUNS, config.maxRuns)
                } else {
                    remove(KEY_MAX_RUNS)
                }
                putInt(KEY_RUNS_COMPLETED, config.runsCompleted)
                putString(KEY_CACHE_TYPE, config.cacheType.name)
            }.apply()
        }
        
        fun startSchedule(context: Context, config: ScheduleConfig) {
            Log.d(TAG, "Starting schedule with config: $config")
            
            // Save the configuration
            saveScheduleConfig(context, config)
            
            // Schedule the work
            scheduleWork(context, config)
            
            // Start the foreground service
            CacheSchedulerService.startService(context)
        }
        
        fun stopSchedule(context: Context) {
            Log.d(TAG, "Stopping schedule")
            
            // Cancel the work
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            
            // Stop the foreground service
            CacheSchedulerService.stopService(context)
            
            // Update the configuration
            val config = getScheduleConfig(context).copy(isActive = false)
            saveScheduleConfig(context, config)
        }
        
        private fun scheduleWork(context: Context, config: ScheduleConfig) {
            val workManager = WorkManager.getInstance(context)
            
            // Calculate initial delay to next run time
            val now = LocalDateTime.now()
            val nextRun = now.with(config.time)
            
            // If the time has passed today, schedule for tomorrow
            val targetTime = if (nextRun.isBefore(now)) {
                nextRun.plusDays(1)
            } else {
                nextRun
            }
            
            val initialDelay = java.time.Duration.between(now, targetTime)
            
            Log.d(TAG, "Scheduling work for ${targetTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}")
            
            // Create periodic work request
            val workRequest = PeriodicWorkRequestBuilder<CacheSchedulerWorker>(
                config.frequencyValue.toLong(),
                when (config.frequency) {
                    Frequency.DAYS -> TimeUnit.DAYS
                    Frequency.WEEKS -> TimeUnit.DAYS
                    Frequency.MONTHS -> TimeUnit.DAYS
                }
            ).setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
                .build()
            
            // Enqueue the work
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }
        
        fun updateLastRunTime(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            prefs.edit().putString(KEY_LAST_RUN, now).apply()
        }
        
        fun incrementRunsCompleted(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val current = prefs.getInt(KEY_RUNS_COMPLETED, 0)
            prefs.edit().putInt(KEY_RUNS_COMPLETED, current + 1).apply()
            
            // Check if we've reached the maximum runs
            val maxRuns = if (prefs.contains(KEY_MAX_RUNS)) prefs.getInt(KEY_MAX_RUNS, 0) else null
            if (maxRuns != null && current + 1 >= maxRuns) {
                Log.d(TAG, "Maximum runs reached, stopping schedule")
                stopSchedule(context)
            }
        }
        
        fun getScheduleStatus(context: Context): ScheduleStatus {
            val config = getScheduleConfig(context)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            val lastRun = prefs.getString(KEY_LAST_RUN, null)
            val nextRun = if (config.isActive) {
                val now = LocalDateTime.now()
                val next = now.with(config.time)
                val target = if (next.isBefore(now)) next.plusDays(1) else next
                target.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            } else null
            
            return ScheduleStatus(
                isActive = config.isActive,
                nextRunTime = nextRun,
                lastRunTime = lastRun,
                runsCompleted = config.runsCompleted,
                maxRuns = config.maxRuns
            )
        }
    }
} 

