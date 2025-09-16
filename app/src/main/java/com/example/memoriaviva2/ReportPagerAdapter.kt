package com.example.memoriaviva2

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ReportPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    
    override fun getItemCount(): Int = 7
    
    override fun createFragment(position: Int): Fragment {
        return DailyReportFragment.newInstance(position)
    }
}