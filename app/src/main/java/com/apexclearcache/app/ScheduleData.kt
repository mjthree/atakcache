package com.apexclearcache.app

import java.time.LocalTime

data class ScheduleConfig(
    val isActive: Boolean = false,
    val frequency: Frequency = Frequency.DAYS,
    val frequencyValue: Int = 1,
    val time: LocalTime = LocalTime.of(2, 0), // 2:00 AM default
    val duration: Duration = Duration.INDEFINITE,
    val maxRuns: Int? = null,
    val runsCompleted: Int = 0,
    val cacheType: CacheType = CacheType.BOTH
)

enum class Frequency {
    DAYS, WEEKS, MONTHS
}

enum class Duration {
    INDEFINITE, LIMITED_RUNS
}

enum class CacheType {
    ATAK, ATOS, BOTH
}

data class ScheduleStatus(
    val isActive: Boolean,
    val nextRunTime: String?,
    val lastRunTime: String?,
    val runsCompleted: Int,
    val maxRuns: Int?
) 

