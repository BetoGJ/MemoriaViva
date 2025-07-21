package com.example.memoriaviva2.dois

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class RoutineRepository(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("RoutineActivitiesPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val activitiesListKey = "routine_activities_list"
    private val lastCheckedDayKey = "last_checked_day_epoch" // Para o reinício diário
    private val dailyReportKeyPrefix = "daily_report_" // Prefixo para salvar relatórios

    // --- Gerenciamento de Atividades ---

    fun addActivity(activity: RoutineActivity) {
        val activities = getAllActivities().toMutableList()
        activities.removeAll { it.id == activity.id } // Evita duplicatas pelo ID
        activities.add(activity)
        saveActivities(activities)
    }

    fun removeActivity(activityId: String) {
        val activities = getAllActivities().toMutableList()
        val removed = activities.removeAll { it.id == activityId }
        if (removed) {
            saveActivities(activities)
        }
    }

    fun updateActivity(updatedActivity: RoutineActivity) {
        val activities = getAllActivities().toMutableList()
        val index = activities.indexOfFirst { it.id == updatedActivity.id }
        if (index != -1) {
            activities[index] = updatedActivity
            saveActivities(activities)
        } else {
            // Se não encontrou, talvez seja uma nova atividade ou um erro de lógica
            Log.w("RoutineRepository", "Tentativa de atualizar atividade não encontrada: ID ${updatedActivity.id}")
            // Poderia adicionar como nova se essa fosse a intenção: addActivity(updatedActivity)
        }
    }


    fun getActivityById(activityId: String): RoutineActivity? {
        return getAllActivities().find { it.id == activityId }
    }

    fun getAllActivities(): List<RoutineActivity> {
        checkForDailyReset() // Verifica e executa o reset antes de retornar as atividades
        val json = sharedPreferences.getString(activitiesListKey, null)
        val activities: MutableList<RoutineActivity> = if (json != null) {
            val type = object : TypeToken<MutableList<RoutineActivity>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }
        return activities.sortedBy { it.timeInMinutes } // Ordena por horário
    }

    private fun saveActivities(activities: List<RoutineActivity>) {
        val json = gson.toJson(activities)
        sharedPreferences.edit().putString(activitiesListKey, json).apply()
    }

    // --- Lógica de Reinício Diário e Relatório ---

    private fun getCurrentEpochDay(): Long {
        return System.currentTimeMillis() / (1000 * 60 * 60 * 24)
    }

    private fun getLastCheckedEpochDay(): Long {
        return sharedPreferences.getLong(lastCheckedDayKey, 0)
    }

    private fun updateLastCheckedEpochDay(day: Long) {
        sharedPreferences.edit().putLong(lastCheckedDayKey, day).apply()
    }

    fun checkForDailyReset() {
        val currentDay = getCurrentEpochDay()
        val lastCheckedDay = getLastCheckedEpochDay()

        Log.d("RoutineRepository", "Checando reset diário. Dia Atual: $currentDay, Último Checado: $lastCheckedDay")


        if (currentDay > lastCheckedDay && lastCheckedDay != 0L) { // lastCheckedDay != 0L para evitar reset na primeira execução
            Log.i("RoutineRepository", "Novo dia detectado ($currentDay). Iniciando reset das atividades para o dia $lastCheckedDay.")

            val activitiesYesterday = getAllActivitiesWithoutReset() // Pega o estado de ontem ANTES de resetar

            if (activitiesYesterday.isNotEmpty()) {
                generateAndSaveDailyReport(lastCheckedDay, activitiesYesterday)
            }

            // Reseta 'isDone' para todas as atividades
            val currentActivities = activitiesYesterday.toMutableList() // Trabalha com a lista de ontem
            currentActivities.forEach { it.isDone = false }
            saveActivities(currentActivities) // Salva as atividades com 'isDone' resetado

            Log.i("RoutineRepository", "Atividades resetadas para o dia $currentDay.")
        }
        // Atualiza o dia da última checagem para o dia atual,
        // independentemente de ter havido reset ou não, para marcar que hoje já foi processado.
        if (lastCheckedDay != currentDay) { // Apenas atualiza se realmente mudou o dia, ou na primeira vez
            updateLastCheckedEpochDay(currentDay)
            Log.d("RoutineRepository", "Último dia checado atualizado para: $currentDay")
        }
    }

    // Método auxiliar para pegar atividades sem disparar o reset (usado internamente pelo checkForDailyReset)
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
        Log.i("RoutineRepository", "Relatório para o dia $reportDayEpoch salvo. Concluídas: ${completed.size}, Perdidas: ${missed.size}")
    }

    fun getDailyReport(reportDayEpoch: Long): DailyReport? {
        val reportKey = "$dailyReportKeyPrefix$reportDayEpoch"
        val reportJson = sharedPreferences.getString(reportKey, null)
        return if (reportJson != null) {
            gson.fromJson(reportJson, DailyReport::class.java)
        } else {
            Log.w("RoutineRepository", "Nenhum relatório encontrado para o dia $reportDayEpoch.")
            null
        }
    }

    fun getYesterdayReport(): DailyReport? {
        val yesterdayEpochDay = getCurrentEpochDay() - 1
        return getDailyReport(yesterdayEpochDay)
    }

    fun getAllReports(): List<DailyReport> {
        val reports = mutableListOf<DailyReport>()
        sharedPreferences.all.keys.filter { it.startsWith(dailyReportKeyPrefix) }.forEach { key ->
            val reportJson = sharedPreferences.getString(key, null)
            if (reportJson != null) {
                gson.fromJson(reportJson, DailyReport::class.java)?.let { reports.add(it) }
            }
        }
        return reports.sortedByDescending { it.dateEpochDay } // Mais recentes primeiro
    }
}
