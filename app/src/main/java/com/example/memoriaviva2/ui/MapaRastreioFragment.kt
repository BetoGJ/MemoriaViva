package com.example.memoriaviva2.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.memoriaviva2.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.os.Looper

class MapaRastreioFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var database: DatabaseReference = Firebase.database.reference
    
    private var pacienteMarker: Marker? = null
    private var cuidadorMarker: Marker? = null
    private var currentTrackingCode: String? = null
    private var locationListener: ValueEventListener? = null
    private var isCuidadorMode = false
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_mapa_rastreio, container, false)
        
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        setupButtons(view)
        return view
    }

    private fun setupButtons(view: View) {
        view.findViewById<android.widget.Button>(R.id.btnCuidadorMapa).setOnClickListener {
            isCuidadorMode = true
            showCodeDialog("Digite o código do paciente:")
        }
        
        view.findViewById<android.widget.Button>(R.id.btnPacienteMapa).setOnClickListener {
            isCuidadorMode = false
            showCodeDialog("Digite seu código de rastreamento:")
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // Configurações do mapa
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
        
        // Permissão de localização
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        }
        
        // Foco inicial no Brasil
        val brasil = LatLng(-14.235, -51.925)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(brasil, 4f))
    }

    private fun showCodeDialog(message: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        val input = android.widget.EditText(requireContext())
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
        
        if (isCuidadorMode) {
            startCuidadorMode(code)
        } else {
            startPacienteMode(code)
        }
        
        // Listener para atualizações em tempo real
        locationListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val latitude = snapshot.child("latitude").getValue(Double::class.java)
                    val longitude = snapshot.child("longitude").getValue(Double::class.java)
                    
                    if (latitude != null && longitude != null) {
                        updatePacienteLocation(latitude, longitude)
                    }
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Erro: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        database.child("localiza_nois").child(code).addValueEventListener(locationListener!!)
    }

    private fun startPacienteMode(code: String) {
        Toast.makeText(context, "Compartilhando localização: $code", Toast.LENGTH_SHORT).show()
        
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMinUpdateIntervalMillis(1000)
            .build()
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // Envia para Firebase
                    val locationData = mapOf(
                        "latitude" to location.latitude,
                        "longitude" to location.longitude,
                        "timestamp" to com.google.firebase.database.ServerValue.TIMESTAMP
                    )
                    database.child("localiza_nois").child(code).setValue(locationData)
                    
                    // Atualiza mapa local
                    updateMyLocation(location.latitude, location.longitude, "Minha Localização")
                }
            }
        }
        
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun startCuidadorMode(code: String) {
        Toast.makeText(context, "Rastreando paciente: $code", Toast.LENGTH_SHORT).show()
        
        // Também obtém localização do cuidador
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        
        val cuidadorCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateCuidadorLocation(location.latitude, location.longitude)
                }
            }
        }
        
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, cuidadorCallback, Looper.getMainLooper())
        }
    }

    private fun updatePacienteLocation(latitude: Double, longitude: Double) {
        googleMap?.let { map ->
            val position = LatLng(latitude, longitude)
            
            if (pacienteMarker == null) {
                pacienteMarker = map.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title("Paciente")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
            } else {
                pacienteMarker?.position = position
            }
        }
    }

    private fun updateCuidadorLocation(latitude: Double, longitude: Double) {
        googleMap?.let { map ->
            val position = LatLng(latitude, longitude)
            
            if (cuidadorMarker == null) {
                cuidadorMarker = map.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title("Cuidador")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )
            } else {
                cuidadorMarker?.position = position
            }
            
            // Calcula distância se ambos estão visíveis
            pacienteMarker?.let { paciente ->
                val distance = FloatArray(1)
                android.location.Location.distanceBetween(
                    latitude, longitude,
                    paciente.position.latitude, paciente.position.longitude,
                    distance
                )
                
                // Atualiza título com distância
                cuidadorMarker?.title = "Cuidador (${distance[0].toInt()}m do paciente)"
            }
        }
    }

    private fun updateMyLocation(latitude: Double, longitude: Double, title: String) {
        googleMap?.let { map ->
            val position = LatLng(latitude, longitude)
            
            if (pacienteMarker == null) {
                pacienteMarker = map.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(title)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
            } else {
                pacienteMarker?.position = position
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        locationListener?.let { listener ->
            currentTrackingCode?.let { code ->
                database.child("localiza_nois").child(code).removeEventListener(listener)
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}