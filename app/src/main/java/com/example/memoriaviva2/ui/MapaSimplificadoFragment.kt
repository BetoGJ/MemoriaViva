package com.example.memoriaviva2.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.webkit.WebView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.memoriaviva2.R
import com.google.android.gms.location.*
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.os.Looper

class MapaSimplificadoFragment : Fragment() {

    private lateinit var webViewMapa: WebView
    private lateinit var txtCoordenadas: TextView
    private lateinit var txtDistancia: TextView
    private lateinit var txtStatus: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var database: DatabaseReference = Firebase.database.reference
    
    private var pacienteLat = 0.0
    private var pacienteLng = 0.0
    private var cuidadorLat = 0.0
    private var cuidadorLng = 0.0
    private var currentTrackingCode: String? = null
    private var locationListener: ValueEventListener? = null
    private var isCuidadorMode = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_mapa_simplificado, container, false)
        
        webViewMapa = view.findViewById(R.id.webViewMapa)
        txtCoordenadas = view.findViewById(R.id.txtCoordenadas)
        txtDistancia = view.findViewById(R.id.txtDistancia)
        txtStatus = view.findViewById(R.id.txtStatus)
        
        setupWebView()
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        setupButtons(view)
        
        return view
    }

    private fun setupButtons(view: View) {
        view.findViewById<Button>(R.id.btnCuidadorSimples).setOnClickListener {
            isCuidadorMode = true
            showCodeDialog("Digite o código do paciente para rastrear:")
        }
        
        // Paciente não vê mapa, apenas compartilha localização
        view.findViewById<Button>(R.id.btnPacienteSimples).visibility = View.GONE
    }



    private fun showCodeDialog(message: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        val input = EditText(requireContext())
        input.hint = "4 caracteres"
        input.filters = arrayOf(android.text.InputFilter.LengthFilter(4))
        
        builder.setTitle("Código de Rastreamento")
            .setMessage(message)
            .setView(input)
            .setPositiveButton("Conectar") { _, _ ->
                val code = input.text.toString().trim().uppercase()
                if (code.length == 4) {
                    startTracking(code)
                } else {
                    Toast.makeText(context, "Código deve ter 4 caracteres", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun startTracking(code: String) {
        currentTrackingCode = code
        txtStatus.text = "🔍 Conectando ao paciente: $code"
        txtStatus.setBackgroundColor(Color.parseColor("#FFF3E0"))
        
        locationListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val latitude = snapshot.child("latitude").getValue(Double::class.java)
                    val longitude = snapshot.child("longitude").getValue(Double::class.java)
                    
                    if (latitude != null && longitude != null) {
                        pacienteLat = latitude
                        pacienteLng = longitude
                        txtStatus.text = "✅ Paciente conectado: $code"
                        txtStatus.setBackgroundColor(Color.parseColor("#E8F5E8"))
                        updateDisplay()
                        Toast.makeText(context, "Localização recebida!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    txtStatus.text = "❌ Paciente offline: $code"
                    txtStatus.setBackgroundColor(Color.parseColor("#FFEBEE"))
                    txtCoordenadas.text = "📍 Aguardando paciente..."
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                txtStatus.text = "❌ Erro de conexão"
                txtStatus.setBackgroundColor(Color.parseColor("#FFEBEE"))
                Toast.makeText(context, "Erro: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        database.child("localiza_nois").child(code).addValueEventListener(locationListener!!)
    }



    private fun setupWebView() {
        webViewMapa.settings.javaScriptEnabled = true
        
        val htmlContent = """
            <html>
            <body style="margin:0; padding:10px; font-family:Arial; text-align:center;">
                <h3>🗺️ Mapa do Paciente</h3>
                <div id="info">Aguardando localização...</div>
                <iframe id="map" width="100%" height="300" frameborder="0" 
                        src="about:blank"></iframe>
                <script>
                    function updateMap(lat, lng) {
                        document.getElementById('info').innerHTML = 'Lat: ' + lat + '<br>Lng: ' + lng;
                        var url = 'https://maps.google.com/maps?q=' + lat + ',' + lng + '&output=embed';
                        document.getElementById('map').src = url;
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
        
        webViewMapa.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
    
    private fun updateDisplay() {
        if (pacienteLat != 0.0 && pacienteLng != 0.0) {
            webViewMapa.evaluateJavascript("updateMap($pacienteLat, $pacienteLng)", null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationListener?.let { listener ->
            currentTrackingCode?.let { code ->
                database.child("localiza_nois").child(code).removeEventListener(listener)
            }
        }
    }
}