package com.example.memoriaviva2.dois // Ou seu pacote correspondente

import java.util.UUID

data class RoutineActivity(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var hour: Int, // 0-23
    var minute: Int, // 0-59
    var classification: ActivityClassification,
    var isDone: Boolean = false,
    val dateAddedEpochDay: Long = System.currentTimeMillis() / (1000 * 60 * 60 * 24) // Dia em que foi adicionada, para referência
) {
    // Para facilitar a ordenação e a comparação de horários
    val timeInMinutes: Int
        get() = hour * 60 + minute
}

// Classe para o relatório diário
data class DailyReport(
    val dateEpochDay: Long, // O dia a que este relatório se refere
    val completedActivities: List<String>, // Nomes das atividades concluídas
    val missedActivities: List<String>     // Nomes das atividades não concluídas
)