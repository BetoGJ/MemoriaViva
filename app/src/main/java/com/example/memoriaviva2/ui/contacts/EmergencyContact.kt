package com.example.memoriaviva2.ui.contacts

data class EmergencyContact(
    val id: String = java.util.UUID.randomUUID().toString(), // ID único para cada contato
    val name: String,
    val ddd: String,
    val number: String
) {
    // Construtor secundário para facilitar a criação sem DDD (será adicionado automaticamente)
    constructor(name: String, number: String) : this(
        name = name,
        ddd = "", // Será preenchido posteriormente
        number = number
    )

    val fullNumber: String
        get() = "+$ddd$number"
}