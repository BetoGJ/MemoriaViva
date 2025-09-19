package com.example.memoriaviva2.ui.backup

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.memoriaviva2.AppPreferencesKeys
import com.example.memoriaviva2.ui.dois.RoutineRepository
import com.example.memoriaviva2.data.DietRepository
import com.example.memoriaviva2.ui.medications.MedicationRepository
import java.io.IOException

class PdfGenerator(private val context: Context) {
    
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 50
    private val lineHeight = 20
    
    fun generatePdf(uri: Uri, comments: String) {
        val document = PdfDocument()
        var currentY = margin + 50
        
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        // Configurar estilos de texto
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isFakeBoldText = true
        }
        
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            isFakeBoldText = true
        }
        
        val normalPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
        }
        
        // Título do documento
        canvas.drawText("RELATÓRIO MÉDICO - MEMÓRIA VIVA", margin.toFloat(), currentY.toFloat(), titlePaint)
        currentY += 40
        
        // Linha separadora
        canvas.drawLine(margin.toFloat(), currentY.toFloat(), (pageWidth - margin).toFloat(), currentY.toFloat(), normalPaint)
        currentY += 30
        
        // Informações do paciente
        val sharedPrefs = context.getSharedPreferences(AppPreferencesKeys.PREFS_USER_DATA, Context.MODE_PRIVATE)
        val name = sharedPrefs.getString(AppPreferencesKeys.KEY_USER_NAME, "Informação não notificada") ?: "Informação não notificada"
        val age = sharedPrefs.getInt(AppPreferencesKeys.KEY_USER_AGE, 0)
        val weight = sharedPrefs.getFloat(AppPreferencesKeys.KEY_USER_WEIGHT, 0f)
        
        canvas.drawText("DADOS DO PACIENTE", margin.toFloat(), currentY.toFloat(), headerPaint)
        currentY += 25
        canvas.drawText("Nome: $name", margin.toFloat(), currentY.toFloat(), normalPaint)
        currentY += lineHeight
        canvas.drawText("Idade: ${if (age > 0) "$age anos" else "Informação não notificada"}", margin.toFloat(), currentY.toFloat(), normalPaint)
        currentY += lineHeight
        canvas.drawText("Peso: ${if (weight > 0) "${weight}kg" else "Informação não notificada"}", margin.toFloat(), currentY.toFloat(), normalPaint)
        currentY += 30
        
        // Cirurgias e internações
        canvas.drawText("HISTÓRICO MÉDICO", margin.toFloat(), currentY.toFloat(), headerPaint)
        currentY += 25
        
        val surgeries = sharedPrefs.getString(AppPreferencesKeys.KEY_USER_RECENT_SURGERIES, "") ?: ""
        val hospitalizations = sharedPrefs.getString(AppPreferencesKeys.KEY_USER_RECENT_HOSPITALIZATIONS, "") ?: ""
        
        canvas.drawText("Cirurgias Recentes:", margin.toFloat(), currentY.toFloat(), normalPaint)
        currentY += lineHeight
        canvas.drawText(if (surgeries.isNotEmpty()) surgeries else "Informação não notificada", margin + 20.toFloat(), currentY.toFloat(), normalPaint)
        currentY += 25
        
        canvas.drawText("Internações Recentes:", margin.toFloat(), currentY.toFloat(), normalPaint)
        currentY += lineHeight
        canvas.drawText(if (hospitalizations.isNotEmpty()) hospitalizations else "Informação não notificada", margin + 20.toFloat(), currentY.toFloat(), normalPaint)
        currentY += 30
        
        // Rotina
        canvas.drawText("ROTINA DIÁRIA", margin.toFloat(), currentY.toFloat(), headerPaint)
        currentY += 25
        
        try {
            val routineRepository = RoutineRepository(context)
            val routines = routineRepository.getAllActivities()
            
            if (routines.isNotEmpty()) {
                routines.forEach { routine ->
                    canvas.drawText("${String.format("%02d:%02d", routine.hour, routine.minute)} - ${routine.name}", margin.toFloat(), currentY.toFloat(), normalPaint)
                    currentY += lineHeight
                }
            } else {
                canvas.drawText("Informação não notificada", margin.toFloat(), currentY.toFloat(), normalPaint)
                currentY += lineHeight
            }
        } catch (e: Exception) {
            canvas.drawText("Informação não notificada", margin.toFloat(), currentY.toFloat(), normalPaint)
            currentY += lineHeight
        }
        currentY += 20
        
        // Dieta
        canvas.drawText("DIETA", margin.toFloat(), currentY.toFloat(), headerPaint)
        currentY += 25
        
        try {
            val dietRepository = DietRepository(context)
            val dietItems = dietRepository.getAllDietItems()
            
            if (dietItems.isNotEmpty()) {
                dietItems.forEach { item ->
                    canvas.drawText("• ${item.foodName} - ${item.portion} (${item.mealTime})", margin.toFloat(), currentY.toFloat(), normalPaint)
                    currentY += lineHeight
                }
            } else {
                canvas.drawText("Informação não notificada", margin.toFloat(), currentY.toFloat(), normalPaint)
                currentY += lineHeight
            }
        } catch (e: Exception) {
            canvas.drawText("Informação não notificada", margin.toFloat(), currentY.toFloat(), normalPaint)
            currentY += lineHeight
        }
        currentY += 20
        
        // Medicamentos
        canvas.drawText("MEDICAMENTOS", margin.toFloat(), currentY.toFloat(), headerPaint)
        currentY += 25
        
        try {
            val medicationRepository = MedicationRepository(context)
            val medications = medicationRepository.getAllMedications()
            
            if (medications.isNotEmpty()) {
                medications.forEach { med ->
                    canvas.drawText("• ${med.nome} - ${med.dosagem}", margin.toFloat(), currentY.toFloat(), normalPaint)
                    currentY += lineHeight
                    canvas.drawText("  Horário: ${String.format("%02d:%02d", med.hora, med.minuto)}", margin + 20.toFloat(), currentY.toFloat(), normalPaint)
                    currentY += lineHeight + 5
                }
            } else {
                canvas.drawText("Informação não notificada", margin.toFloat(), currentY.toFloat(), normalPaint)
                currentY += lineHeight
            }
        } catch (e: Exception) {
            canvas.drawText("Informação não notificada", margin.toFloat(), currentY.toFloat(), normalPaint)
            currentY += lineHeight
        }
        currentY += 20
        
        // Comentários
        if (comments.isNotEmpty()) {
            canvas.drawText("COMENTÁRIOS", margin.toFloat(), currentY.toFloat(), headerPaint)
            currentY += 25
            
            // Quebrar texto em linhas
            val words = comments.split(" ")
            var currentLine = ""
            
            words.forEach { word ->
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val textWidth = normalPaint.measureText(testLine)
                
                if (textWidth > pageWidth - 2 * margin) {
                    canvas.drawText(currentLine, margin.toFloat(), currentY.toFloat(), normalPaint)
                    currentY += lineHeight
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, margin.toFloat(), currentY.toFloat(), normalPaint)
                currentY += lineHeight
            }
        }
        
        // Data de geração
        currentY += 30
        canvas.drawLine(margin.toFloat(), currentY.toFloat(), (pageWidth - margin).toFloat(), currentY.toFloat(), normalPaint)
        currentY += 20
        canvas.drawText("Relatório gerado em: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}", margin.toFloat(), currentY.toFloat(), normalPaint)
        
        document.finishPage(page)
        
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                document.writeTo(outputStream)
            }
        } catch (e: IOException) {
            throw e
        } finally {
            document.close()
        }
    }
}