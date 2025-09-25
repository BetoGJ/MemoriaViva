package com.example.memoriaviva2.ui.recreativa

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.memoriaviva2.R

class RecreativaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recreativa, container, false)
        
        setupButtons(view)
        
        return view
    }
    
    private fun setupButtons(view: View) {
        // Todos os botões funcionando
        view.findViewById<Button>(R.id.btnFraseDia).setOnClickListener {
            startActivity(Intent(requireContext(), FraseDiaActivity::class.java))
        }
        
        view.findViewById<Button>(R.id.btnJogoMemoria).setOnClickListener {
            startActivity(Intent(requireContext(), JogoMemoriaActivity::class.java))
        }
        
        view.findViewById<Button>(R.id.btnQuizImagens).setOnClickListener {
            startActivity(Intent(requireContext(), QuizImagensActivity::class.java))
        }
        
        view.findViewById<Button>(R.id.btnRespiracao).setOnClickListener {
            startActivity(Intent(requireContext(), RespiracaoActivity::class.java))
        }
        
        view.findViewById<Button>(R.id.btnAlongamento).setOnClickListener {
            startActivity(Intent(requireContext(), AlongamentoActivity::class.java))
        }
    }
}