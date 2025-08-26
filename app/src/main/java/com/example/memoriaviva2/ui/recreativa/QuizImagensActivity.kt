package com.example.memoriaviva2.ui.recreativa

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.memoriaviva2.R

class QuizImagensActivity : AppCompatActivity() {
    
    private lateinit var imagemQuiz: ImageView
    private lateinit var perguntaTexto: TextView
    private lateinit var btnOpcao1: Button
    private lateinit var btnOpcao2: Button
    private lateinit var btnOpcao3: Button
    private lateinit var scoreText: TextView
    
    private var perguntaAtual = 0
    private var pontuacao = 0
    
    // Dados do quiz (imagem, pergunta, opções, resposta correta)
    private val perguntas = arrayOf(
        QuizItem(android.R.drawable.ic_menu_camera, "O que é isto?", 
                arrayOf("Câmera", "Telefone", "Rádio"), 0),
        QuizItem(android.R.drawable.ic_menu_call, "Para que serve?", 
                arrayOf("Fotografar", "Ligar", "Ouvir música"), 1),
        QuizItem(android.R.drawable.ic_menu_gallery, "O que representa?", 
                arrayOf("Galeria", "Calendário", "Calculadora"), 0)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_imagens)
        
        initViews()
        setupButtons()
        mostrarPergunta()
    }
    
    private fun initViews() {
        imagemQuiz = findViewById(R.id.imagemQuiz)
        perguntaTexto = findViewById(R.id.perguntaTexto)
        btnOpcao1 = findViewById(R.id.btnOpcao1)
        btnOpcao2 = findViewById(R.id.btnOpcao2)
        btnOpcao3 = findViewById(R.id.btnOpcao3)
        scoreText = findViewById(R.id.scoreText)
        findViewById<Button>(R.id.btnVoltarQuiz).setOnClickListener {
            finish()
        }
    }
    
    private fun setupButtons() {
        btnOpcao1.setOnClickListener { verificarResposta(0) }
        btnOpcao2.setOnClickListener { verificarResposta(1) }
        btnOpcao3.setOnClickListener { verificarResposta(2) }
    }
    
    private fun mostrarPergunta() {
        val pergunta = perguntas[perguntaAtual]
        imagemQuiz.setImageResource(pergunta.imagem)
        perguntaTexto.text = pergunta.pergunta
        perguntaTexto.setTextColor(getColor(android.R.color.black))
        
        btnOpcao1.text = pergunta.opcoes[0]
        btnOpcao2.text = pergunta.opcoes[1]
        btnOpcao3.text = pergunta.opcoes[2]
        
        // Resetar cores dos botões
        btnOpcao1.setBackgroundColor(getColor(R.color.purple_200))
        btnOpcao2.setBackgroundColor(getColor(R.color.purple_200))
        btnOpcao3.setBackgroundColor(getColor(R.color.purple_200))
        
        scoreText.text = "Pontuação: $pontuacao"
    }
    
    private fun verificarResposta(opcaoSelecionada: Int) {
        val pergunta = perguntas[perguntaAtual]
        
        // Desabilitar botões temporariamente
        btnOpcao1.isEnabled = false
        btnOpcao2.isEnabled = false
        btnOpcao3.isEnabled = false
        
        if (opcaoSelecionada == pergunta.respostaCorreta) {
            pontuacao += 10
            
            // Destacar resposta correta em verde
            when (opcaoSelecionada) {
                0 -> btnOpcao1.setBackgroundColor(getColor(android.R.color.holo_green_light))
                1 -> btnOpcao2.setBackgroundColor(getColor(android.R.color.holo_green_light))
                2 -> btnOpcao3.setBackgroundColor(getColor(android.R.color.holo_green_light))
            }
            
            perguntaTexto.text = "✅ CORRETO! Parabéns!"
            perguntaTexto.setTextColor(getColor(android.R.color.holo_green_dark))
            
        } else {
            // Destacar resposta errada em vermelho
            when (opcaoSelecionada) {
                0 -> btnOpcao1.setBackgroundColor(getColor(android.R.color.holo_red_light))
                1 -> btnOpcao2.setBackgroundColor(getColor(android.R.color.holo_red_light))
                2 -> btnOpcao3.setBackgroundColor(getColor(android.R.color.holo_red_light))
            }
            
            // Mostrar resposta correta em verde
            when (pergunta.respostaCorreta) {
                0 -> btnOpcao1.setBackgroundColor(getColor(android.R.color.holo_green_light))
                1 -> btnOpcao2.setBackgroundColor(getColor(android.R.color.holo_green_light))
                2 -> btnOpcao3.setBackgroundColor(getColor(android.R.color.holo_green_light))
            }
            
            perguntaTexto.text = "❌ ERRADO! A resposta correta é: ${pergunta.opcoes[pergunta.respostaCorreta]}"
            perguntaTexto.setTextColor(getColor(android.R.color.holo_red_dark))
        }
        
        // Avançar para próxima pergunta após 3 segundos
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            perguntaAtual = (perguntaAtual + 1) % perguntas.size
            mostrarPergunta()
            
            // Reabilitar botões
            btnOpcao1.isEnabled = true
            btnOpcao2.isEnabled = true
            btnOpcao3.isEnabled = true
        }, 3000)
    }
    
    data class QuizItem(
        val imagem: Int,
        val pergunta: String,
        val opcoes: Array<String>,
        val respostaCorreta: Int
    )
}