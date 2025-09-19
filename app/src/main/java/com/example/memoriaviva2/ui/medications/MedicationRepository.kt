package com.example.memoriaviva2.ui.medications

import android.content.Context
import android.content.SharedPreferences
import com.example.memoriaviva2.ui.backup.Remedio
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MedicationRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MemoriaVivaMedicationsPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val medicationsKey = "medications_list"

    fun saveMedications(medications: List<Remedio>) {
        val jsonMedications = gson.toJson(medications)
        sharedPreferences.edit().putString(medicationsKey, jsonMedications).apply()
    }

    fun getMedications(): MutableList<Remedio> {
        val jsonMedications = sharedPreferences.getString(medicationsKey, null)
        return if (jsonMedications != null) {
            val type = object : TypeToken<MutableList<Remedio>>() {}.type
            gson.fromJson(jsonMedications, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }
    }

    fun addMedication(medication: Remedio) {
        val medications = getMedications()
        medications.removeAll { it.id == medication.id }
        medications.add(medication)
        saveMedications(medications)
    }

    fun removeMedication(medicationId: String) {
        val medications = getMedications()
        medications.removeAll { it.id == medicationId }
        saveMedications(medications)
    }

    fun updateMedication(updatedMedication: Remedio) {
        val medications = getMedications()
        val index = medications.indexOfFirst { it.id == updatedMedication.id }
        if (index != -1) {
            medications[index] = updatedMedication
            saveMedications(medications)
        }
    }
    
    fun getAllMedications(): List<Remedio> {
        return getMedications()
    }
}