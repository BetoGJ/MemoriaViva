package com.example.memoriaviva2.ui.dois

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriaviva2.R
import java.util.Calendar

class WorkingRoutineFragment : Fragment() {

    private lateinit var btnAddActivity: Button
    private lateinit var btnShowReport: Button
    private lateinit var rvActivities: RecyclerView
    private lateinit var adapter: SimpleRoutineAdapter
    private lateinit var repository: SimpleRoutineRepository
    private val activities = mutableListOf<SimpleActivity>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.simple_routine_layout, container, false)
        
        btnAddActivity = view.findViewById(R.id.btn_add_activity)
        btnShowReport = view.findViewById(R.id.btn_show_report)
        rvActivities = view.findViewById(R.id.rv_activities)
        
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = SimpleRoutineRepository(requireContext())
        loadActivities()
        setupRecyclerView()
        
        btnAddActivity.setOnClickListener {
            showAddActivityDialog()
        }
        
        btnShowReport.setOnClickListener {
            val intent = android.content.Intent(requireContext(), com.example.memoriaviva2.RoutineReportActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadActivities() {
        activities.clear()
        activities.addAll(repository.getActivities())
    }
    
    private fun setupRecyclerView() {
        adapter = SimpleRoutineAdapter(
            activities,
            onDoneToggle = { activity, isChecked ->
                activity.isDone = isChecked
                repository.saveActivities(activities)
                Toast.makeText(requireContext(), "${activity.name} ${if(isChecked) "concluída" else "pendente"}", Toast.LENGTH_SHORT).show()
            },
            onRemove = { activity ->
                activities.remove(activity)
                repository.saveActivities(activities)
                adapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "${activity.name} removida", Toast.LENGTH_SHORT).show()
            }
        )
        rvActivities.adapter = adapter
        rvActivities.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showAddActivityDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(android.R.layout.select_dialog_item, null)
        
        val etName = EditText(requireContext())
        etName.hint = "Nome da atividade"
        
        var selectedHour = 8
        var selectedMinute = 0
        
        val classifications = arrayOf("Exercício físico", "Estimulação cognitiva", "Alimentação", "Saúde")
        val spinner = Spinner(requireContext())
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, classifications)
        
        val timeButton = Button(requireContext())
        timeButton.text = String.format("%02d:%02d", selectedHour, selectedMinute)
        timeButton.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                timeButton.text = String.format("%02d:%02d", hour, minute)
            }, selectedHour, selectedMinute, true).show()
        }
        
        val layout = android.widget.LinearLayout(requireContext())
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.addView(etName)
        layout.addView(timeButton)
        layout.addView(spinner)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Adicionar Atividade")
            .setView(layout)
            .setPositiveButton("Adicionar") { _, _ ->
                val name = etName.text.toString()
                if (name.isNotBlank()) {
                    val activity = SimpleActivity(
                        name = name,
                        hour = selectedHour,
                        minute = selectedMinute,
                        classification = classifications[spinner.selectedItemPosition]
                    )
                    activities.add(activity)
                    activities.sortBy { it.hour * 60 + it.minute }
                    repository.saveActivities(activities)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Atividade adicionada!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showReport() {
        val report = repository.getYesterdayReport()
        if (report == null) {
            Toast.makeText(requireContext(), "Nenhum relatório disponível para ontem", Toast.LENGTH_SHORT).show()
            return
        }
        
        val completed = report["completed"] as? List<String> ?: emptyList()
        val missed = report["missed"] as? List<String> ?: emptyList()
        
        val message = StringBuilder()
        message.append("Relatório de Ontem\n\n")
        message.append("CONCLUÍDAS (${completed.size}):\n")
        if (completed.isNotEmpty()) {
            completed.forEach { message.append("- $it\n") }
        } else {
            message.append("- Nenhuma\n")
        }
        message.append("\nNÃO CONCLUÍDAS (${missed.size}):\n")
        if (missed.isNotEmpty()) {
            missed.forEach { message.append("- $it\n") }
        } else {
            message.append("- Nenhuma\n")
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Relatório do Dia Anterior")
            .setMessage(message.toString())
            .setPositiveButton("OK", null)
            .show()
    }
}

data class SimpleActivity(
    val name: String,
    val hour: Int,
    val minute: Int,
    val classification: String,
    var isDone: Boolean = false
)