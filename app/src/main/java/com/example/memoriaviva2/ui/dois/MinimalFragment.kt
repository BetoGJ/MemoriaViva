package com.example.memoriaviva2.ui.dois

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class MinimalFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val textView = TextView(requireContext())
        textView.text = "Rotina funcionando!"
        textView.textSize = 20f
        textView.setPadding(50, 50, 50, 50)
        return textView
    }
}