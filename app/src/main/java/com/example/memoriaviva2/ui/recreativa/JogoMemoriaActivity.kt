package com.example.memoriaviva2.ui.recreativa

import android.os.Bundle
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.memoriaviva2.R

class JogoMemoriaActivity : AppCompatActivity() {
    
    private lateinit var gridLayout: GridLayout
    private lateinit var scoreText: TextView
    private val cards = mutableListOf<ImageButton>()
    private var firstCard: ImageButton? = null
    private var secondCard: ImageButton? = null
    private var score = 0
    private var matches = 0
    
    // Array de imagens para o jogo da memória
    private val cardImages = arrayOf(
        R.drawable.flor,
        R.drawable.gato,
        R.drawable.arvore,
        R.drawable.sol,
        R.drawable.coracao,
        R.drawable.casainha,
        R.drawable.flor,
        R.drawable.gato,
        R.drawable.arvore,
        R.drawable.sol,
        R.drawable.coracao,
        R.drawable.casainha
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jogo_memoria)
        
        initViews()
        setupGame()
    }
    
    private fun initViews() {
        gridLayout = findViewById(R.id.gridMemoria)
        scoreText = findViewById(R.id.textScore)
        findViewById<android.widget.Button>(R.id.btnNovoJogo).setOnClickListener {
            reiniciarJogo()
        }
        findViewById<android.widget.Button>(R.id.btnVoltarJogo).setOnClickListener {
            finish()
        }
        updateScore()
    }
    
    private fun reiniciarJogo() {
        gridLayout.removeAllViews()
        cards.clear()
        firstCard = null
        secondCard = null
        score = 0
        matches = 0
        setupGame()
    }
    
    private fun setupGame() {
        // Embaralhar as cartas
        cardImages.shuffle()
        
        // Criar botões das cartas
        for (i in cardImages.indices) {
            val card = ImageButton(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 180
                    height = 180
                    setMargins(8, 8, 8, 8)
                }
                setImageResource(android.R.drawable.ic_menu_help) // Carta virada
                scaleType = ImageView.ScaleType.CENTER_CROP
                setPadding(4, 4, 4, 4)
                tag = cardImages[i] // Guardar a imagem real
                setOnClickListener { onCardClick(this) }
            }
            cards.add(card)
            gridLayout.addView(card)
        }
    }
    
    private fun onCardClick(card: ImageButton) {
        // Se a carta já foi virada, ignorar
        if (card.tag == null) return
        
        // Verificar se já está virada
        val cardImage = card.tag as Int
        if (card.drawable?.constantState == getDrawable(cardImage)?.constantState) {
            return
        }
        
        // Virar a carta (mostrar imagem real)
        card.setImageResource(cardImage)
        
        when {
            firstCard == null -> {
                firstCard = card
            }
            secondCard == null && card != firstCard -> {
                secondCard = card
                checkMatch()
            }
        }
    }
    
    private fun checkMatch() {
        val first = firstCard!!
        val second = secondCard!!
        
        if (first.tag == second.tag) {
            // Acertou! Manter cartas viradas
            matches++
            score += 10
            resetSelection()
            
            // Verificar se terminou o jogo
            if (matches == cardImages.size / 2) {
                scoreText.text = "Parabéns! Você completou o jogo!"
            }
        } else {
            // Errou! Virar cartas de volta após delay
            gridLayout.postDelayed({
                first.setImageResource(android.R.drawable.ic_menu_help)
                second.setImageResource(android.R.drawable.ic_menu_help)
                resetSelection()
            }, 1000)
        }
        
        updateScore()
    }
    
    private fun resetSelection() {
        firstCard = null
        secondCard = null
    }
    
    private fun updateScore() {
        scoreText.text = "Pontuação: $score"
    }
}