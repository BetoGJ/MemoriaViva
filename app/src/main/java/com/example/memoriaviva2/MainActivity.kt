package com.example.memoriaviva2 // Substitua pelo seu nome de pacote real

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import androidx.core.view.isEmpty
import androidx.core.view.isVisible // Para fácil gerenciamento de visibilidade
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp // Para onSupportNavigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

// Defina sua classe AppPreferencesKeys em algum lugar, por exemplo:


class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    // Views para informações do usuário e redirecionamento (gerenciadas condicionalmente)
    private lateinit var scrollViewUserInfo: ScrollView
    private lateinit var textViewWelcome: TextView
    private lateinit var textViewUserNameDisplay: TextView
    private lateinit var textViewUserAgeDisplay: TextView
    private lateinit var textViewUserAddressDisplay: TextView
    private lateinit var textViewUserSurgeriesDisplay: TextView
    private lateinit var textViewUserHospitalizationsDisplay: TextView
    private lateinit var textViewUserComorbiditiesDisplay: TextView
    private lateinit var textViewUserAllergiesDisplay: TextView
    private lateinit var buttonEditRegistration: Button
    private lateinit var buttonClearTestData: Button
    private lateinit var textViewRedirecting: TextView

    // Componentes de Navegação
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var drawerLayout: DrawerLayout? = null // Pode não estar em todos os layouts de activity
    private lateinit var bottomNavView: BottomNavigationView // Assumindo que sempre existe se registrado
    private var sideNavView: NavigationView? = null // Menu lateral, dentro do DrawerLayout
    private lateinit var toolbarMain: Toolbar
    private lateinit var navHostContainer: FrameLayout // Container do NavHostFragment

    private lateinit var registrationLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Log device information for debugging
            DeviceCompatibilityHelper.logDeviceInfo()
            
            setContentView(R.layout.activity_main)
            Log.d(TAG, "onCreate: View da MainActivity inflada.")

            // Initialize SharedPreferences with error handling
            sharedPreferences = try {
                getSharedPreferences(AppPreferencesKeys.PREFS_USER_DATA, Context.MODE_PRIVATE)
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing SharedPreferences", e)
                // Fallback to default preferences
                getSharedPreferences("default_prefs", Context.MODE_PRIVATE)
            }

            initializeViews()
            Log.d(TAG, "onCreate: Views de UI inicializadas.")

            setupNavigation()
            Log.d(TAG, "onCreate: Navegação principal configurada.")

            registrationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                Log.d(TAG, "onActivityResult: Retorno da RegistrationActivity com resultado: ${result.resultCode}")
                if (result.resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "onActivityResult: Registro do usuário bem-sucedido.")
                    // Small delay to ensure data is written
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        checkUserRegistrationStatusAndSetupUI()
                    }, 100)
                } else {
                    Log.w(TAG, "onActivityResult: Registro do usuário cancelado ou falhou.")
                    checkUserRegistrationStatusAndSetupUI()
                }
            }
            Log.d(TAG, "onCreate: ActivityResultLauncher para registro configurado.")

            // Small delay before checking registration
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                checkUserRegistrationStatusAndSetupUI()
            }, 50)
            
            Log.i(TAG, "onCreate: MainActivity totalmente inicializada.")
            
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in onCreate", e)
            finish()
        }
    }

    private fun initializeViews() {
        try {
            toolbarMain = findViewById(R.id.toolbarMain)
            setSupportActionBar(toolbarMain)

            navHostContainer = findViewById(R.id.nav_host_container)
            scrollViewUserInfo = findViewById(R.id.scrollViewUserInfo)
            textViewWelcome = findViewById(R.id.textViewWelcome)
            textViewUserNameDisplay = findViewById(R.id.textViewUserNameDisplay)
            textViewUserAgeDisplay = findViewById(R.id.textViewUserAgeDisplay)
            textViewUserAddressDisplay = findViewById(R.id.textViewUserWeightDisplay)
            textViewUserSurgeriesDisplay = findViewById(R.id.textViewUserSurgeriesDisplay)
            textViewUserHospitalizationsDisplay = findViewById(R.id.textViewUserHospitalizationsDisplay)
            textViewUserComorbiditiesDisplay = findViewById(R.id.textViewUserComorbiditiesDisplay)
            textViewUserAllergiesDisplay = findViewById(R.id.textViewUserAllergiesDisplay)
            buttonEditRegistration = findViewById(R.id.buttonEditRegistration)
            buttonClearTestData = findViewById(R.id.buttonClearRegistrationTestData)
            textViewRedirecting = findViewById(R.id.textViewRedirectingToRegistration)

            buttonEditRegistration.setOnClickListener {
                openEditRegistration()
            }
            
            buttonClearTestData.setOnClickListener {
                clearRegistrationDataAndRestartCheck()
            }

            drawerLayout = findViewById(R.id.drawer_layout)
            bottomNavView = findViewById(R.id.nav_view_bottom)
            sideNavView = findViewById(R.id.nav_view_side)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            finish()
        }
    }

    private fun setupNavigation() {
        try {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment_activity_main) as? NavHostFragment
            if (navHostFragment == null) {
                Log.e(TAG, "NavHostFragment not found")
                return
            }
            navController = navHostFragment.navController

            val topLevelDestinations = setOf(
                R.id.navigation_home, R.id.navigation_contact, R.id.navigation_notifications, 
                R.id.navigation_backup, R.id.navigation_rastreio, R.id.item_1, R.id.item_2, 
                R.id.item_3, R.id.item_recreativa, R.id.item_monitoramento_saude
            )

            appBarConfiguration = if (drawerLayout != null) {
                AppBarConfiguration(topLevelDestinations, drawerLayout)
            } else {
                AppBarConfiguration(topLevelDestinations)
            }

            setupActionBarWithNavController(navController, appBarConfiguration)
            bottomNavView.setupWithNavController(navController)
            
            // Custom navigation handling for side menu
            sideNavView?.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_edit_registration -> {
                        openEditRegistration()
                        drawerLayout?.closeDrawers()
                        true
                    }
                    else -> {
                        try {
                            navController.navigate(menuItem.itemId)
                            bottomNavView.menu.setGroupCheckable(0, true, false)
                            for (i in 0 until bottomNavView.menu.size()) {
                                bottomNavView.menu.getItem(i).isChecked = false
                            }
                            bottomNavView.menu.setGroupCheckable(0, true, true)
                            drawerLayout?.closeDrawers()
                            true
                        } catch (e: Exception) {
                            Log.e(TAG, "Navigation error for menu item: ${menuItem.itemId}", e)
                            false
                        }
                    }
                }
            }
            
            // Ensure bottom navigation works after side navigation
            bottomNavView.setOnItemSelectedListener { item ->
                try {
                    navController.navigate(item.itemId)
                    // Clear side navigation selection
                    sideNavView?.checkedItem?.isChecked = false
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Bottom navigation error for item: ${item.itemId}", e)
                    false
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up navigation", e)
        }
    }




    private fun checkUserRegistrationStatusAndSetupUI() {
        try {
            Log.d(TAG, "checkUserRegistrationStatusAndSetupUI: Starting registration check")
            
            // Use safe SharedPreferences read
            val isRegistered = DeviceCompatibilityHelper.safeSharedPreferencesRead(
                this, AppPreferencesKeys.PREFS_USER_DATA, 
                AppPreferencesKeys.KEY_IS_USER_REGISTERED, false
            ) as Boolean
            
            val userName = DeviceCompatibilityHelper.safeSharedPreferencesRead(
                this, AppPreferencesKeys.PREFS_USER_DATA,
                AppPreferencesKeys.KEY_USER_NAME, ""
            ) as String
            
            val userAge = DeviceCompatibilityHelper.safeSharedPreferencesRead(
                this, AppPreferencesKeys.PREFS_USER_DATA,
                AppPreferencesKeys.KEY_USER_AGE, 0
            ) as Int
            
            // Triple check - user must be registered, have a name, and valid age
            val isValidRegistration = isRegistered && userName.isNotEmpty() && userAge > 0
            
            Log.i(TAG, "Registration Status:")
            Log.i(TAG, "  Registered: $isRegistered")
            Log.i(TAG, "  Name: '$userName' (length: ${userName.length})")
            Log.i(TAG, "  Age: $userAge")
            Log.i(TAG, "  Valid: $isValidRegistration")

            if (isValidRegistration) {
                Log.i(TAG, "Valid registration found, showing main UI")
                displayMainNavigationAndContentUI()
            } else {
                // Clear invalid registration data
                if (isRegistered && (userName.isEmpty() || userAge <= 0)) {
                    Log.w(TAG, "Invalid registration detected (registered=$isRegistered, name='$userName', age=$userAge), clearing data")
                    clearRegistrationDataAndRestartCheck()
                    return
                }
                Log.i(TAG, "No valid registration, redirecting to registration screen")
                redirectToRegistrationScreen()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking registration status", e)
            Log.e(TAG, "Exception details: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            redirectToRegistrationScreen()
        }
    }

    private fun displayMainNavigationAndContentUI() {
        Log.i(TAG, "displayMainNavigationAndContentUI: Configurando UI para usuário registrado.")

        // --- MOSTRAR COMPONENTES DE NAVEGAÇÃO E CONTEÚDO PRINCIPAL ---
        toolbarMain.isVisible = true
        supportActionBar?.show()

        navHostContainer.isVisible = true // Container dos fragmentos deve estar visível
        bottomNavView.isVisible = true   // Menu inferior visível

        scrollViewUserInfo.isVisible = false

        textViewRedirecting.isVisible = false // Esconder mensagem de redirecionamento

        drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED) // Desbloquear drawer

        loadAndDisplayUserData() // Carregar dados do usuário

        // O título da Toolbar será gerenciado pelo NavController + AppBarConfiguration
        Log.d(TAG, "displayMainNavigationAndContentUI: UI principal e de navegação configurada para usuário registrado.")
    }

    private fun loadAndDisplayUserData() {
        val name = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_NAME, "N/A")
        val age = sharedPreferences.getInt(AppPreferencesKeys.KEY_USER_AGE, 0)
        val address = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_ADDRESS, "")
        val surgeries = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_RECENT_SURGERIES, "")
        val hospitalizations = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_RECENT_HOSPITALIZATIONS, "")
        val comorbidities = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_COMORBIDITIES, "")
        val allergies = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_ALLERGIES, "")

        textViewWelcome.text = getString(R.string.welcome_message, name)
        textViewUserNameDisplay.text = getString(R.string.user_name_display, name)
        textViewUserAgeDisplay.text = getString(R.string.user_age_display, age)
        textViewUserAddressDisplay.text = getString(R.string.user_address_display, address?.ifEmpty { getString(R.string.none_reported) })
        textViewUserSurgeriesDisplay.text = getString(R.string.user_surgeries_display, surgeries?.ifEmpty { getString(R.string.none_reported) })
        textViewUserHospitalizationsDisplay.text = getString(R.string.user_hospitalizations_display, hospitalizations?.ifEmpty { getString(R.string.none_reported) })
        textViewUserComorbiditiesDisplay.text = getString(R.string.user_comorbidities_display, comorbidities?.ifEmpty { getString(R.string.none_reported) })
        textViewUserAllergiesDisplay.text = getString(R.string.user_allergies_display, allergies?.ifEmpty { getString(R.string.none_reported) })
        Log.d(TAG, "loadAndDisplayUserData: Dados do usuário carregados e exibidos.")
    }

    private fun redirectToRegistrationScreen() {
        Log.w(TAG, "redirectToRegistrationScreen: Usuário não registrado. Redirecionando.")

        try {
            // --- ESCONDER COMPONENTES DE NAVEGAÇÃO E CONTEÚDO PRINCIPAL ---
            toolbarMain.isVisible = false
            supportActionBar?.hide()

            navHostContainer.isVisible = false
            bottomNavView.isVisible = false
            scrollViewUserInfo.isVisible = false

            textViewRedirecting.isVisible = true
            textViewRedirecting.text = "Nenhum paciente registrado. Redirecionando para o cadastro..."

            drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            // Small delay for UI updates before launching activity
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    val intent = Intent(this, RegistrationActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    registrationLauncher.launch(intent)
                    Log.d(TAG, "redirectToRegistrationScreen: Intent para RegistrationActivity lançado.")
                } catch (e: Exception) {
                    Log.e(TAG, "redirectToRegistrationScreen: Falha ao iniciar RegistrationActivity.", e)
                    textViewRedirecting.text = "Erro ao iniciar cadastro. Toque para tentar novamente."
                    textViewRedirecting.setOnClickListener {
                        redirectToRegistrationScreen()
                    }
                }
            }, 100)
            
        } catch (e: Exception) {
            Log.e(TAG, "redirectToRegistrationScreen: Erro geral", e)
            textViewRedirecting.text = "Erro no sistema. Reinicie o aplicativo."
        }
    }

    private fun clearRegistrationDataAndRestartCheck() {
        Log.i(TAG, "clearRegistrationDataAndRestartCheck: Limpando dados de registro.")
        with(sharedPreferences.edit()) {
            // Limpa apenas as chaves relacionadas ao registro do usuário
            remove(AppPreferencesKeys.KEY_IS_USER_REGISTERED)
            remove(AppPreferencesKeys.KEY_USER_NAME)
            remove(AppPreferencesKeys.KEY_USER_AGE)
            remove(AppPreferencesKeys.KEY_USER_ADDRESS)
            remove(AppPreferencesKeys.KEY_USER_RECENT_SURGERIES)
            remove(AppPreferencesKeys.KEY_USER_RECENT_HOSPITALIZATIONS)
            apply()
        }
        Log.d(TAG, "clearRegistrationDataAndRestartCheck: Dados limpos. Verificando novamente o status do usuário.")
        checkUserRegistrationStatusAndSetupUI()
    }
    
    private fun openEditRegistration() {
        try {
            val intent = Intent(this, RegistrationActivity::class.java)
            intent.putExtra("EDIT_MODE", true)
            registrationLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao abrir edição de cadastro", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d(TAG, "onSupportNavigateUp: Navegação 'Up' solicitada.")
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: MainActivity está resumindo.")
        // Revalidar o estado da UI pode ser útil se o estado de registro
        // puder mudar enquanto a activity está pausada, mas não por meio do fluxo de registrationLauncher.
        // Se registrationLauncher é o único caminho para mudar KEY_IS_USER_REGISTERED,
        // esta chamada pode ser redundante ou causar uma reconfiguração da UI desnecessária.
        // Avalie com base no seu fluxo completo. Por segurança, manter pode ser bom.
        checkUserRegistrationStatusAndSetupUI()
        Log.d(TAG, "onResume: Status do usuário e UI revalidados.")
    }

    // Não é necessário sobrescrever onPause, onStop, onDestroy apenas para logs,
    // a menos que você tenha limpeza específica para fazer.
}

// Lembre-se de definir as strings em res/values/strings.xml:
/*
<resources>
    <string name="welcome_message">Bem-vindo(a), %1$s!</string>
    <string name="user_name_display">Nome: %1$s</string>
    <string name="user_age_display">Idade: %1$d anos</string>
    <string name="user_weight_display">Peso: %.1f kg</string>
    <string name="user_surgeries_display">Cirurgias Recentes: %1$s</string>
    <string name="user_hospitalizations_display">Internações Recentes: %1$s</string>
    <string name="none_reported">Nenhuma informada</string>
    <string name="patient_data_title">Dados do Paciente</string>
    <string name="registration_activity_start_error">Erro ao iniciar cadastro. Tente novamente.</string>

    <!-- Adicione o ID do seu destino home aqui se for usá-lo no código Kotlin -->
    <!-- Exemplo: Se o seu fragmento home no nav_graph tem android:id="@+id/navigation_home" -->
    <!-- <item name="navigation_home" type="id"/> -->
</resources>
*/

// E certifique-se que sua RegistrationActivity existe e está no Manifest:
// <activity android:name=".RegistrationActivity" />
