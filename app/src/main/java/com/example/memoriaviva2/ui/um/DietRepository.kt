package com.example.memoriaviva2.data // Ajuste o pacote

import android.content.Context
import android.content.SharedPreferences
import com.example.memoriaviva2.model.DietItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DietRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MemoriaVivaDietPrefs_UmFragment", Context.MODE_PRIVATE) // Nome pode ser específico
    private val gson = Gson()
    private val dietItemsKey = "diet_items_list_umfragment"

    fun saveDietItems(dietItems: List<DietItem>) {
        val jsonDietItems = gson.toJson(dietItems)
        sharedPreferences.edit().putString(dietItemsKey, jsonDietItems).apply()
    }

    fun getDietItems(): MutableList<DietItem> {
        val jsonDietItems = sharedPreferences.getString(dietItemsKey, null)
        return if (jsonDietItems != null) {
            val type = object : TypeToken<MutableList<DietItem>>() {}.type
            gson.fromJson(jsonDietItems, type)
        } else {
            mutableListOf()
        }
    }

    fun addDietItem(dietItem: DietItem) {
        val dietItems = getDietItems()
        dietItems.add(dietItem)
        saveDietItems(dietItems)
    }

    fun removeDietItem(dietItemId: String) {
        val dietItems = getDietItems()
        dietItems.removeAll { it.id == dietItemId }
        saveDietItems(dietItems)
    }
}