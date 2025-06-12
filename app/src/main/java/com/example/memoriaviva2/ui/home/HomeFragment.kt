package com.example.memoriaviva2.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.memoriaviva2.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Propriedades para armazenar os dados recebidos
    private var nomeIdosoArg: String? = "Idoso"
    private var idadeIdosoArg: Int = 81
    private var cidadeIdosoArg: String? = "Santa Rita do Sapucaí"
    private var internacoesIdosoArg: ArrayList<String>? = arrayListOf("Internacao 1", "Internacao2")

    private var cirurgiasIdosoArg: ArrayList<String>? = arrayListOf("Cirurgia 1", "Cirurgia2")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            nomeIdosoArg = it.getString(ARG_NOME)
            idadeIdosoArg = it.getInt(ARG_IDADE, 0)
            cidadeIdosoArg = it.getString(ARG_CIDADE)
            internacoesIdosoArg = it.getStringArrayList(ARG_INTERNACOES)
            cirurgiasIdosoArg = it.getStringArrayList(ARG_CIRURGIAS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        nomeIdosoArg?.let { binding.textViewNomeIdoso.text = "Nome: $it" }
        binding.textViewIdadeIdoso.text = "Idade: $idadeIdosoArg anos"
        cidadeIdosoArg?.let { binding.textViewCidadeIdoso.text = "Cidade: $it" }

        internacoesIdosoArg?.let {
            binding.textViewInternacoesIdoso.text = if (it.isNotEmpty()) {
                it.joinToString(separator = "\n") { item -> "- $item" }
            } else {
                "Nenhuma internação registrada."
            }
        }

        cirurgiasIdosoArg?.let {
            binding.textViewCirurgiasIdoso.text = if (it.isNotEmpty()) {
                it.joinToString(separator = "\n") { item -> "- $item" }
            } else {
                "Nenhuma cirurgia registrada."
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_NOME = "arg_nome"
        private const val ARG_IDADE = "arg_idade"
        private const val ARG_CIDADE = "arg_cidade"
        private const val ARG_INTERNACOES = "arg_internacoes"
        private const val ARG_CIRURGIAS = "arg_cirurgias"

        @JvmStatic
        fun newInstance(
            nome: String,
            idade: Int,
            cidade: String,
            internacoes: List<String>, // Recebe List
            cirurgias: List<String>  // Recebe List
        ): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putString(ARG_NOME, nome)
            args.putInt(ARG_IDADE, idade)
            args.putString(ARG_CIDADE, cidade)
            args.putStringArrayList(ARG_INTERNACOES, ArrayList(internacoes)) // Converte para ArrayList
            args.putStringArrayList(ARG_CIRURGIAS, ArrayList(cirurgias))     // Converte para ArrayList
            fragment.arguments = args
            return fragment
        }


    }
}