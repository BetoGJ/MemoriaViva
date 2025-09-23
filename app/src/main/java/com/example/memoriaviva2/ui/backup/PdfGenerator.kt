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
    private val maxY = pageHeight - margin - 50
    
    private lateinit var document: PdfDocument
    private lateinit var canvas: Canvas
    private lateinit var currentPage: PdfDocument.Page
    private var currentY = 0
    private var pageNumber = 1
    
    fun generatePdf(uri: Uri, comments: String) {
        document = PdfDocument()
        startNewPage()
        
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
        
        drawText("RELATÓRIO MÉDICO - MEMÓRIA VIVA", titlePaint)
        currentY += 40
        
        canvas.drawLine(margin.toFloat(), currentY.toFloat(), (pageWidth - margin).toFloat(), currentY.toFloat(), normalPaint)
        currentY += 30
        
        val sharedPrefs = context.getSharedPreferences(AppPreferencesKeys.PREFS_USER_DATA, Context.MODE_PRIVATE)
        val name = sharedPrefs.getString(AppPreferencesKeys.KEY_USER_NAME, "Informação não notificada") ?: "Informação não notificada"
        val age = sharedPrefs.getInt(AppPreferencesKeys.KEY_USER_AGE, 0)
        val weight = sharedPrefs.getFloat(AppPreferencesKeys.KEY_USER_WEIGHT, 0f)
        
        drawText("DADOS DO PACIENTE", headerPaint)
        currentY += 25
        drawText("Nome: $name", normalPaint)
        drawText("Idade: ${if (age > 0) "$age anos" else "Informação não notificada"}", normalPaint)
        drawText("Peso: ${if (weight > 0) "${weight}kg" else "Informação não notificada"}", normalPaint)
        currentY += 30
        
        drawText("HISTÓRICO MÉDICO", headerPaint)
        currentY += 25
        
        val surgeries = sharedPrefs.getString(AppPreferencesKeys.KEY_USER_RECENT_SURGERIES, "") ?: ""
        val hospitalizations = sharedPrefs.getString(AppPreferencesKeys.KEY_USER_RECENT_HOSPITALIZATIONS, "") ?: ""
        
        drawText("Cirurgias Recentes:", normalPaint)
        drawTextWithIndent(if (surgeries.isNotEmpty()) surgeries else "Informação não notificada", normalPaint)
        currentY += 25
        
        drawText("Internações Recentes:", normalPaint)
        drawTextWithIndent(if (hospitalizations.isNotEmpty()) hospitalizations else "Informação não notificada", normalPaint)
        currentY += 25
        
        val comorbidities = sharedPrefs.getString(AppPreferencesKeys.KEY_USER_COMORBIDITIES, "") ?: ""
        val allergies = sharedPrefs.getString(AppPreferencesKeys.KEY_USER_ALLERGIES, "") ?: ""
        
        drawText("Problemas de Saúde:", normalPaint)
        drawTextWithIndent(if (comorbidities.isNotEmpty()) comorbidities else "Informação não notificada", normalPaint)
        currentY += 25
        
        drawText("Alergias:", normalPaint)
        drawTextWithIndent(if (allergies.isNotEmpty()) allergies else "Informação não notificada", normalPaint)
        currentY += 30
        
        drawText("ROTINA DIÁRIA", headerPaint)
        currentY += 25
        
        try {
            val routineRepository = RoutineRepository(context)
            val routines = routineRepository.getAllActivities()
            
            if (routines.isNotEmpty()) {
                routines.forEach { routine ->
                    drawText("${String.format("%02d:%02d", routine.hour, routine.minute)} - ${routine.name}", normalPaint)
                }
            } else {
                drawText("Informação não notificada", normalPaint)
            }
        } catch (e: Exception) {
            drawText("Informação não notificada", normalPaint)
        }
        currentY += 20
        
        drawText("DIETA", headerPaint)
        currentY += 25
        
        try {
            val dietRepository = DietRepository(context)
            val dietItems = dietRepository.getAllDietItems()
            
            if (dietItems.isNotEmpty()) {
                dietItems.forEach { item ->
                    drawText("• ${item.foodName} - ${item.portion} (${item.mealTime})", normalPaint)
                }
            } else {
                drawText("Informação não notificada", normalPaint)
            }
        } catch (e: Exception) {
            drawText("Informação não notificada", normalPaint)
        }
        currentY += 20
        
        drawText("MEDICAMENTOS", headerPaint)
        currentY += 25
        
        try {
            val medicationRepository = MedicationRepository(context)
            val medications = medicationRepository.getAllMedications()
            
            if (medications.isNotEmpty()) {
                medications.forEach { med ->
                    drawText("• ${med.nome} - ${med.dosagem}", normalPaint)
                    drawTextWithIndent("Horário: ${String.format("%02d:%02d", med.hora, med.minuto)}", normalPaint)
                    currentY += 5
                }
            } else {
                drawText("Informação não notificada", normalPaint)
            }
        } catch (e: Exception) {
            drawText("Informação não notificada", normalPaint)
        }
        currentY += 20
        
        drawText("MONITORAMENTO DE SAÚDE", headerPaint)
        currentY += 25
        
        try {
            val saudePrefs = context.getSharedPreferences("dados_saude", Context.MODE_PRIVATE)
            val saudeJson = saudePrefs.getString("lista_dados", "[]")
            val type = object : com.google.gson.reflect.TypeToken<List<com.example.memoriaviva2.ui.saude.DadosSaude>>() {}.type
            val dadosSaude = com.google.gson.Gson().fromJson<List<com.example.memoriaviva2.ui.saude.DadosSaude>>(saudeJson, type) ?: emptyList()
            
            if (dadosSaude.isNotEmpty()) {
                val hoje = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                val dadosHoje = dadosSaude.filter { it.dataRegistro.startsWith(hoje) }
                val dadosParaMostrar = if (dadosHoje.isNotEmpty()) dadosHoje else dadosSaude.take(1)
                
                dadosParaMostrar.forEach { dados ->
                    drawText("Data: ${dados.dataRegistro}", normalPaint)
                    if (dados.pressaoArterial.isNotEmpty()) {
                        drawTextWithIndent("PA: ${dados.pressaoArterial}", normalPaint)
                    }
                    if (dados.frequenciaCardiaca.isNotEmpty()) {
                        drawTextWithIndent("FC: ${dados.frequenciaCardiaca} bpm", normalPaint)
                    }
                    if (dados.temperatura.isNotEmpty()) {
                        drawTextWithIndent("Temp: ${dados.temperatura}°C", normalPaint)
                    }
                    if (dados.peso.isNotEmpty() && dados.imc.isNotEmpty()) {
                        drawTextWithIndent("Peso: ${dados.peso}kg | IMC: ${dados.imc}", normalPaint)
                    }
                    if (dados.queixas.isNotEmpty()) {
                        drawTextWithIndent("Queixas: ${dados.queixas}", normalPaint)
                    }
                    currentY += 10
                }
            } else {
                drawText("Informação não notificada", normalPaint)
            }
        } catch (e: Exception) {
            drawText("Informação não notificada", normalPaint)
        }
        currentY += 20
        
        drawText("CONTATOS DE EMERGÊNCIA", headerPaint)
        currentY += 25
        
        try {
            val contactRepository = com.example.memoriaviva2.ui.contacts.EmergencyContactRepository(context)
            val contacts = contactRepository.getEmergencyContacts()
            
            if (contacts.isNotEmpty()) {
                contacts.forEach { contact ->
                    drawText("• ${contact.name} - ${contact.fullNumber}", normalPaint)
                    if (contact.description.isNotEmpty()) {
                        drawTextWithIndent(contact.description, normalPaint)
                    }
                    currentY += 5
                }
            } else {
                drawText("Informação não notificada", normalPaint)
            }
        } catch (e: Exception) {
            drawText("Informação não notificada", normalPaint)
        }
        currentY += 20
        
        if (comments.isNotEmpty()) {
            drawText("COMENTÁRIOS", headerPaint)
            currentY += 25
            drawWrappedText(comments, normalPaint)
        }
        
        currentY += 30
        canvas.drawLine(margin.toFloat(), currentY.toFloat(), (pageWidth - margin).toFloat(), currentY.toFloat(), normalPaint)
        currentY += 20
        drawText("Relatório gerado em: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}", normalPaint)
        
        finishCurrentPage()
        
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
    
    private fun startNewPage() {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        currentPage = document.startPage(pageInfo)
        canvas = currentPage.canvas
        currentY = margin + 50
    }
    
    private fun finishCurrentPage() {
        document.finishPage(currentPage)
    }
    
    private fun checkPageSpace(neededSpace: Int = lineHeight) {
        if (currentY + neededSpace > maxY) {
            finishCurrentPage()
            pageNumber++
            startNewPage()
        }
    }
    
    private fun drawText(text: String, paint: Paint) {
        checkPageSpace()
        canvas.drawText(text, margin.toFloat(), currentY.toFloat(), paint)
        currentY += lineHeight
    }
    
    private fun drawTextWithIndent(text: String, paint: Paint) {
        checkPageSpace()
        canvas.drawText(text, (margin + 20).toFloat(), currentY.toFloat(), paint)
        currentY += lineHeight
    }
    
    private fun drawWrappedText(text: String, paint: Paint) {
        val words = text.split(" ")
        var currentLine = ""
        
        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val textWidth = paint.measureText(testLine)
            
            if (textWidth > pageWidth - 2 * margin) {
                if (currentLine.isNotEmpty()) {
                    drawText(currentLine, paint)
                }
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        
        if (currentLine.isNotEmpty()) {
            drawText(currentLine, paint)
        }
    }
}