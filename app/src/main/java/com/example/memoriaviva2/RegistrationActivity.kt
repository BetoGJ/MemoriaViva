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
    private var isEditMode = false

    private lateinit var toolbarRegistration: Toolbar // View para a Toolbar
    private lateinit var textInputLayoutNameReg: TextInputLayout
    private lateinit var editTextNameReg: TextInputEditText
    private lateinit var textInputLayoutAgeReg: TextInputLayout
    private lateinit var editTextAgeReg: TextInputEditText
    private lateinit var textInputLayoutAddressReg: TextInputLayout
    private lateinit var editTextAddressReg: TextInputEditText
    private lateinit var editTextRecentSurgeriesReg: TextInputEditText
    private lateinit var editTextRecentHospitalizationsReg: TextInputEditText
    private lateinit var editTextComorbiditiesReg: TextInputEditText
    private lateinit var editTextAllergiesReg: TextInputEditText
    private lateinit var buttonSaveRegistration: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_registration)

            // Initialize SharedPreferences with error handling
            sharedPreferences = try {
                getSharedPreferences(AppPreferencesKeys.PREFS_USER_DATA, Context.MODE_PRIVATE)
            } catch (e: Exception) {
                // Fallback to default preferences if there's an issue
                getSharedPreferences("default_prefs", Context.MODE_PRIVATE)
            }

            // Check if this is edit mode
            isEditMode = intent.getBooleanExtra("EDIT_MODE", false)
            
            // If not edit mode and already registered, finish
            if (!isEditMode && sharedPreferences.getBoolean(AppPreferencesKeys.KEY_IS_USER_REGISTERED, false)) {
                Toast.makeText(this, "Usuário já cadastrado!", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
                return
            }

            // Initialize toolbar
            toolbarRegistration = findViewById(R.id.toolbarRegistration)
            setSupportActionBar(toolbarRegistration)
            
            // Set title based on mode
            if (isEditMode) {
                toolbarRegistration.title = "Editar Cadastro do Paciente"
            }

            // Initialize views with error handling
            try {
                textInputLayoutNameReg = findViewById(R.id.textInputLayoutNameReg)
                editTextNameReg = findViewById(R.id.editTextNameReg)
                textInputLayoutAgeReg = findViewById(R.id.textInputLayoutAgeReg)
                editTextAgeReg = findViewById(R.id.editTextAgeReg)
                textInputLayoutAddressReg = findViewById(R.id.textInputLayoutWeightReg)
                editTextAddressReg = findViewById(R.id.editTextWeightReg)
                editTextRecentSurgeriesReg = findViewById(R.id.editTextRecentSurgeriesReg)
                editTextRecentHospitalizationsReg = findViewById(R.id.editTextRecentHospitalizationsReg)
                editTextComorbiditiesReg = findViewById(R.id.editTextComorbiditiesReg)
                editTextAllergiesReg = findViewById(R.id.editTextAllergiesReg)
                buttonSaveRegistration = findViewById(R.id.buttonSaveRegistration)

                // Set button text based on mode
                if (isEditMode) {
                    buttonSaveRegistration.text = "ATUALIZAR CADASTRO"
                    loadExistingData()
                }
                
                buttonSaveRegistration.setOnClickListener {
                    validateAndSaveData()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Erro ao inicializar formulário", Toast.LENGTH_LONG).show()
                finish()
            }
            
        } catch (e: Exception) {
            Toast.makeText(this, "Erro crítico na inicialização", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun validateAndSaveData() {
        // Disable button to prevent multiple clicks
        buttonSaveRegistration.isEnabled = false
        
        val name = editTextNameReg.text.toString().trim()
        val ageStr = editTextAgeReg.text.toString().trim()
        val address = editTextAddressReg.text.toString().trim()
        val surgeries = editTextRecentSurgeriesReg.text.toString().trim()
        val hospitalizations = editTextRecentHospitalizationsReg.text.toString().trim()
        val comorbidities = editTextComorbiditiesReg.text.toString().trim()
        val allergies = editTextAllergiesReg.text.toString().trim()

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

        if (address.isEmpty()) {
            textInputLayoutAddressReg.error = "Endereço é obrigatório"
            isValid = false
        } else {
            textInputLayoutAddressReg.error = null
        }

        if (!isValid) {
            buttonSaveRegistration.isEnabled = true // Re-enable button
            Toast.makeText(this, "Por favor, corrija os campos marcados.", Toast.LENGTH_LONG).show()
            return
        }

        // Use DeviceCompatibilityHelper for safer SharedPreferences operations
        val success = DeviceCompatibilityHelper.safeSharedPreferencesWrite(sharedPreferences) { editor ->
            editor.putBoolean(AppPreferencesKeys.KEY_IS_USER_REGISTERED, true)
            editor.putString(AppPreferencesKeys.KEY_USER_NAME, name)
            editor.putInt(AppPreferencesKeys.KEY_USER_AGE, age!!)
            editor.putString(AppPreferencesKeys.KEY_USER_ADDRESS, address)
            editor.putString(AppPreferencesKeys.KEY_USER_RECENT_SURGERIES, surgeries)
            editor.putString(AppPreferencesKeys.KEY_USER_RECENT_HOSPITALIZATIONS, hospitalizations)
            editor.putString(AppPreferencesKeys.KEY_USER_COMORBIDITIES, comorbidities)
            editor.putString(AppPreferencesKeys.KEY_USER_ALLERGIES, allergies)
        }
        
        if (success) {
            val message = if (isEditMode) "Dados atualizados com sucesso!" else "Paciente registrado com sucesso!"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            
            // Small delay before finishing to ensure data is written
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                setResult(Activity.RESULT_OK)
                finish()
            }, 50)
        } else {
            buttonSaveRegistration.isEnabled = true // Re-enable button on error
            Toast.makeText(this, "Erro ao salvar dados. Tente novamente.", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun loadExistingData() {
        try {
            val name = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_NAME, "")
            val age = sharedPreferences.getInt(AppPreferencesKeys.KEY_USER_AGE, 0)
            val address = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_ADDRESS, "")
            val surgeries = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_RECENT_SURGERIES, "")
            val hospitalizations = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_RECENT_HOSPITALIZATIONS, "")
            val comorbidities = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_COMORBIDITIES, "")
            val allergies = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_ALLERGIES, "")
            
            editTextNameReg.setText(name)
            if (age > 0) editTextAgeReg.setText(age.toString())
            editTextAddressReg.setText(address)
            editTextRecentSurgeriesReg.setText(surgeries)
            editTextRecentHospitalizationsReg.setText(hospitalizations)
            editTextComorbiditiesReg.setText(comorbidities)
            editTextAllergiesReg.setText(allergies)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao carregar dados existentes", Toast.LENGTH_SHORT).show()
        }
    }

    // Se você habilitou o botão "Up" com setDisplayHomeAsUpEnabled(true)
    // e definiu parentActivityName no Manifest, este método trata o clique no botão "Up".
    // override fun onSupportNavigateUp(): Boolean {
    //     onBackPressedDispatcher.onBackPressed() // Comportamento padrão de voltar
    //     return true
    // }
}
