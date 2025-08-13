package com.example.memoriaviva2.ui.dois

enum class ActivityClassification(val displayName: String) {
    EXERCICIO_FISICO("Exercício físico"),
    ESTIMULACAO_COGNITIVA("Estimulação cognitiva"),
    ALIMENTACAO("Alimentação"),
    SAUDE("Saúde");

    companion object {
        fun fromDisplayName(displayName: String): ActivityClassification? {
            return values().find { it.displayName == displayName }
        }
    }
}