package com.example.memoriaviva2.ui.notifications

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.memoriaviva2.NotificationReceiver
import com.example.memoriaviva2.databinding.FragmentNotificationsBinding
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val prefs by lazy {
        requireContext().getSharedPreferences("remedios", Context.MODE_PRIVATE)
    }

    private var horaSelecionada = -1
    private var minutoSelecionado = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        criarCanalDeNotificacao()
        atualizarLista()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnHorario.setOnClickListener {
            val timePicker = TimePickerDialog(requireContext(), { _, hour, minute ->
                horaSelecionada = hour
                minutoSelecionado = minute
                binding.txtHorarioSelecionado.text = "Horário: %02d:%02d".format(hour, minute)
            }, 12, 0, true)
            timePicker.show()
        }

        binding.btnAgendar.setOnClickListener {
            val nome = binding.editRemedio.text.toString().trim()
            if (nome.isEmpty() || horaSelecionada == -1) {
                Toast.makeText(context, "Preencha tudo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val remedios = carregarRemedios()
            remedios.put(JSONObject().apply {
                put("nome", nome)
                put("hora", horaSelecionada)
                put("minuto", minutoSelecionado)
            })

            salvarRemedios(remedios)
            agendar(nome, horaSelecionada, minutoSelecionado)
            atualizarLista()
            binding.editRemedio.text.clear()
            binding.txtHorarioSelecionado.text = "Nenhum horário selecionado"
            horaSelecionada = -1
        }
    }

    private fun atualizarLista() {
        binding.listaRemedios.removeAllViews()
        val remedios = carregarRemedios()
        for (i in 0 until remedios.length()) {
            val r = remedios.getJSONObject(i)
            val nome = r.getString("nome")
            val hora = r.getInt("hora")
            val minuto = r.getInt("minuto")

            val item = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 8)
            }

            val texto = TextView(requireContext()).apply {
                text = "$nome - %02d:%02d".format(hora, minuto)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val botao = Button(requireContext()).apply {
                text = "Remover"
                setOnClickListener {
                    cancelarAgendamento(nome)
                    removerRemedio(i)
                    atualizarLista()
                }
            }

            item.addView(texto)
            item.addView(botao)
            binding.listaRemedios.addView(item)
        }
    }

    private fun salvarRemedios(remedios: JSONArray) {
        prefs.edit().putString("lista", remedios.toString()).apply()
    }

    private fun carregarRemedios(): JSONArray {
        val json = prefs.getString("lista", "[]")
        return JSONArray(json)
    }

    private fun removerRemedio(indice: Int) {
        val arr = carregarRemedios()
        val novo = JSONArray()
        for (i in 0 until arr.length()) {
            if (i != indice) novo.put(arr.getJSONObject(i))
        }
        salvarRemedios(novo)
    }

    private fun agendar(nome: String, hora: Int, minuto: Int) {
        val intent = Intent(requireContext(), NotificationReceiver::class.java).apply {
            putExtra("remedio", nome)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), nome.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
        }

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelarAgendamento(nome: String) {
        val intent = Intent(requireContext(), NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), nome.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun criarCanalDeNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "canal_remedios",
                "Lembretes de Remédios",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = requireContext().getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
