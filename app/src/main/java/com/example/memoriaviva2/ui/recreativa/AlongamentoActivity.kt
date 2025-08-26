package com.example.memoriaviva2.ui.recreativa

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.memoriaviva2.R

class AlongamentoActivity : AppCompatActivity() {
    
    private lateinit var imagemAlongamento: ImageView
    private lateinit var instrucaoTexto: TextView
    private lateinit var btnProximo: Button
    private lateinit var btnAnterior: Button
    
    private var exercicioAtual = 0
    
    // Exercícios de alongamento
    private val exercicios = arrayOf(
        ExercicioAlongamento(android.R.drawable.ic_menu_compass, 
            "Alongamento do Pescoço", 
            "Incline suavemente a cabeça para o lado direito. Mantenha por 15 segundos."),
        ExercicioAlongamento(android.R.drawable.ic_menu_gallery, 
            "Alongamento dos Braços", 
            "Estenda o braço direito e puxe suavemente com a mão esquerda. 15 segundos cada braço."),
        ExercicioAlongamento(android.R.drawable.ic_menu_camera, 
            "Alongamento das Costas", 
            "Sentado, gire o tronco suavemente para a direita. Mantenha por 15 segundos.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alongamento)
        
        initViews()
        setupButtons()
        mostrarExercicio()
    }
    
    private fun initViews() {
        imagemAlongamento = findViewById(R.id.imagemAlongamento)
        instrucaoTexto = findViewById(R.id.instrucaoTexto)
        btnProximo = findViewById(R.id.btnProximo)
        btnAnterior = findViewById(R.id.btnAnterior)
    }
    
    private fun setupButtons() {
        btnProximo.setOnClickListener {
            exercicioAtual = (exercicioAtual + 1) % exercicios.size
            mostrarExercicio()
        }
        
        btnAnterior.setOnClickListener {
            exercicioAtual = if (exercicioAtual > 0) exercicioAtual - 1 else exercicios.size - 1
            mostrarExercicio()
        }
    }
    
    private fun mostrarExercicio() {
        val exercicio = exercicios[exercicioAtual]
        imagemAlongamento.setImageResource(exercicio.imagem)
        instrucaoTexto.text = "${exercicio.nome}\n\n${exercicio.instrucao}"
    }
    
    data class ExercicioAlongamento(
        val imagem: Int,
        val nome: String,
        val instrucao: String
    )
}