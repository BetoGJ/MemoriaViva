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
    private lateinit var textViewUserWeightDisplay: TextView
    private lateinit var textViewUserSurgeriesDisplay: TextView
    private lateinit var textViewUserHospitalizationsDisplay: TextView
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
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate: View da MainActivity inflada.")

        sharedPreferences = getSharedPreferences(AppPreferencesKeys.PREFS_USER_DATA, Context.MODE_PRIVATE)

        initializeViews()
        Log.d(TAG, "onCreate: Views de UI inicializadas.")

        // A configuração da navegação deve ocorrer APÓS initializeViews
        // e idealmente ANTES de checkUserRegistrationStatusAndSetupUI se a UI depende dela.
        // No entanto, a lógica de setupNavigation em si não precisa do estado de registro,
        // mas a UI que ela controla (toolbar, menus) será mostrada/escondida depois.
        setupNavigation()
        Log.d(TAG, "onCreate: Navegação principal configurada.")

        registrationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "onActivityResult: Retorno da RegistrationActivity com resultado: ${result.resultCode}")
            if (result.resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "onActivityResult: Registro do usuário bem-sucedido.")
                // Importante: Após o registro, configure a UI para o estado registrado
                sharedPreferences.edit().putBoolean(AppPreferencesKeys.KEY_IS_USER_REGISTERED, true).apply() // Garanta que o estado seja salvo
            } else {
                Log.w(TAG, "onActivityResult: Registro do usuário cancelado ou falhou.")
            }
            // Sempre reavalie a UI após o retorno da RegistrationActivity
            checkUserRegistrationStatusAndSetupUI()
        }
        Log.d(TAG, "onCreate: ActivityResultLauncher para registro configurado.")

        // Verifica o status do usuário e configura a UI de acordo
        checkUserRegistrationStatusAndSetupUI()
        Log.i(TAG, "onCreate: MainActivity totalmente inicializada.")
    }

    private fun initializeViews() {
        toolbarMain = findViewById(R.id.toolbarMain)
        setSupportActionBar(toolbarMain) // Configura a Toolbar como ActionBar

        navHostContainer = findViewById(R.id.nav_host_container) // Referência ao container do NavHost

        // Views condicionais
        scrollViewUserInfo = findViewById(R.id.scrollViewUserInfo)
        textViewWelcome = findViewById(R.id.textViewWelcome)
        textViewUserNameDisplay = findViewById(R.id.textViewUserNameDisplay)
        textViewUserAgeDisplay = findViewById(R.id.textViewUserAgeDisplay)
        textViewUserWeightDisplay = findViewById(R.id.textViewUserWeightDisplay)
        textViewUserSurgeriesDisplay = findViewById(R.id.textViewUserSurgeriesDisplay)
        textViewUserHospitalizationsDisplay = findViewById(R.id.textViewUserHospitalizationsDisplay)
        buttonClearTestData = findViewById(R.id.buttonClearRegistrationTestData)
        textViewRedirecting = findViewById(R.id.textViewRedirectingToRegistration)

        buttonClearTestData.setOnClickListener {
            Log.d(TAG, "Botão 'Limpar Dados (Teste)' clicado.")
            clearRegistrationDataAndRestartCheck()
        }

        // Componentes de navegação principais (inicializados aqui, configurados em setupNavigation)
        drawerLayout = findViewById(R.id.drawer_layout) // Pode ser nulo
        bottomNavView = findViewById(R.id.nav_view_bottom) // Deve existir
        sideNavView = findViewById(R.id.nav_view_side)     // Pode ser nulo (se não houver drawer)
    }

    private fun setupNavigation() {
        Log.d(TAG, "setupNavigation: Iniciando configuração da navegação.")
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController
        Log.d(TAG, "setupNavigation: NavController obtido.")

        // Define os destinos de nível superior para o AppBarConfiguration.
        val topLevelDestinations = getTopLevelDestinationsFromMenus()

        appBarConfiguration = if (drawerLayout != null) {
            AppBarConfiguration(topLevelDestinations, drawerLayout)
        } else {
            AppBarConfiguration(topLevelDestinations)
        }
        Log.d(TAG, "setupNavigation: AppBarConfiguration criada com destinos: $topLevelDestinations e drawer: ${drawerLayout != null}")

        // Conecta a ActionBar (Toolbar) com o NavController
        setupActionBarWithNavController(navController, appBarConfiguration)
        Log.d(TAG, "setupNavigation: ActionBar (Toolbar) configurada com NavController.")

        // Conecta BottomNavigationView com o NavController
        bottomNavView.setupWithNavController(navController)
        Log.d(TAG, "setupNavigation: BottomNavigationView configurada com NavController.")

        // Conecta NavigationView (menu lateral) com o NavController (se existir)
        sideNavView?.setupWithNavController(navController)
        if (sideNavView != null) Log.d(TAG, "setupNavigation: NavigationView (lateral) configurada com NavController.")
        else Log.d(TAG, "setupNavigation: NavigationView (lateral) não encontrada ou não configurada.")

        // Listener para controlar a visibilidade do scrollViewUserInfo vs navHostContainer
        // Esta é uma abordagem. Outra é ter um "HomeFragment" que mostra os dados do usuário.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Adapte R.id.navigation_home ao ID real do seu destino "home" no nav_graph.xml
            // Se o seu destino home DEVE mostrar o scrollViewUserInfo:
            if (destination.id == R.id.navigation_home) { // SUBSTITUA PELO SEU ID DE DESTINO HOME
                scrollViewUserInfo.isVisible = true
                // Se a home é o scrollView, o navHostContainer pode ser escondido
                // ou o fragmento home simplesmente não infla nada visível.
                // Para clareza, se scrollViewUserInfo é a home, o fragmento associado a R.id.navigation_home
                // no nav_graph poderia ser um fragmento vazio ou um que não interfira.
                // navHostContainer.isVisible = false; // Descomente se a home for APENAS o scrollview
                Log.d(TAG, "Navigated to Home destination, showing user info scroll view.")
            } else {
                scrollViewUserInfo.isVisible = false
                // navHostContainer.isVisible = true; // Garante que está visível para outros fragmentos
                Log.d(TAG, "Navigated to other destination (${destination.label}), hiding user info scroll view.")
            }
        }
        Log.i(TAG, "setupNavigation: Configuração da navegação concluída.")
    }

    private fun getTopLevelDestinationsFromMenus(): Set<Int> {
        val destinations = mutableSetOf<Int>()
        bottomNavView.menu.let { menu ->
            for (i in 0 until menu.size()) {
                val item = menu.getItem(i)
                if (item.itemId != View.NO_ID && item.itemId != 0) {
                    destinations.add(item.itemId)
                }
            }
        }
        sideNavView?.menu?.let { menu ->
            for (i in 0 until menu.size()) {
                val item = menu.getItem(i)
                if (item.itemId != View.NO_ID && item.itemId != 0) {
                    destinations.add(item.itemId)
                }
            }
        }
        if (destinations.isEmpty()) {
            // Se nenhum destino for encontrado nos menus, adicione o startDestination do gráfico como fallback
            // ou defina um conjunto padrão. É importante ter pelo menos o startDestination.
            Log.w(TAG, "Nenhum destino de nível superior encontrado nos menus. Usando o startDestination do NavGraph.")
            destinations.add(navController.graph.startDestinationId)
        }
        Log.d(TAG, "setupNavigation: Destinos de nível superior definidos: $destinations")
        return destinations
    }


    private fun checkUserRegistrationStatusAndSetupUI() {
        val isRegistered = sharedPreferences.getBoolean(AppPreferencesKeys.KEY_IS_USER_REGISTERED, false)
        Log.d(TAG, "checkUserRegistrationStatusAndSetupUI: Usuário registrado? $isRegistered")

        if (isRegistered) {
            displayMainNavigationAndContentUI() // Nome mais descritivo
        } else {
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

        // Estado inicial do scrollViewUserInfo (se a home o mostra)
        // O addOnDestinationChangedListener em setupNavigation cuidará disso dinamicamente.
        // Forçamos o estado inicial aqui baseado no destino atual (que provavelmente é o startDestination).
        if (navController.currentDestination?.id == R.id.navigation_home) { // SUBSTITUA PELO SEU ID DE DESTINO HOME
            scrollViewUserInfo.isVisible = true
        } else {
            scrollViewUserInfo.isVisible = false
        }

        textViewRedirecting.isVisible = false // Esconder mensagem de redirecionamento

        drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED) // Desbloquear drawer

        loadAndDisplayUserData() // Carregar dados do usuário

        // O título da Toolbar será gerenciado pelo NavController + AppBarConfiguration
        Log.d(TAG, "displayMainNavigationAndContentUI: UI principal e de navegação configurada para usuário registrado.")
    }

    private fun loadAndDisplayUserData() {
        val name = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_NAME, "N/A")
        val age = sharedPreferences.getInt(AppPreferencesKeys.KEY_USER_AGE, 0)
        val weight = sharedPreferences.getFloat(AppPreferencesKeys.KEY_USER_WEIGHT, 0f)
        val surgeries = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_RECENT_SURGERIES, "")
        val hospitalizations = sharedPreferences.getString(AppPreferencesKeys.KEY_USER_RECENT_HOSPITALIZATIONS, "")

        textViewWelcome.text = getString(R.string.welcome_message, name)
        textViewUserNameDisplay.text = getString(R.string.user_name_display, name)
        textViewUserAgeDisplay.text = getString(R.string.user_age_display, age)
        textViewUserWeightDisplay.text = getString(R.string.user_weight_display, weight)
        textViewUserSurgeriesDisplay.text = getString(R.string.user_surgeries_display, surgeries?.ifEmpty { getString(R.string.none_reported) })
        textViewUserHospitalizationsDisplay.text = getString(R.string.user_hospitalizations_display, hospitalizations?.ifEmpty { getString(R.string.none_reported) })
        Log.d(TAG, "loadAndDisplayUserData: Dados do usuário carregados e exibidos.")
    }

    private fun redirectToRegistrationScreen() {
        Log.w(TAG, "redirectToRegistrationScreen: Usuário não registrado. Redirecionando.")

        // --- ESCONDER COMPONENTES DE NAVEGAÇÃO E CONTEÚDO PRINCIPAL ---
        toolbarMain.isVisible = false
        supportActionBar?.hide()

        navHostContainer.isVisible = false
        bottomNavView.isVisible = false
        scrollViewUserInfo.isVisible = false // Esconder também o scrollview de dados

        textViewRedirecting.isVisible = true // Mostrar mensagem de redirecionamento

        drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED) // Bloquear drawer

        try {
            val intent = Intent(this, RegistrationActivity::class.java) // Verifique se o nome da classe está correto
            registrationLauncher.launch(intent)
            Log.d(TAG, "redirectToRegistrationScreen: Intent para RegistrationActivity lançado.")
        } catch (e: Exception) {
            Log.e(TAG, "redirectToRegistrationScreen: Falha ao iniciar RegistrationActivity. Verifique o Manifest e o nome da classe.", e)
            textViewRedirecting.text = getString(R.string.registration_activity_start_error)
        }
    }

    private fun clearRegistrationDataAndRestartCheck() {
        Log.i(TAG, "clearRegistrationDataAndRestartCheck: Limpando dados de registro.")
        with(sharedPreferences.edit()) {
            // Limpa apenas as chaves relacionadas ao registro do usuário
            remove(AppPreferencesKeys.KEY_IS_USER_REGISTERED)
            remove(AppPreferencesKeys.KEY_USER_NAME)
            remove(AppPreferencesKeys.KEY_USER_AGE)
            remove(AppPreferencesKeys.KEY_USER_WEIGHT)
            remove(AppPreferencesKeys.KEY_USER_RECENT_SURGERIES)
            remove(AppPreferencesKeys.KEY_USER_RECENT_HOSPITALIZATIONS)
            apply()
        }
        Log.d(TAG, "clearRegistrationDataAndRestartCheck: Dados limpos. Verificando novamente o status do usuário.")
        checkUserRegistrationStatusAndSetupUI()
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
        Log.d(TAG, "onResume: Status do usuário e UI revalidados.");
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
