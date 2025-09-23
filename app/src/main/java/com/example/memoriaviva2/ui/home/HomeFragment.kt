package com.example.memoriaviva2.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.memoriaviva2.AppPreferencesKeys
import com.example.memoriaviva2.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!






    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        loadUserData()
        return root
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadUserData() {
        val sharedPreferences = requireContext().getSharedPreferences(AppPreferencesKeys.PREFS_USER_DATA, Context.MODE_PRIVATE)
        
        val nome = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_NAME, "Usuário")
        val idade = sharedPreferences.getInt(AppPreferencesKeys.KEY_USER_AGE, 0)
        val peso = sharedPreferences.getFloat(AppPreferencesKeys.KEY_USER_WEIGHT, 0f)
        val cirurgias = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_RECENT_SURGERIES, "")
        val internacoes = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_RECENT_HOSPITALIZATIONS, "")
        val comorbidades = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_COMORBIDITIES, "")
        val alergias = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_ALLERGIES, "")
        
        binding.textViewNomeIdoso.text = "Nome: $nome"
        binding.textViewIdadeIdoso.text = "Idade: $idade anos"
        binding.textViewCidadeIdoso.text = "Peso: ${peso}kg"
        
        binding.textViewInternacoesIdoso.text = if (internacoes.isNullOrEmpty()) {
            "Nenhuma internação registrada."
        } else {
            "Internações: $internacoes"
        }
        
        binding.textViewCirurgiasIdoso.text = if (cirurgias.isNullOrEmpty()) {
            "Nenhuma cirurgia registrada."
        } else {
            "Cirurgias: $cirurgias"
        }
        
        binding.textViewComorbidesIdoso.text = if (comorbidades.isNullOrEmpty()) {
            "Nenhum problema de saúde registrado."
        } else {
            "Problemas: $comorbidades"
        }
        
        binding.textViewAlergiasIdoso.text = if (alergias.isNullOrEmpty()) {
            "Nenhuma alergia registrada."
        } else {
            "Alergias: $alergias"
        }
    }
}