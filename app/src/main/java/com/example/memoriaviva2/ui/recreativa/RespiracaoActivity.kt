package com.example.memoriaviva2.ui.recreativa

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.memoriaviva2.R

class RespiracaoActivity : AppCompatActivity() {
    
    private lateinit var circuloRespiracao: View
    private lateinit var textoInstrucao: TextView
    private lateinit var btnIniciar: Button
    private lateinit var btnParar: Button
    
    private var isRunning = false
    private val handler = Handler(Looper.getMainLooper())
    private var cicloAtual = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_respiracao)
        
        initViews()
        setupButtons()
    }
    
    private fun initViews() {
        circuloRespiracao = findViewById(R.id.circuloRespiracao)
        textoInstrucao = findViewById(R.id.textoInstrucao)
        btnIniciar = findViewById(R.id.btnIniciar)
        btnParar = findViewById(R.id.btnParar)
        findViewById<Button>(R.id.btnVoltarRespiracao).setOnClickListener {
            finish()
        }
    }
    
    private fun setupButtons() {
        btnIniciar.setOnClickListener {
            iniciarRespiracao()
        }
        
        btnParar.setOnClickListener {
            pararRespiracao()
        }
    }
    
    private fun iniciarRespiracao() {
        isRunning = true
        cicloAtual = 0
        btnIniciar.isEnabled = false
        btnParar.isEnabled = true
        
        executarCicloRespiracao()
    }
    
    private fun executarCicloRespiracao() {
        if (!isRunning) return
        
        // Fase de inspiração (4 segundos)
        textoInstrucao.text = "Inspire... 🌬️"
        animarCirculo(300f, 4000) // Cresce para 300dp em 4 segundos
        
        handler.postDelayed({
            if (!isRunning) return@postDelayed
            
            // Fase de expiração (4 segundos)
            textoInstrucao.text = "Expire... 💨"
            animarCirculo(150f, 4000) // Volta para 150dp em 4 segundos
            
            handler.postDelayed({
                if (!isRunning) return@postDelayed
                
                cicloAtual++
                textoInstrucao.text = "Ciclo $cicloAtual completado ✨"
                
                // Continuar próximo ciclo após 1 segundo
                handler.postDelayed({
                    if (isRunning) executarCicloRespiracao()
                }, 1000)
                
            }, 4000)
        }, 4000)
    }
    
    private fun animarCirculo(tamanho: Float, duracao: Long) {
        val animatorX = ObjectAnimator.ofFloat(circuloRespiracao, "scaleX", tamanho / 150f)
        val animatorY = ObjectAnimator.ofFloat(circuloRespiracao, "scaleY", tamanho / 150f)
        
        animatorX.duration = duracao
        animatorY.duration = duracao
        
        animatorX.start()
        animatorY.start()
    }
    
    private fun pararRespiracao() {
        isRunning = false
        btnIniciar.isEnabled = true
        btnParar.isEnabled = false
        textoInstrucao.text = "Pressione 'Iniciar' para começar"
        
        // Resetar círculo
        circuloRespiracao.scaleX = 1f
        circuloRespiracao.scaleY = 1f
    }
    
    override fun onDestroy() {
        super.onDestroy()
        pararRespiracao()
    }
}