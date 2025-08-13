package com.example.memoriaviva2.ui.dois

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SimpleRoutineRepository(private val context: Context) {
    
    private val prefs = context.getSharedPreferences("routine_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    fun saveActivities(activities: List<SimpleActivity>) {
        val json = gson.toJson(activities)
        prefs.edit().putString("activities", json).apply()
    }
    
    fun getActivities(): MutableList<SimpleActivity> {
        checkDailyReset()
        val json = prefs.getString("activities", null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<SimpleActivity>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }
    }
    
    private fun checkDailyReset() {
        val today = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        val lastDay = prefs.getLong("last_day", 0)
        
        if (today > lastDay && lastDay != 0L) {
            // Salvar relatório do dia anterior
            val activities = getActivitiesWithoutReset()
            if (activities.isNotEmpty()) {
                saveReport(lastDay, activities)
            }
            
            // Resetar checkboxes
            activities.forEach { it.isDone = false }
            saveActivities(activities)
        }
        
        prefs.edit().putLong("last_day", today).apply()
    }
    
    private fun getActivitiesWithoutReset(): MutableList<SimpleActivity> {
        val json = prefs.getString("activities", null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<SimpleActivity>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }
    }
    
    private fun saveReport(day: Long, activities: List<SimpleActivity>) {
        val completed = activities.filter { it.isDone }.map { "${it.name} (${it.classification})" }
        val missed = activities.filter { !it.isDone }.map { "${it.name} (${it.classification})" }
        
        val report = mapOf(
            "day" to day,
            "completed" to completed,
            "missed" to missed
        )
        
        val json = gson.toJson(report)
        prefs.edit().putString("report_$day", json).apply()
    }
    
    fun getYesterdayReport(): Map<String, Any>? {
        val yesterday = (System.currentTimeMillis() / (1000 * 60 * 60 * 24)) - 1
        val json = prefs.getString("report_$yesterday", null)
        return if (json != null) {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }
}