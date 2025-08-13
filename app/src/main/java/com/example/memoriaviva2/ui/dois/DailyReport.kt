package com.example.memoriaviva2.ui.dois

data class DailyReport(
    val dateEpochDay: Long,
    val completedActivities: List<String>,
    val missedActivities: List<String>
)