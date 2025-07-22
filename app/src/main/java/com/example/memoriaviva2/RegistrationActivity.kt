package com.example.memoriaviva2

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar // Importar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegistrationActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var toolbarRegistration: Toolbar // View para a Toolbar
    private lateinit var textInputLayoutNameReg: TextInputLayout
    private lateinit var editTextNameReg: TextInputEditText
    private lateinit var textInputLayoutAgeReg: TextInputLayout
    private lateinit var editTextAgeReg: TextInputEditText
    private lateinit var textInputLayoutWeightReg: TextInputLayout
    private lateinit var editTextWeightReg: TextInputEditText
    private lateinit var editTextRecentSurgeriesReg: TextInputEditText
    private lateinit var editTextRecentHospitalizationsReg: TextInputEditText
    private lateinit var buttonSaveRegistration: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // NENHUMA CHAMADA PARA requestWindowFeature() ou supportRequestWindowFeature() aqui
        setContentView(R.layout.activity_registration)

        // Inicialização da Toolbar
        toolbarRegistration = findViewById(R.id.toolbarRegistration) // Usa o ID do XML
        setSupportActionBar(toolbarRegistration)
        // O título da Toolbar já é definido no XML (app:title="Cadastro do Paciente")
        // Se quiser habilitar o botão "Up" (voltar) na Toolbar:
        // supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // supportActionBar?.setDisplayShowHomeEnabled(true)


        sharedPreferences = getSharedPreferences(AppPreferencesKeys.PREFS_USER_DATA, Context.MODE_PRIVATE)

        textInputLayoutNameReg = findViewById(R.id.textInputLayoutNameReg)
        editTextNameReg = findViewById(R.id.editTextNameReg)
        textInputLayoutAgeReg = findViewById(R.id.textInputLayoutAgeReg)
        editTextAgeReg = findViewById(R.id.editTextAgeReg)
        textInputLayoutWeightReg = findViewById(R.id.textInputLayoutWeightReg)
        editTextWeightReg = findViewById(R.id.editTextWeightReg)
        editTextRecentSurgeriesReg = findViewById(R.id.editTextRecentSurgeriesReg)
        editTextRecentHospitalizationsReg = findViewById(R.id.editTextRecentHospitalizationsReg)
        buttonSaveRegistration = findViewById(R.id.buttonSaveRegistration)

        buttonSaveRegistration.setOnClickListener {
            validateAndSaveData()
        }
    }

    private fun validateAndSaveData() {
        val name = editTextNameReg.text.toString().trim()
        val ageStr = editTextAgeReg.text.toString().trim()
        val weightStr = editTextWeightReg.text.toString().trim()
        val surgeries = editTextRecentSurgeriesReg.text.toString().trim()
        val hospitalizations = editTextRecentHospitalizationsReg.text.toString().trim()

        var isValid = true

        if (name.isEmpty()) {
            textInputLayoutNameReg.error = "Nome é obrigatório"
            isValid = false
        } else {
            textInputLayoutNameReg.error = null
        }

        val age = ageStr.toIntOrNull()
        if (ageStr.isEmpty() || age == null || age <= 0 || age > 130) {
            textInputLayoutAgeReg.error = "Idade inválida"
            isValid = false
        } else {
            textInputLayoutAgeReg.error = null
        }

        val weight = weightStr.toDoubleOrNull()
        if (weightStr.isEmpty() || weight == null || weight <= 0.0 || weight > 500.0) {
            textInputLayoutWeightReg.error = "Peso inválido"
            isValid = false
        } else {
            textInputLayoutWeightReg.error = null
        }

        if (!isValid) {
            Toast.makeText(this, "Por favor, corrija os campos marcados.", Toast.LENGTH_LONG).show()
            return
        }

        with(sharedPreferences.edit()) {
            putBoolean(AppPreferencesKeys.KEY_IS_USER_REGISTERED, true)
            putString(AppPreferencesKeys.KEY_USER_NAME, name)
            putInt(AppPreferencesKeys.KEY_USER_AGE, age!!)
            putFloat(AppPreferencesKeys.KEY_USER_WEIGHT, weight!!.toFloat())
            putString(AppPreferencesKeys.KEY_USER_RECENT_SURGERIES, surgeries)
            putString(AppPreferencesKeys.KEY_USER_RECENT_HOSPITALIZATIONS, hospitalizations)
            apply()
        }

        Toast.makeText(this, "Paciente registrado com sucesso!", Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

    // Se você habilitou o botão "Up" com setDisplayHomeAsUpEnabled(true)
    // e definiu parentActivityName no Manifest, este método trata o clique no botão "Up".
    // override fun onSupportNavigateUp(): Boolean {
    //     onBackPressedDispatcher.onBackPressed() // Comportamento padrão de voltar
    //     return true
    // }
}
