package com.example.memoriaviva2.ui.contacts

data class EmergencyContact(
    val id: String = java.util.UUID.randomUUID().toString(), // ID único para cada contato
    val name: String,
    val ddd: String,
    val number: String,
    val description: String = "" // Descrição do contato
) {
    // Construtor secundário para facilitar a criação sem DDD (será adicionado automaticamente)
    constructor(name: String, number: String) : this(
        name = name,
        ddd = "", // Será preenchido posteriormente
        number = number,
        description = ""
    )
    
    // Construtor com descrição
    constructor(name: String, number: String, description: String) : this(
        name = name,
        ddd = "",
        number = number,
        description = description
    )

    val fullNumber: String
        get() = "+$ddd$number"
}