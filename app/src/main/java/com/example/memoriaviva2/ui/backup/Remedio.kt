package com.example.memoriaviva2.ui.backup

import java.util.UUID

data class Remedio(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val dosagem: String,
    val hora: Int,
    val minuto: Int,
    val alarmeAtivo: Boolean = false
)