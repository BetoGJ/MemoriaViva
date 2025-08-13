package com.example.memoriaviva2.ui.backup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.memoriaviva2.R
import com.google.gson.Gson
import java.io.IOException

class BackupFragment : Fragment() {

    private lateinit var buttonExport: Button
    private lateinit var buttonImport: Button
    private val backupManager = BackupManager()

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                exportData(uri)
            }
        }
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                importData(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_backup, container, false)

        buttonExport = root.findViewById(R.id.buttonExport)
        buttonImport = root.findViewById(R.id.buttonImport)

        buttonExport.setOnClickListener {
            createExportFile()
        }

        buttonImport.setOnClickListener {
            selectImportFile()
        }

        return root
    }

    private fun createExportFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "memoria_viva_backup.json")
        }
        exportLauncher.launch(intent)
    }

    private fun selectImportFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        importLauncher.launch(intent)
    }

    private fun exportData(uri: Uri) {
        try {
            val backupData = backupManager.createBackup(requireContext())
            val json = Gson().toJson(backupData)
            
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
            
            Toast.makeText(requireContext(), "Dados exportados com sucesso!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Erro ao exportar dados: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun importData(uri: Uri) {
        try {
            val json = requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes().toString(Charsets.UTF_8)
            } ?: return

            val backupData = Gson().fromJson(json, BackupData::class.java)
            backupManager.restoreBackup(requireContext(), backupData)
            
            Toast.makeText(requireContext(), "Dados importados com sucesso!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erro ao importar dados: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}