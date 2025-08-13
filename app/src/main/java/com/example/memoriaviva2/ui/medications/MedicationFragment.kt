package com.example.memoriaviva2.ui.medications

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriaviva2.R
import com.example.memoriaviva2.ui.backup.Remedio

class MedicationFragment : Fragment() {

    private lateinit var btnAddMedication: Button
    private lateinit var rvMedications: RecyclerView
    private lateinit var adapter: MedicationAdapter
    private lateinit var repository: MedicationRepository
    private val medications = mutableListOf<Remedio>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.simple_routine_layout, container, false)
        
        btnAddMedication = view.findViewById(R.id.btn_add_activity)
        rvMedications = view.findViewById(R.id.rv_activities)
        
        btnAddMedication.text = "Adicionar Remédio"
        view.findViewById<Button>(R.id.btn_show_report).visibility = View.GONE
        
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = MedicationRepository(requireContext())
        loadMedications()
        setupRecyclerView()
        
        btnAddMedication.setOnClickListener {
            showAddMedicationDialog()
        }
    }

    private fun loadMedications() {
        medications.clear()
        medications.addAll(repository.getMedications())
    }

    private fun setupRecyclerView() {
        adapter = MedicationAdapter(
            medications,
            onToggleAlarm = { medication ->
                val updated = medication.copy(alarmeAtivo = !medication.alarmeAtivo)
                repository.updateMedication(updated)
                loadMedications()
                adapter.notifyDataSetChanged()
                
                if (updated.alarmeAtivo) {
                    MedicationAlarmManager.setAlarm(requireContext(), updated)
                    Toast.makeText(requireContext(), "Alarme ativado para ${updated.nome}", Toast.LENGTH_SHORT).show()
                } else {
                    MedicationAlarmManager.cancelAlarm(requireContext(), updated)
                    Toast.makeText(requireContext(), "Alarme desativado para ${updated.nome}", Toast.LENGTH_SHORT).show()
                }
            },
            onRemove = { medication ->
                MedicationAlarmManager.cancelAlarm(requireContext(), medication)
                repository.removeMedication(medication.id)
                loadMedications()
                adapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "${medication.nome} removido", Toast.LENGTH_SHORT).show()
            }
        )
        rvMedications.adapter = adapter
        rvMedications.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showAddMedicationDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(android.R.layout.select_dialog_item, null)
        
        val etName = EditText(requireContext())
        etName.hint = "Nome do remédio"
        
        val etDosage = EditText(requireContext())
        etDosage.hint = "Dosagem (ex: 1 comprimido)"
        
        var selectedHour = 8
        var selectedMinute = 0
        
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
        layout.addView(etDosage)
        layout.addView(timeButton)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Adicionar Remédio")
            .setView(layout)
            .setPositiveButton("Adicionar") { _, _ ->
                val name = etName.text.toString()
                val dosage = etDosage.text.toString()
                if (name.isNotBlank() && dosage.isNotBlank()) {
                    val medication = Remedio(
                        nome = name,
                        dosagem = dosage,
                        hora = selectedHour,
                        minuto = selectedMinute,
                        alarmeAtivo = false
                    )
                    repository.addMedication(medication)
                    loadMedications()
                    adapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Remédio adicionado!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}