package com.example.memoriaviva2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ActivityReportAdapter(private val activities: List<ActivityReport>) : 
    RecyclerView.Adapter<ActivityReportAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvActivityName: TextView = view.findViewById(R.id.tvActivityName)
        val tvActivityTime: TextView = view.findViewById(R.id.tvActivityTime)
        val tvActivityStatus: TextView = view.findViewById(R.id.tvActivityStatus)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_report, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = activities[position]
        
        holder.tvActivityName.text = activity.name
        holder.tvActivityTime.text = "Horário: ${activity.time}"
        
        if (activity.completed) {
            holder.tvActivityStatus.text = "✓ Concluída"
            holder.tvActivityStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.teal_200))
        } else {
            holder.tvActivityStatus.text = "✗ Não realizada"
            holder.tvActivityStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
        }
    }
    
    override fun getItemCount() = activities.size
}