package com.example.memoriaviva2.ui.dois

import java.util.UUID

data class RoutineActivity(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val hour: Int,
    val minute: Int,
    val classification: ActivityClassification = ActivityClassification.SAUDE,
    var isDone: Boolean = false,
    val dateAddedEpochDay: Long = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
) {
    val timeInMinutes: Int
        get() = hour * 60 + minute
}