package com.example.memoriaviva2.ui.medications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriaviva2.R
import com.example.memoriaviva2.ui.backup.Remedio

class MedicationAdapter(
    private val medications: List<Remedio>,
    private val onToggleAlarm: (Remedio) -> Unit,
    private val onRemove: (Remedio) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.medication_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medication = medications[position]
        holder.bind(medication, onToggleAlarm, onRemove)
    }

    override fun getItemCount() = medications.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvDosage: TextView = itemView.findViewById(R.id.tv_dosage)
        private val btnAlarm: Button = itemView.findViewById(R.id.btn_alarm)
        private val btnRemove: Button = itemView.findViewById(R.id.btn_remove)

        fun bind(
            medication: Remedio,
            onToggleAlarm: (Remedio) -> Unit,
            onRemove: (Remedio) -> Unit
        ) {
            tvTime.text = String.format("%02d:%02d", medication.hora, medication.minuto)
            tvName.text = medication.nome
            tvDosage.text = medication.dosagem
            
            btnAlarm.text = if (medication.alarmeAtivo) "🔔" else "🔕"
            btnAlarm.setOnClickListener {
                onToggleAlarm(medication)
            }
            
            btnRemove.setOnClickListener {
                androidx.appcompat.app.AlertDialog.Builder(itemView.context)
                    .setTitle("Remover Remédio")
                    .setMessage("Tem certeza que deseja remover ${medication.nome}?")
                    .setPositiveButton("Remover") { dialog, _ ->
                        onRemove(medication)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancelar") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }
}