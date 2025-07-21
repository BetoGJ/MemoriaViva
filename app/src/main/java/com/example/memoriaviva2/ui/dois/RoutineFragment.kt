package com.example.memoriaviva2.dois

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriaviva2.R // Importe o R do seu módulo app
import java.util.Calendar

class RoutineFragment : Fragment() {

    private lateinit var routineRepository: RoutineRepository
    private lateinit var routineAdapter: RoutineAdapter

    // Views do layout principal do Fragment
    private lateinit var rvActivities: RecyclerView
    private lateinit var btnAddActivity: Button
    private lateinit var btnShowReport: Button

    // onCreateView é onde você infla o layout do Fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o layout para este fragment
        // Use R.layout.dois se esse for o layout principal que você quer para este fragment
        val view = inflater.inflate(R.layout.activity_manage_routine, container, false)

        // Inicialize o repositório aqui, usando requireContext() para um Contexto seguro
        routineRepository = RoutineRepository(requireContext())

        // Encontre as views dentro do layout inflado do fragment
        rvActivities = view.findViewById(R.id.rv_routine_activities)
        btnAddActivity = view.findViewById(R.id.btn_add_routine_activity)
        btnShowReport = view.findViewById(R.id.btn_show_yesterday_report)

        return view
    }

    // onViewCreated é chamado depois que onCreateView retorna,
    // garantindo que a hierarquia de views do fragment foi completamente criada.
    // É um bom lugar para configurar as views.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        btnAddActivity.setOnClickListener {
            showAddOrEditActivityDialog(null) // null para adicionar nova
        }

        btnShowReport.setOnClickListener {
            showYesterdayReport()
        }

        loadActivitiesIntoAdapter()
    }

    private fun setupRecyclerView() {
        routineAdapter = RoutineAdapter(
            onItemClick = { activity -> showAddOrEditActivityDialog(activity) },
            onDoneToggle = { activity, isDone ->
                val updatedActivity = activity.copy(isDone = isDone)
                routineRepository.updateActivity(updatedActivity)
                Toast.makeText(requireContext(), "${activity.name} marcado como ${if(isDone) "feito" else "não feito"}", Toast.LENGTH_SHORT).show()
                // A lista será atualizada no próximo onResume ou explicitamente se necessário.
                // Se a atualização visual imediata de toda a lista for crucial após o toggle:
                // loadActivitiesIntoAdapter()
            }
        )
        rvActivities.adapter = routineAdapter
        rvActivities.layoutManager = LinearLayoutManager(requireContext()) // Use requireContext()
    }

    private fun loadActivitiesIntoAdapter() {
        val activities = routineRepository.getAllActivities()
        routineAdapter.submitList(activities.toMutableList())
        Log.d("RoutineFragment", "Atividades carregadas no adapter: ${activities.size} itens.")
    }

    private fun showAddOrEditActivityDialog(existingActivity: RoutineActivity?) {
        // Use requireContext() para obter o Context para o LayoutInflater e AlertDialog.Builder
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_edit_routine, null)
        val etName: EditText = dialogView.findViewById(R.id.et_routine_name)
        val timePicker: TimePicker = dialogView.findViewById(R.id.tp_routine_time)
        val spinnerClassification: Spinner = dialogView.findViewById(R.id.spinner_routine_classification)
        val cbDialogIsDone: CheckBox = dialogView.findViewById(R.id.cb_dialog_routine_is_done)

        val classificationOptions = ActivityClassification.values().map { it.displayName }.toTypedArray()
        // Use requireContext() para o ArrayAdapter
        spinnerClassification.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, classificationOptions)

        val title = if (existingActivity == null) "Adicionar Nova Atividade" else "Editar Atividade"

        if (existingActivity != null) {
            etName.setText(existingActivity.name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.hour = existingActivity.hour
                timePicker.minute = existingActivity.minute
            } else {
                timePicker.currentHour = existingActivity.hour
                timePicker.currentMinute = existingActivity.minute
            }
            spinnerClassification.setSelection(ActivityClassification.values().indexOf(existingActivity.classification))
            cbDialogIsDone.isChecked = existingActivity.isDone
            cbDialogIsDone.visibility = View.VISIBLE
        } else {
            cbDialogIsDone.visibility = View.GONE
        }

        val builder = AlertDialog.Builder(requireContext()) // Use requireContext()
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton(if (existingActivity == null) "Adicionar" else "Salvar") { _, _ ->
                val name = etName.text.toString()
                val hour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) timePicker.hour else timePicker.currentHour
                val minute = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) timePicker.minute else timePicker.currentMinute
                val classification = ActivityClassification.fromDisplayName(spinnerClassification.selectedItem.toString())
                    ?: ActivityClassification.HEALTH

                if (name.isNotBlank()) {
                    if (existingActivity == null) {
                        val newActivity = RoutineActivity(
                            name = name,
                            hour = hour,
                            minute = minute,
                            classification = classification
                        )
                        routineRepository.addActivity(newActivity)
                        Toast.makeText(requireContext(), "'${newActivity.name}' adicionada.", Toast.LENGTH_SHORT).show()
                    } else {
                        val updatedActivity = existingActivity.copy(
                            name = name,
                            hour = hour,
                            minute = minute,
                            classification = classification,
                            isDone = cbDialogIsDone.isChecked
                        )
                        routineRepository.updateActivity(updatedActivity)
                        Toast.makeText(requireContext(), "'${updatedActivity.name}' atualizada.", Toast.LENGTH_SHORT).show()
                    }
                    loadActivitiesIntoAdapter()
                } else {
                    Toast.makeText(requireContext(), "O nome da atividade é obrigatório.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)

        if (existingActivity != null) {
            builder.setNeutralButton("Remover") { _, _ ->
                routineRepository.removeActivity(existingActivity.id)
                Toast.makeText(requireContext(), "'${existingActivity.name}' removida.", Toast.LENGTH_SHORT).show()
                loadActivitiesIntoAdapter()
            }
        }
        builder.show()
    }

    private fun showYesterdayReport() {
        val report = routineRepository.getYesterdayReport()
        if (report == null) {
            Toast.makeText(requireContext(), "Nenhum relatório disponível para ontem.", Toast.LENGTH_SHORT).show()
            Log.d("RoutineFragment", "Tentativa de mostrar relatório, mas não há dados para ontem.")
            return
        }

        val reportDate = Calendar.getInstance().apply {
            timeInMillis = report.dateEpochDay * (1000L * 60 * 60 * 24)
        }
        val dateString = "${reportDate.get(Calendar.DAY_OF_MONTH)}/${reportDate.get(Calendar.MONTH) + 1}/${reportDate.get(Calendar.YEAR)}"

        val message = StringBuilder()
        message.append("Relatório do dia: $dateString\n\n")
        message.append("CONCLUÍDAS (${report.completedActivities.size}):\n")
        if (report.completedActivities.isNotEmpty()) {
            report.completedActivities.forEach { message.append("- $it\n") }
        } else {
            message.append("- Nenhuma\n")
        }
        message.append("\nNÃO CONCLUÍDAS (${report.missedActivities.size}):\n")
        if (report.missedActivities.isNotEmpty()) {
            report.missedActivities.forEach { message.append("- $it\n") }
        } else {
            message.append("- Nenhuma\n")
        }

        AlertDialog.Builder(requireContext()) // Use requireContext()
            .setTitle("Relatório do Dia Anterior")
            .setMessage(message.toString())
            .setPositiveButton("OK", null)
            .show()
        Log.d("RoutineFragment", "Relatório de ontem exibido.")
    }

    override fun onResume() {
        super.onResume()
        // O checkForDailyReset é chamado dentro de getAllActivities,
        // então carregar as atividades aqui já garante que o reset seja verificado.
        loadActivitiesIntoAdapter()
        Log.d("RoutineFragment", "onResume: Atividades recarregadas e reset diário verificado.")
    }
}

