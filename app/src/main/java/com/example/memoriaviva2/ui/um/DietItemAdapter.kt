package com.example.memoriaviva2.ui // Ajuste o pacote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriaviva2.R
import com.example.memoriaviva2.model.DietItem

class DietItemAdapter(
    private var dietItems: MutableList<DietItem>,
    private val onRemoveClickListener: (DietItem) -> Unit
) : RecyclerView.Adapter<DietItemAdapter.DietItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DietItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_diet, parent, false) // Certifique-se que R.layout.item_diet existe
        return DietItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: DietItemViewHolder, position: Int) {
        holder.bind(dietItems[position])
    }

    override fun getItemCount(): Int = dietItems.size

    fun updateDietItems(newDietItems: List<DietItem>) {
        dietItems.clear()
        dietItems.addAll(newDietItems)
        notifyDataSetChanged()
    }

    inner class DietItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodNameTextView: TextView = itemView.findViewById(R.id.textViewFoodName)
        private val portionTextView: TextView = itemView.findViewById(R.id.textViewPortion)
        private val mealTimeTextView: TextView = itemView.findViewById(R.id.textViewMealTime)
        private val removeButton: ImageButton = itemView.findViewById(R.id.buttonRemoveDietItem)

        fun bind(dietItem: DietItem) {
            foodNameTextView.text = dietItem.foodName
            portionTextView.text = "Porção: ${dietItem.portion}"
            mealTimeTextView.text = "Horário: ${dietItem.mealTime}"
            removeButton.setOnClickListener { onRemoveClickListener(dietItem) }
        }
    }
}