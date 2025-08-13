package com.example.memoriaviva2.ui.backup

import com.example.memoriaviva2.ui.contacts.EmergencyContact
import com.example.memoriaviva2.ui.dois.SimpleActivity
import com.example.memoriaviva2.model.DietItem

data class BackupData(
    val contatos: List<EmergencyContact> = emptyList(),
    val remedios: List<Remedio> = emptyList(),
    val rotina: List<SimpleActivity> = emptyList(),
    val dieta: List<DietItem> = emptyList(),
    val registro: RegistroData? = null,
    val backupVersion: String = "1.0",
    val timestamp: Long = System.currentTimeMillis()
)



data class RegistroData(
    val nome: String,
    val idade: Int,
    val peso: Float,
    val cirurgiasRecentes: String,
    val internacoes: String
)