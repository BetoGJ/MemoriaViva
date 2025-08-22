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

class RastreioFragment : Fragment() {

    private var _binding: FragmentRastreioBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isTracking = false

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
    }

    private fun setupButtons() {
        binding.btnTutor.setOnClickListener {
            openMapsForTracking()
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
        
        if (!isLocationEnabled()) {
            Toast.makeText(context, "Ative o GPS nas configurações do dispositivo", Toast.LENGTH_LONG).show()
            return
        }
        
        Toast.makeText(context, "Obtendo localização...", Toast.LENGTH_SHORT).show()
        
        // Sempre solicita localização atual para garantir precisão
        requestCurrentLocation()
    }
    
    private fun requestCurrentLocation() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000
        ).setMaxUpdates(1)
         .setWaitForAccurateLocation(true)
         .build()
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    fusedLocationClient.removeLocationUpdates(this)
                    openMaps(location)
                } ?: run {
                    Toast.makeText(context, "GPS não conseguiu obter localização. Verifique se está ao ar livre.", Toast.LENGTH_LONG).show()
                }
            }
            
            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) {
                    Toast.makeText(context, "GPS indisponível. Verifique as configurações.", Toast.LENGTH_LONG).show()
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        
        // Timeout após 10 segundos
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Toast.makeText(context, "Timeout: Não foi possível obter localização", Toast.LENGTH_SHORT).show()
        }, 10000)
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
        
        if (!isLocationEnabled()) {
            Toast.makeText(context, "Ative o GPS nas configurações", Toast.LENGTH_LONG).show()
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
        input.hint = "Digite a chave de rastreamento"
        
        builder.setTitle("Chave de Rastreamento")
            .setMessage("Digite a chave fornecida pelo tutor para iniciar o compartilhamento")
            .setView(input)
            .setPositiveButton("Conectar") { _, _ ->
                val key = input.text.toString().trim()
                if (key.isNotEmpty()) {
                    startTracking(key)
                } else {
                    Toast.makeText(context, "Chave não pode estar vazia", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun startTracking(key: String) {
        isTracking = true
        binding.btnMonitorado.text = getString(R.string.stop_sharing)
        Toast.makeText(context, "Rastreamento iniciado com chave: $key", Toast.LENGTH_SHORT).show()
        
        // Aqui você salvaria a chave e iniciaria o envio para Firebase
        // Por exemplo: saveTrackingKey(key)
    }
    
    private fun stopTracking() {
        isTracking = false
        binding.btnMonitorado.text = getString(R.string.start_monitoring)
        Toast.makeText(context, getString(R.string.location_sharing_disabled), Toast.LENGTH_SHORT).show()
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}