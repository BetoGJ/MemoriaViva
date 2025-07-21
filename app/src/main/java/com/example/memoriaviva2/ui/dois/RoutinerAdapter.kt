package com.example.memoriaviva2.dois

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriaviva2.R // Importe o R do seu módulo app

class RoutineAdapter(
    private val onItemClick: (RoutineActivity) -> Unit,
    private val onDoneToggle: (RoutineActivity, Boolean) -> Unit
) : ListAdapter<RoutineActivity, RoutineAdapter.RoutineViewHolder>(RoutineDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_routine_activity, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val activity = getItem(position)
        holder.bind(activity, onItemClick, onDoneToggle)
    }

    class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime: TextView = itemView.findViewById(R.id.tv_routine_time)
        private val tvName: TextView = itemView.findViewById(R.id.tv_routine_name)
        private val tvClassification: TextView = itemView.findViewById(R.id.tv_routine_classification)
        private val cbIsDone: CheckBox = itemView.findViewById(R.id.cb_routine_is_done)

        fun bind(
            activity: RoutineActivity,
            onItemClick: (RoutineActivity) -> Unit,
            onDoneToggle: (RoutineActivity, Boolean) -> Unit
        ) {
            tvTime.text = String.format("%02d:%02d", activity.hour, activity.minute)
            tvName.text = activity.name
            tvClassification.text = activity.classification.displayName

            // Configurar o CheckBox sem disparar o listener durante a configuração inicial
            cbIsDone.setOnCheckedChangeListener(null)
            cbIsDone.isChecked = activity.isDone
            cbIsDone.setOnCheckedChangeListener { _, isChecked ->
                onDoneToggle(activity, isChecked)
            }

            itemView.setOnClickListener {
                onItemClick(activity)
            }
        }
    }

    class RoutineDiffCallback : DiffUtil.ItemCallback<RoutineActivity>() {
        override fun areItemsTheSame(oldItem: RoutineActivity, newItem: RoutineActivity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RoutineActivity, newItem: RoutineActivity): Boolean {
            return oldItem == newItem // Data class compara todos os campos
        }
    }
}
