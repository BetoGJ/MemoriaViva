package com.example.memoriaviva2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class DailyReportFragment : Fragment() {
    
    private lateinit var tvDate: TextView
    private lateinit var rvActivities: RecyclerView
    private lateinit var tvNoActivities: TextView
    private var dayOffset: Int = 0
    
    companion object {
        private const val ARG_DAY_OFFSET = "day_offset"
        
        fun newInstance(dayOffset: Int): DailyReportFragment {
            val fragment = DailyReportFragment()
            val args = Bundle()
            args.putInt(ARG_DAY_OFFSET, dayOffset)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dayOffset = arguments?.getInt(ARG_DAY_OFFSET) ?: 0
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_daily_report, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        tvDate = view.findViewById(R.id.tvDate)
        rvActivities = view.findViewById(R.id.rvActivities)
        tvNoActivities = view.findViewById(R.id.tvNoActivities)
        
        setupDate()
        setupRecyclerView()
        loadActivities()
    }
    
    private fun setupDate() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy - EEEE", Locale("pt", "BR"))
        tvDate.text = dateFormat.format(calendar.time)
    }
    
    private fun setupRecyclerView() {
        rvActivities.layoutManager = LinearLayoutManager(requireContext())
    }
    
    private fun loadActivities() {
        // Simulação de dados - aqui você carregaria os dados reais
        val activities = getActivitiesForDay(dayOffset)
        
        if (activities.isEmpty()) {
            tvNoActivities.visibility = View.VISIBLE
            rvActivities.visibility = View.GONE
        } else {
            tvNoActivities.visibility = View.GONE
            rvActivities.visibility = View.VISIBLE
            rvActivities.adapter = ActivityReportAdapter(activities)
        }
    }
    
    private fun getActivitiesForDay(dayOffset: Int): List<ActivityReport> {
        // Simulação - substitua pela lógica real de carregamento
        return when (dayOffset) {
            0 -> listOf(
                ActivityReport("Tomar medicamento", "08:00", true),
                ActivityReport("Exercício matinal", "09:00", true),
                ActivityReport("Almoço", "12:00", false)
            )
            1 -> listOf(
                ActivityReport("Tomar medicamento", "08:00", true),
                ActivityReport("Caminhada", "10:00", true)
            )
            else -> emptyList()
        }
    }
}

data class ActivityReport(
    val name: String,
    val time: String,
    val completed: Boolean
)