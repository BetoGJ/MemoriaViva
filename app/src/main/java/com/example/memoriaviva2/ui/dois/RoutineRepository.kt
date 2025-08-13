package com.example.memoriaviva2.ui.dois

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RoutineRepository(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("RoutineActivitiesPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val activitiesListKey = "routine_activities_list"
    private val lastCheckedDayKey = "last_checked_day_epoch"
    private val dailyReportKeyPrefix = "daily_report_"

    fun addActivity(activity: RoutineActivity) {
        val activities = getAllActivities().toMutableList()
        activities.removeAll { it.id == activity.id }
        activities.add(activity)
        saveActivities(activities)
    }

    fun removeActivity(activityId: String) {
        val activities = getAllActivities().toMutableList()
        activities.removeAll { it.id == activityId }
        saveActivities(activities)
    }

    fun updateActivity(updatedActivity: RoutineActivity) {
        val activities = getAllActivities().toMutableList()
        val index = activities.indexOfFirst { it.id == updatedActivity.id }
        if (index != -1) {
            activities[index] = updatedActivity
            saveActivities(activities)
        }
    }

    fun getAllActivities(): List<RoutineActivity> {
        checkForDailyReset()
        val json = sharedPreferences.getString(activitiesListKey, null)
        val activities: MutableList<RoutineActivity> = if (json != null) {
            val type = object : TypeToken<MutableList<RoutineActivity>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }
        return activities.sortedBy { it.timeInMinutes }
    }

    private fun saveActivities(activities: List<RoutineActivity>) {
        val json = gson.toJson(activities)
        sharedPreferences.edit().putString(activitiesListKey, json).apply()
    }

    private fun getCurrentEpochDay(): Long {
        return System.currentTimeMillis() / (1000 * 60 * 60 * 24)
    }

    private fun checkForDailyReset() {
        val currentDay = getCurrentEpochDay()
        val lastCheckedDay = sharedPreferences.getLong(lastCheckedDayKey, 0)

        if (currentDay > lastCheckedDay && lastCheckedDay != 0L) {
            val activitiesYesterday = getAllActivitiesWithoutReset()
            
            if (activitiesYesterday.isNotEmpty()) {
                generateAndSaveDailyReport(lastCheckedDay, activitiesYesterday)
            }

            val currentActivities = activitiesYesterday.toMutableList()
            for (activity in currentActivities) {
                activity.isDone = false
            }
            saveActivities(currentActivities)
        }
        
        if (lastCheckedDay != currentDay) {
            sharedPreferences.edit().putLong(lastCheckedDayKey, currentDay).apply()
        }
    }

    private fun getAllActivitiesWithoutReset(): List<RoutineActivity> {
        val json = sharedPreferences.getString(activitiesListKey, null)
        val activities: MutableList<RoutineActivity> = if (json != null) {
            val type = object : TypeToken<MutableList<RoutineActivity>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }
        return activities.sortedBy { it.timeInMinutes }
    }

    private fun generateAndSaveDailyReport(reportDayEpoch: Long, activities: List<RoutineActivity>) {
        val completed = activities.filter { it.isDone }.map { "${it.name} (${it.classification.displayName} às ${String.format("%02d:%02d", it.hour, it.minute)})" }
        val missed = activities.filter { !it.isDone }.map { "${it.name} (${it.classification.displayName} às ${String.format("%02d:%02d", it.hour, it.minute)})" }

        val report = DailyReport(reportDayEpoch, completed, missed)
        val reportJson = gson.toJson(report)
        val reportKey = "$dailyReportKeyPrefix$reportDayEpoch"

        sharedPreferences.edit().putString(reportKey, reportJson).apply()
    }

    fun getYesterdayReport(): DailyReport? {
        val yesterdayEpochDay = getCurrentEpochDay() - 1
        val reportKey = "$dailyReportKeyPrefix$yesterdayEpochDay"
        val reportJson = sharedPreferences.getString(reportKey, null)
        return if (reportJson != null) {
            gson.fromJson(reportJson, DailyReport::class.java)
        } else {
            null
        }
    }
}