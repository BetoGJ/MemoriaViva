package com.example.memoriaviva2.ui.backup

import android.content.Context
import android.content.SharedPreferences
import com.example.memoriaviva2.AppPreferencesKeys
import com.example.memoriaviva2.ui.contacts.EmergencyContact
import com.example.memoriaviva2.ui.contacts.EmergencyContactRepository


import com.example.memoriaviva2.model.DietItem
import com.example.memoriaviva2.data.DietRepository
import com.example.memoriaviva2.ui.medications.MedicationRepository
import com.example.memoriaviva2.ui.saude.DadosSaude
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BackupManager {

    fun createBackup(context: Context): BackupData {
        val sharedPrefs = context.getSharedPreferences(AppPreferencesKeys.PREFS_USER_DATA, Context.MODE_PRIVATE)
        
        // Get contacts
        val contactRepo = EmergencyContactRepository(context)
        val contatos = contactRepo.getEmergencyContacts()
        

        
        // Get diet items
        val dietRepo = DietRepository(context)
        val dieta = dietRepo.getDietItems()
        
        // Get medications from SharedPreferences directly
        val medicationPrefs = context.getSharedPreferences("MemoriaVivaMedicationsPrefs", Context.MODE_PRIVATE)
        val remediosJson = medicationPrefs.getString("medications_list", "[]")
        val remedios = try {
            val type = object : TypeToken<List<Remedio>>() {}.type
            Gson().fromJson<List<Remedio>>(remediosJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList<Remedio>()
        }
        
        // Get routine activities
        val routinePrefs = context.getSharedPreferences("routine_prefs", Context.MODE_PRIVATE)
        val rotinaJson = routinePrefs.getString("activities", "[]")
        val rotina = try {
            val type = object : TypeToken<List<com.example.memoriaviva2.ui.dois.SimpleActivity>>() {}.type
            Gson().fromJson<List<com.example.memoriaviva2.ui.dois.SimpleActivity>>(rotinaJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList<com.example.memoriaviva2.ui.dois.SimpleActivity>()
        }
        
        // Get health data
        val saudePrefs = context.getSharedPreferences("dados_saude", Context.MODE_PRIVATE)
        val saudeJson = saudePrefs.getString("lista_dados", "[]")
        val dadosSaude = try {
            val type = object : TypeToken<List<DadosSaude>>() {}.type
            Gson().fromJson<List<DadosSaude>>(saudeJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList<DadosSaude>()
        }
        
        // Get user registration data
        val registro = if (sharedPrefs.getBoolean(AppPreferencesKeys.KEY_IS_USER_REGISTERED, false)) {
            RegistroData(
                nome = sharedPrefs.getString(AppPreferencesKeys.KEY_USER_NAME, "") ?: "",
                idade = sharedPrefs.getInt(AppPreferencesKeys.KEY_USER_AGE, 0),
                peso = sharedPrefs.getFloat(AppPreferencesKeys.KEY_USER_WEIGHT, 0f),
                cirurgiasRecentes = sharedPrefs.getString(AppPreferencesKeys.KEY_USER_RECENT_SURGERIES, "") ?: "",
                internacoes = sharedPrefs.getString(AppPreferencesKeys.KEY_USER_RECENT_HOSPITALIZATIONS, "") ?: "",
                comorbidades = sharedPrefs.getString(AppPreferencesKeys.KEY_USER_COMORBIDITIES, "") ?: "",
                alergias = sharedPrefs.getString(AppPreferencesKeys.KEY_USER_ALLERGIES, "") ?: ""
            )
        } else null
        
        return BackupData(
            contatos = contatos,
            remedios = remedios,
            rotina = rotina,
            dieta = dieta,
            dadosSaude = dadosSaude,
            registro = registro
        )
    }
    
    fun restoreBackup(context: Context, backupData: BackupData) {
        val sharedPrefs = context.getSharedPreferences(AppPreferencesKeys.PREFS_USER_DATA, Context.MODE_PRIVATE)
        
        // Clear and restore contacts
        val contactRepo = EmergencyContactRepository(context)
        contactRepo.saveEmergencyContacts(backupData.contatos)
        

        
        // Clear and restore diet items
        val dietRepo = DietRepository(context)
        dietRepo.saveDietItems(emptyList()) // Clear existing
        for (dietItem in backupData.dieta) {
            dietRepo.addDietItem(dietItem)
        }
        
        // Restore medications directly to SharedPreferences (with alarms disabled)
        val medicationPrefs = context.getSharedPreferences("MemoriaVivaMedicationsPrefs", Context.MODE_PRIVATE)
        val remediosWithoutAlarms = backupData.remedios.map { it.copy(alarmeAtivo = false) }
        val remediosJson = Gson().toJson(remediosWithoutAlarms)
        medicationPrefs.edit().putString("medications_list", remediosJson).apply()
        
        // Restore routine activities
        val routinePrefs = context.getSharedPreferences("routine_prefs", Context.MODE_PRIVATE)
        val rotinaJson = Gson().toJson(backupData.rotina)
        routinePrefs.edit().putString("activities", rotinaJson).apply()
        
        // Restore health data
        val saudePrefs = context.getSharedPreferences("dados_saude", Context.MODE_PRIVATE)
        val saudeJson = Gson().toJson(backupData.dadosSaude)
        saudePrefs.edit().putString("lista_dados", saudeJson).apply()
        
        // Restore user registration
        backupData.registro?.let { registro ->
            with(sharedPrefs.edit()) {
                putBoolean(AppPreferencesKeys.KEY_IS_USER_REGISTERED, true)
                putString(AppPreferencesKeys.KEY_USER_NAME, registro.nome)
                putInt(AppPreferencesKeys.KEY_USER_AGE, registro.idade)
                putFloat(AppPreferencesKeys.KEY_USER_WEIGHT, registro.peso)
                putString(AppPreferencesKeys.KEY_USER_RECENT_SURGERIES, registro.cirurgiasRecentes)
                putString(AppPreferencesKeys.KEY_USER_RECENT_HOSPITALIZATIONS, registro.internacoes)
                putString(AppPreferencesKeys.KEY_USER_COMORBIDITIES, registro.comorbidades)
                putString(AppPreferencesKeys.KEY_USER_ALLERGIES, registro.alergias)
                apply()
            }
        }
    }
}