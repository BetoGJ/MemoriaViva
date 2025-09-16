package com.example.memoriaviva2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.SimpleDateFormat
import java.util.*

class RoutineReportActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routine_report)
        
        setupViews()
        setupToolbar()
        setupViewPager()
    }
    
    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }
    
    private fun setupViewPager() {
        val adapter = ReportPagerAdapter(this)
        viewPager.adapter = adapter
        
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -position)
            
            val dayFormat = SimpleDateFormat("dd/MM", Locale("pt", "BR"))
            val dayName = when (position) {
                0 -> "Hoje"
                1 -> "Ontem"
                else -> {
                    val weekFormat = SimpleDateFormat("EEE", Locale("pt", "BR"))
                    weekFormat.format(calendar.time)
                }
            }
            
            tab.text = "$dayName\n${dayFormat.format(calendar.time)}"
        }.attach()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}