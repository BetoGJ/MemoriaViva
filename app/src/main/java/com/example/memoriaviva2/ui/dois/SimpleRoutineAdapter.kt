package com.example.memoriaviva2.ui.dois

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriaviva2.R

class SimpleRoutineAdapter(
    private val activities: List<SimpleActivity>,
    private val onDoneToggle: (SimpleActivity, Boolean) -> Unit,
    private val onRemove: (SimpleActivity) -> Unit
) : RecyclerView.Adapter<SimpleRoutineAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.simple_activity_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = activities[position]
        holder.bind(activity, onDoneToggle, onRemove)
    }

    override fun getItemCount() = activities.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvClassification: TextView = itemView.findViewById(R.id.tv_classification)
        private val cbDone: CheckBox = itemView.findViewById(R.id.cb_done)
        private val btnRemove: android.widget.Button = itemView.findViewById(R.id.btn_remove)

        fun bind(
            activity: SimpleActivity, 
            onDoneToggle: (SimpleActivity, Boolean) -> Unit,
            onRemove: (SimpleActivity) -> Unit
        ) {
            tvTime.text = String.format("%02d:%02d", activity.hour, activity.minute)
            tvName.text = activity.name
            tvClassification.text = activity.classification
            
            cbDone.setOnCheckedChangeListener(null)
            cbDone.isChecked = activity.isDone
            cbDone.setOnCheckedChangeListener { _, isChecked ->
                onDoneToggle(activity, isChecked)
            }
            
            btnRemove.setOnClickListener {
                onRemove(activity)
            }
        }
    }
}