package com.example.memoriaviva2

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.memoriaviva2.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)  // chama logo após inflar o binding

        val drawerLayout = binding.drawerLayout
        val buttonMenu = binding.buttonMenu
        val navViewBottom: BottomNavigationView = binding.navViewBottom
        val navViewSide: NavigationView = binding.navViewSide

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            ),
            drawerLayout  // importante passar o drawerLayout para habilitar o hamburger
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navViewBottom.setupWithNavController(navController)
        navViewSide.setupWithNavController(navController)

        buttonMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(binding.navViewSide)) {
                drawerLayout.closeDrawer(binding.navViewSide)
            } else {
                drawerLayout.openDrawer(binding.navViewSide)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
