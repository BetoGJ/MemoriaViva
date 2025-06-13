package com.example.memoriaviva2.model // Ajuste o pacote conforme seu projeto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class DietItem(
    val id: String = UUID.randomUUID().toString(),
    var foodName: String,
    var portion: String,
    var mealTime: String // Ex: "Café da Manhã", "08:00", etc.
) : Parcelable