package com.example.memoriaviva2.ui // Ou o pacote onde UmFragment está localizado

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memoriaviva2.data.DietRepository
import com.example.memoriaviva2.databinding.UmBinding // Importe o binding correto para fragment_um.xml
import com.example.memoriaviva2.model.DietItem

// Certifique-se que o nome da classe e o pacote estão corretos
class TelaUmFragment : Fragment() {

    private var _binding: UmBinding? = null
    private val binding get() = _binding!!

    private lateinit var dietItemAdapter: DietItemAdapter
    private lateinit var dietRepository: DietRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla o layout usando ViewBinding
        _binding = UmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dietRepository = DietRepository(requireContext())
        setupRecyclerView()
        loadDietItems()

        binding.buttonAddDietItem.setOnClickListener {
            showAddDietItemDialog()
        }
        // Se você tiver um título no XML como textViewFragmentTitle, pode configurá-lo aqui se necessário
        // binding.textViewFragmentTitle.text = "Minha Dieta Personalizada"
    }

    private fun setupRecyclerView() {
        // Use o DietItemAdapter criado anteriormente
        dietItemAdapter = DietItemAdapter(
            mutableListOf(),
            onRemoveClickListener = { dietItem -> showRemoveDietItemConfirmationDialog(dietItem) }
        )
        binding.recyclerViewDietItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dietItemAdapter
        }
    }

    private fun loadDietItems() {
        val dietItems = dietRepository.getDietItems()
        dietItemAdapter.updateDietItems(dietItems)
        updateEmptyViewVisibility(dietItems.isEmpty())
    }

    private fun updateEmptyViewVisibility(isEmpty: Boolean) {
        binding.recyclerViewDietItems.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.textViewEmptyDiet.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun showAddDietItemDialog() {
        val context = requireContext()
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Adicionar Alimento à Dieta")

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20) // Ajuste o padding conforme necessário
        }

        val foodNameInput = EditText(context).apply { hint = "Nome do Alimento" }
        layout.addView(foodNameInput)

        val portionInput = EditText(context).apply { hint = "Porção (ex: 100g, 1 fatia)" }
        layout.addView(portionInput)

        val mealTimeInput = EditText(context).apply {
            hint = "Horário/Refeição (ex: Café da Manhã, 08:00)"
        }
        layout.addView(mealTimeInput)

        builder.setView(layout)

        builder.setPositiveButton("Adicionar") { dialog, _ ->
            val foodName = foodNameInput.text.toString().trim()
            val portion = portionInput.text.toString().trim()
            val mealTime = mealTimeInput.text.toString().trim()

            if (foodName.isNotEmpty() && portion.isNotEmpty() && mealTime.isNotEmpty()) {
                val newDietItem = DietItem(foodName = foodName, portion = portion, mealTime = mealTime)
                dietRepository.addDietItem(newDietItem)
                loadDietItems()
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Todos os campos são obrigatórios.", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showRemoveDietItemConfirmationDialog(dietItem: DietItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remover Alimento")
            .setMessage("Remover ${dietItem.foodName} da dieta?")
            .setPositiveButton("Remover") { _, _ ->
                dietRepository.removeDietItem(dietItem.id)
                loadDietItems()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpa a referência ao binding para evitar memory leaks
    }

}
