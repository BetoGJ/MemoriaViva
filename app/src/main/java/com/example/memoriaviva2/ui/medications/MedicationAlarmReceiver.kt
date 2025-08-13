package com.example.memoriaviva2.ui.medications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.widget.Toast

class MedicationAlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val medicationName = intent.getStringExtra("medication_name") ?: "Remédio"
        val medicationDosage = intent.getStringExtra("medication_dosage") ?: ""
        
        // Toca o som do alarme
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone = RingtoneManager.getRingtone(context, notification)
            ringtone.play()
        } catch (e: Exception) {
            // Se não conseguir tocar o alarme, mostra apenas o toast
        }
        
        // Mostra notificação
        Toast.makeText(
            context, 
            "Hora do remédio: $medicationName - $medicationDosage", 
            Toast.LENGTH_LONG
        ).show()
    }
}