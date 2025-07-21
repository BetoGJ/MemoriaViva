package com.example.memoriaviva2.dois // Ou seu pacote correspondente

enum class ActivityClassification(val displayName: String) {
    PHYSICAL_EXERCISE("Exercício físico"),
    COGNITIVE_STIMULATION("Estimulação cognitiva"),
    NUTRITION("Alimentação"),
    HEALTH("Saúde");

    companion object {
        fun fromDisplayName(displayName: String): ActivityClassification? {
            return values().find { it.displayName == displayName }
        }
    }
}