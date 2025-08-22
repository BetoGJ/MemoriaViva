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
import com.example.memoriaviva2.databinding.FragmentRastreioBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

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
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val uri = "geo:${location.latitude},${location.longitude}?z=15"
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri))
                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(intent)
                    } else {
                        Toast.makeText(context, "App de mapas não encontrado", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Localização não disponível", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun toggleLocationSharing() {
        if (checkLocationPermission()) {
            isTracking = !isTracking
            if (isTracking) {
                binding.btnMonitorado.text = "Parar Compartilhamento"
                Toast.makeText(context, "Compartilhamento de localização ativado", Toast.LENGTH_SHORT).show()
            } else {
                binding.btnMonitorado.text = "Entrar como Monitorado"
                Toast.makeText(context, "Compartilhamento de localização desativado", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestLocationPermission()
        }
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
            1001
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && 
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permissão concedida", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}