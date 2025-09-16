// NotificationReceiver.kt
package com.example.memoriaviva2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nomeRemedio = intent.getStringExtra("remedio") ?: "Remédio"

        val notificacao = NotificationCompat.Builder(context, "canal_remedios")
            .setSmallIcon(R.drawable.ic_call)
            .setContentTitle("Hora do remédio!")
            .setContentText("Tome seu remédio: $nomeRemedio")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(1001, notificacao)
            }
        } catch (e: SecurityException) {
            // Android 13+ notification permission not granted
        }
    }
}
