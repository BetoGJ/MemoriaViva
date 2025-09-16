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
import com.example.memoriaviva2.databinding.FragmentRastreioBinding
import com.google.android.gms.location.*
import android.os.Looper
import android.location.LocationManager
import android.content.Context
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

class RastreioFragment : Fragment() {

    private var _binding: FragmentRastreioBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isTracking = false
    private var database: DatabaseReference = Firebase.database.reference
    private var currentTrackingCode: String? = null
    private var locationListener: ValueEventListener? = null
    private var tutorLocation: android.location.Location? = null
    private var monitoradoLocation: android.location.Location? = null
    private var googleMap: GoogleMap? = null
    private var tutorMarker: Marker? = null
    private var monitoradoMarker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRastreioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupButtons()
        setupMap(savedInstanceState)
    }
    
    private fun setupMap(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync { map ->
            googleMap = map
            map.uiSettings.isZoomControlsEnabled = true
        }
    }

    private fun setupButtons() {
        binding.btnTutor.setOnClickListener {
            showTutorCodeDialog()
        }

        binding.btnMonitorado.setOnClickListener {
            toggleLocationSharing()
        }
    }

    private fun openMapsForTracking() {
        if (!checkLocationPermission()) {
            requestLocationPermission()
            return
        }
        
        Toast.makeText(context, "Obtendo localização...", Toast.LENGTH_SHORT).show()
        requestCurrentLocation()
    }
    
    private fun requestCurrentLocation() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            0
        ).setMaxUpdates(1).build()
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    fusedLocationClient.removeLocationUpdates(this)
                    openMaps(location)
                } else {
                    Toast.makeText(context, "Localização não encontrada", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        
        // Timeout de 8 segundos
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Toast.makeText(context, "Tempo esgotado. Tente novamente.", Toast.LENGTH_SHORT).show()
        }, 8000)
    }
    
    private fun openMaps(location: android.location.Location) {
        val uri = "geo:${location.latitude},${location.longitude}?z=15"
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri))
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(context, getString(R.string.maps_app_not_found), Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun toggleLocationSharing() {
        if (!checkLocationPermission()) {
            requestLocationPermission()
            return
        }
        
        if (!isTracking) {
            showTrackingKeyDialog()
        } else {
            stopTracking()
        }
    }
    
    private fun showTrackingKeyDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        val input = android.widget.EditText(requireContext())
        input.hint = "Digite 4 caracteres"
        input.filters = arrayOf(android.text.InputFilter.LengthFilter(4))
        
        builder.setTitle("Chave de Rastreamento")
            .setMessage("Digite a chave de 4 caracteres fornecida pelo tutor")
            .setView(input)
            .setPositiveButton("Conectar") { _, _ ->
                val key = input.text.toString().trim().uppercase()
                if (validateCode(key)) {
                    startTracking(key)
                } else {
                    Toast.makeText(context, "Código deve ter exatamente 4 caracteres", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun startTracking(key: String) {
        currentTrackingCode = key
        isTracking = true
        binding.btnMonitorado.text = getString(R.string.stop_sharing)
        Toast.makeText(context, "Compartilhando localização: $key", Toast.LENGTH_SHORT).show()
        
        startLocationSharing()
        showOwnLocationOnMap()
    }
    
    private fun startLocationSharing() {
        if (!checkLocationPermission()) {
            requestLocationPermission()
            return
        }
        
        // Força GPS de alta precisão para coordenadas reais do celular
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000 // 2 segundos - muito frequente
        ).setMinUpdateIntervalMillis(500) // Mínimo 0.5 segundo
         .setMaxUpdateDelayMillis(3000) // Máximo 3 segundos
         .setMinUpdateDistanceMeters(1f) // Atualiza a cada 1 metro
         .setWaitForAccurateLocation(true) // Espera GPS preciso
         .build()
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // Verifica se a localização é válida e precisa
                    if (location.accuracy <= 20) { // Precisão de até 20 metros
                        sendLocationToFirebase(location)
                        // Atualiza próprio mapa em tempo real
                        showLocationOnMap(location.latitude, location.longitude, "Minha Localização", true)
                        // Atualiza informações de localização
                        showLocationConnected(location.latitude, location.longitude, System.currentTimeMillis(), "GPS Ativo")
                    } else {
                        // GPS ainda não está preciso o suficiente
                        Toast.makeText(context, "Aguardando GPS mais preciso...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        
        Toast.makeText(context, "Rastreamento em tempo real ativo", Toast.LENGTH_SHORT).show()
    }
    
    private fun sendLocationToFirebase(location: android.location.Location) {
        currentTrackingCode?.let { code ->
            // Validação de segurança antes de enviar
            if (!validateCode(code)) {
                Toast.makeText(context, "Código inválido", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Debug: mostra coordenadas capturadas do GPS
            val lat = location.latitude
            val lng = location.longitude
            
            val locationData = mapOf(
                "latitude" to lat,
                "longitude" to lng,
                "accuracy" to location.accuracy,
                "timestamp" to com.google.firebase.database.ServerValue.TIMESTAMP,
                "package" to "com.example.memoriaviva2"
            )
            
            try {
                database.child("localiza_nois").child(code).setValue(locationData)
                    .addOnSuccessListener {
                        // Confirma envio das coordenadas reais
                        Toast.makeText(context, "GPS: ${String.format("%.6f", lat)}, ${String.format("%.6f", lng)}", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(context, "Erro Firebase: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro de conexão: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun stopTracking() {
        isTracking = false
        binding.btnMonitorado.text = getString(R.string.start_monitoring)
        Toast.makeText(context, getString(R.string.location_sharing_disabled), Toast.LENGTH_SHORT).show()
    }
    
    private fun showTutorCodeDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        val input = android.widget.EditText(requireContext())
        input.hint = "Digite 4 caracteres"
        input.filters = arrayOf(android.text.InputFilter.LengthFilter(4))
        
        builder.setTitle("Código do Monitorado")
            .setMessage("Digite o código de 4 caracteres do monitorado")
            .setView(input)
            .setPositiveButton("Rastrear") { _, _ ->
                val code = input.text.toString().trim().uppercase()
                if (validateCode(code)) {
                    startTutorTracking(code)
                } else {
                    Toast.makeText(context, "Código deve ter exatamente 4 caracteres", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun validateCode(code: String): Boolean {
        return code.length == 4 && code.matches(Regex("[A-Z0-9]{4}"))
    }
    
    private fun startTutorTracking(code: String) {
        currentTrackingCode = code
        binding.txtStatus.text = "Conectando ao monitorado: $code"
        binding.txtStatus.setBackgroundColor(android.graphics.Color.parseColor("#FFF3E0"))
        binding.txtStatus.setTextColor(android.graphics.Color.parseColor("#F57C00"))
        
        // Inicia rastreamento da própria localização do tutor
        startTutorLocationTracking()
        
        // Remove listener anterior se existir
        locationListener?.let { 
            database.child("localiza_nois").child(code).removeEventListener(it)
        }
        
        // Novo listener para atualizações em tempo real
        locationListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val latitude = snapshot.child("latitude").getValue(Double::class.java)
                    val longitude = snapshot.child("longitude").getValue(Double::class.java)
                    val timestamp = snapshot.child("timestamp").getValue(Long::class.java)
                    
                    if (latitude != null && longitude != null) {
                        // Atualiza localização do monitorado
                        monitoradoLocation = android.location.Location("").apply {
                            this.latitude = latitude
                            this.longitude = longitude
                        }
                        
                        showLocationConnected(latitude, longitude, timestamp, "Monitorado: $code")
                        // Usa coordenadas do Firebase para mostrar no mapa integrado
                        showFirebaseLocationOnMap(latitude, longitude)
                        checkGeofence()
                    } else {
                        showLocationNotAvailable()
                    }
                } else {
                    showLocationNotAvailable()
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                showLocationNotAvailable()
                Toast.makeText(context, "Erro Firebase: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        database.child("localiza_nois").child(code).addValueEventListener(locationListener!!)
    }
    
    private fun showLocationConnected(latitude: Double, longitude: Double, timestamp: Long?, title: String) {
        binding.txtStatus.text = "CONECTADO"
        binding.txtStatus.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E8"))
        binding.txtStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
        
        val timeStr = if (timestamp != null) {
            val date = java.util.Date(timestamp)
            java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(date)
        } else "--:--:--"
        
        binding.txtLocationInfo.text = "$title\n" +
                "${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}\n" +
                "Última atualização: $timeStr\n" +
                "[Toque para buscar no Google Maps]"
        
        // Adiciona clique para buscar coordenadas no Google Maps
        binding.txtLocationInfo.setOnClickListener {
            searchCoordinatesInGoogleMaps(latitude, longitude)
        }
        
        binding.txtLocationInfo.setOnClickListener {
            openExternalMap(latitude, longitude, title)
        }
    }
    
    private fun showOwnLocationOnMap() {
        if (!checkLocationPermission()) {
            requestLocationPermission()
            return
        }
        
        binding.txtStatus.text = "OBTENDO LOCALIZAÇÃO..."
        binding.txtStatus.setBackgroundColor(android.graphics.Color.parseColor("#FFF3E0"))
        binding.txtStatus.setTextColor(android.graphics.Color.parseColor("#F57C00"))
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                showLocationConnected(location.latitude, location.longitude, System.currentTimeMillis(), "Minha Localização")
                showLocationOnMap(location.latitude, location.longitude, "Minha Localização", true)
            } else {
                requestCurrentLocationForMap()
            }
        }
    }
    
    private fun requestCurrentLocationForMap() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            0
        ).setMaxUpdates(1).build()
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    fusedLocationClient.removeLocationUpdates(this)
                    showLocationConnected(location.latitude, location.longitude, System.currentTimeMillis(), "Minha Localização")
                    showLocationOnMap(location.latitude, location.longitude, "Minha Localização", true)
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
    
    private fun openExternalMap(latitude: Double, longitude: Double, title: String) {
        val uri = "geo:$latitude,$longitude?q=$latitude,$longitude($title)"
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri))
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }
    
    private fun startTutorLocationTracking() {
        if (!checkLocationPermission()) return
        
        // Tutor também precisa de tracking frequente para geofencing preciso
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000 // 5 segundos para geofencing rápido
        ).build()
        
        val tutorCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    tutorLocation = location
                    // Adiciona marcador azul do tutor no mapa
                    addTutorMarkerToMap(location.latitude, location.longitude)
                    checkGeofence()
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            tutorCallback,
            Looper.getMainLooper()
        )
    }
    
    private fun addTutorMarkerToMap(latitude: Double, longitude: Double) {
        googleMap?.let { map ->
            val position = LatLng(latitude, longitude)
            
            tutorMarker?.remove()
            tutorMarker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title("Tutor (Você)")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
        }
    }
    
    private fun checkGeofence() {
        val tutor = tutorLocation
        val monitorado = monitoradoLocation
        
        if (tutor != null && monitorado != null) {
            val distance = tutor.distanceTo(monitorado)
            
            if (distance > 100) { // 100 metros - ALARME
                triggerGeofenceAlarm(distance)
            } else {
                // Dentro da área segura
                binding.txtStatus.text = "CONECTADO - DISTÂNCIA: ${String.format("%.0f", distance)}m"
                binding.txtStatus.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E8"))
                binding.txtStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
            }
        }
    }
    
    private fun triggerGeofenceAlarm(distance: Float) {
        val distanceText = String.format("%.0f metros", distance)
        
        // ALARME VISUAL E SONORO
        Toast.makeText(context, "🚨 ALARME: Monitorado muito longe! $distanceText", Toast.LENGTH_LONG).show()
        
        // Status vermelho piscante
        binding.txtStatus.text = "🚨 ALARME - $distanceText"
        binding.txtStatus.setBackgroundColor(android.graphics.Color.parseColor("#F44336"))
        binding.txtStatus.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
        
        // Vibração se disponível
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = requireContext().getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(1000, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = requireContext().getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                @Suppress("DEPRECATION")
                vibrator.vibrate(1000)
            }
        } catch (e: Exception) {
            // Ignora se não tiver vibrador
        }
    }
    
    private fun showLocationOnMap(latitude: Double, longitude: Double, title: String, isOwnLocation: Boolean = false) {
        googleMap?.let { map ->
            val position = LatLng(latitude, longitude)
            
            if (isOwnLocation) {
                // Monitorado vendo sua própria localização
                monitoradoMarker?.remove()
                monitoradoMarker = map.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title("Você")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )
                // Segue o usuário em tempo real
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 17f))
            } else {
                // Tutor vendo localização do monitorado - atualização suave
                monitoradoMarker?.remove()
                monitoradoMarker = map.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title("👤 Monitorado")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )
                
                // Mantém ambos os marcadores visíveis se possível
                if (tutorLocation != null) {
                    val tutorPos = LatLng(tutorLocation!!.latitude, tutorLocation!!.longitude)
                    val bounds = LatLngBounds.Builder()
                        .include(position)
                        .include(tutorPos)
                        .build()
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                } else {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
                }
            }
        }
    }
    
    private fun showFirebaseLocationOnMap(latitude: Double, longitude: Double) {
        // Mostra coordenadas do Firebase no mapa integrado do app
        googleMap?.let { map ->
            val position = LatLng(latitude, longitude)
            
            // Remove marcadores anteriores
            monitoradoMarker?.remove()
            
            // Adiciona marcador com coordenadas do Firebase
            monitoradoMarker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title("📍 Localização Compartilhada")
                    .snippet("${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
            
            // Centraliza mapa nas coordenadas compartilhadas
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 16f))
        }
    }
    
    private fun searchCoordinatesInGoogleMaps(latitude: Double, longitude: Double) {
        // Atualiza o mapa integrado com as coordenadas
        showFirebaseLocationOnMap(latitude, longitude)
        Toast.makeText(context, "Mapa atualizado com coordenadas compartilhadas", Toast.LENGTH_SHORT).show()
    }
    
    private fun showLocationNotAvailable() {
        binding.txtStatus.text = "LOCALIZAÇÃO NÃO DISPONÍVEL"
        binding.txtStatus.setBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"))
        binding.txtStatus.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
        
        binding.txtLocationInfo.text = "Código incorreto ou sem conexão"
        binding.txtLocationInfo.setOnClickListener(null)
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        
        // Android 13+ notification permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        requestPermissions(permissions.toTypedArray(), LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && 
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        // Remove Firebase listener
        locationListener?.let { listener ->
            currentTrackingCode?.let { code ->
                database.child("localiza_nois").child(code).removeEventListener(listener)
            }
        }
        _binding = null
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}