package com.example.memoriaviva2.ui.recreativa

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.memoriaviva2.R

class AlbumMemoriasActivity : AppCompatActivity() {
    
    private lateinit var imagemMemoria: ImageView
    private lateinit var legendaTexto: TextView
    private lateinit var btnProxima: Button
    private lateinit var btnAnterior: Button
    
    private var memoriaAtual = 0
    
    // Memórias de exemplo (usar ícones como placeholder)
    private val memorias = arrayOf(
        Memoria(android.R.drawable.ic_menu_gallery, "Família reunida no Natal de 2023"),
        Memoria(android.R.drawable.ic_menu_camera, "Passeio no parque com os netos"),
        Memoria(android.R.drawable.ic_menu_compass, "Viagem para a praia no verão"),
        Memoria(android.R.drawable.ic_menu_call, "Aniversário de 80 anos - festa especial")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_memorias)
        
        initViews()
        setupButtons()
        mostrarMemoria()
    }
    
    private fun initViews() {
        imagemMemoria = findViewById(R.id.imagemMemoria)
        legendaTexto = findViewById(R.id.legendaTexto)
        btnProxima = findViewById(R.id.btnProxima)
        btnAnterior = findViewById(R.id.btnAnterior)
    }
    
    private fun setupButtons() {
        btnProxima.setOnClickListener {
            memoriaAtual = (memoriaAtual + 1) % memorias.size
            mostrarMemoria()
        }
        
        btnAnterior.setOnClickListener {
            memoriaAtual = if (memoriaAtual > 0) memoriaAtual - 1 else memorias.size - 1
            mostrarMemoria()
        }
    }
    
    private fun mostrarMemoria() {
        val memoria = memorias[memoriaAtual]
        imagemMemoria.setImageResource(memoria.imagem)
        legendaTexto.text = memoria.legenda
    }
    
    data class Memoria(
        val imagem: Int,
        val legenda: String
    )
}