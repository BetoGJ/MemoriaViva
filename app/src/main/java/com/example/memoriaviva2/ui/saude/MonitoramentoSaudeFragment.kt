package com.example.memoriaviva2.ui.saude

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.memoriaviva2.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class DadosSaude(
    var pressaoArterial: String = "",
    var frequenciaCardiaca: String = "",
    var frequenciaRespiratoria: String = "",
    var saturacao: String = "",
    var temperatura: String = "",
    var peso: String = "",
    var altura: String = "",
    var imc: String = "",
    var queixas: String = "",
    var dataRegistro: String = ""
)

class MonitoramentoSaudeFragment : Fragment() {

    private lateinit var listViewDados: ListView
    private lateinit var btnAdicionarDados: Button
    private var dadosSaudeList = mutableListOf<DadosSaude>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_monitoramento_saude, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        listViewDados = view.findViewById(R.id.listViewDadosSaude)
        btnAdicionarDados = view.findViewById(R.id.btnAdicionarDados)
        
        carregarDados()
        configurarListView()
        
        btnAdicionarDados.setOnClickListener {
            mostrarDialogDados(null, -1)
        }
    }

    private fun carregarDados() {
        val sharedPref = requireActivity().getSharedPreferences("dados_saude", Context.MODE_PRIVATE)
        val dadosJson = sharedPref.getString("lista_dados", "[]")
        val type = object : TypeToken<MutableList<DadosSaude>>() {}.type
        dadosSaudeList = Gson().fromJson(dadosJson, type) ?: mutableListOf()
    }

    private fun salvarDados() {
        val sharedPref = requireActivity().getSharedPreferences("dados_saude", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val dadosJson = Gson().toJson(dadosSaudeList)
        editor.putString("lista_dados", dadosJson)
        editor.apply()
    }

    private fun configurarListView() {
        val displayList = dadosSaudeList.map { dados ->
            "📅 ${dados.dataRegistro}\n💓 FC: ${dados.frequenciaCardiaca} | 🩸 PA: ${dados.pressaoArterial}"
        }
        
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, displayList)
        listViewDados.adapter = adapter
        
        listViewDados.setOnItemClickListener { _, _, position, _ ->
            mostrarOpcoesItem(position)
        }
    }

    private fun mostrarOpcoesItem(position: Int) {
        val opcoes = arrayOf("Visualizar", "Editar", "Remover")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Opções")
            .setItems(opcoes) { _, which ->
                when (which) {
                    0 -> mostrarDetalhes(position)
                    1 -> mostrarDialogDados(dadosSaudeList[position], position)
                    2 -> removerDados(position)
                }
            }
            .show()
    }

    private fun mostrarDetalhes(position: Int) {
        val dados = dadosSaudeList[position]
        val detalhes = """
            📅 Data: ${dados.dataRegistro}
            🩸 Pressão Arterial: ${dados.pressaoArterial}
            💓 Frequência Cardíaca: ${dados.frequenciaCardiaca}
            🫁 Frequência Respiratória: ${dados.frequenciaRespiratoria}
            🩸 Saturação: ${dados.saturacao}
            🌡️ Temperatura: ${dados.temperatura}
            ⚖️ Peso: ${dados.peso}
            📏 Altura: ${dados.altura}
            📊 IMC: ${dados.imc}
            💬 Queixas: ${dados.queixas}
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("Detalhes dos Dados de Saúde")
            .setMessage(detalhes)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun mostrarDialogDados(dados: DadosSaude?, position: Int) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_dados_saude, null)
        
        val etPressao = dialogView.findViewById<EditText>(R.id.etPressaoArterial)
        val etFreqCardiaca = dialogView.findViewById<EditText>(R.id.etFrequenciaCardiaca)
        val etFreqRespiratoria = dialogView.findViewById<EditText>(R.id.etFrequenciaRespiratoria)
        val etSaturacao = dialogView.findViewById<EditText>(R.id.etSaturacao)
        val etTemperatura = dialogView.findViewById<EditText>(R.id.etTemperatura)
        val etPeso = dialogView.findViewById<EditText>(R.id.etPeso)
        val etAltura = dialogView.findViewById<EditText>(R.id.etAltura)
        val etQueixas = dialogView.findViewById<EditText>(R.id.etQueixas)
        
        dados?.let {
            etPressao.setText(it.pressaoArterial)
            etFreqCardiaca.setText(it.frequenciaCardiaca)
            etFreqRespiratoria.setText(it.frequenciaRespiratoria)
            etSaturacao.setText(it.saturacao)
            etTemperatura.setText(it.temperatura)
            etPeso.setText(it.peso)
            etAltura.setText(it.altura)
            etQueixas.setText(it.queixas)
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle(if (dados == null) "Adicionar Dados" else "Editar Dados")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val novoDado = DadosSaude(
                    pressaoArterial = etPressao.text.toString(),
                    frequenciaCardiaca = etFreqCardiaca.text.toString(),
                    frequenciaRespiratoria = etFreqRespiratoria.text.toString(),
                    saturacao = etSaturacao.text.toString(),
                    temperatura = etTemperatura.text.toString(),
                    peso = etPeso.text.toString(),
                    altura = etAltura.text.toString(),
                    queixas = etQueixas.text.toString(),
                    dataRegistro = if (dados == null) 
                        java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                    else dados.dataRegistro
                )
                
                // Calcular IMC se peso e altura estiverem preenchidos
                if (novoDado.peso.isNotEmpty() && novoDado.altura.isNotEmpty()) {
                    try {
                        val pesoFloat = novoDado.peso.toFloat()
                        val alturaFloat = novoDado.altura.toFloat() / 100 // converter cm para m
                        val imc = pesoFloat / (alturaFloat * alturaFloat)
                        novoDado.imc = String.format("%.1f", imc)
                    } catch (e: Exception) {
                        novoDado.imc = ""
                    }
                }
                
                if (position == -1) {
                    dadosSaudeList.add(0, novoDado)
                } else {
                    dadosSaudeList[position] = novoDado
                }
                
                salvarDados()
                configurarListView()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun removerDados(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Remoção")
            .setMessage("Deseja realmente remover estes dados de saúde?")
            .setPositiveButton("Sim") { _, _ ->
                dadosSaudeList.removeAt(position)
                salvarDados()
                configurarListView()
            }
            .setNegativeButton("Não", null)
            .show()
    }
}