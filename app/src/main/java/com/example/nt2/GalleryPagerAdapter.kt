package com.example.nt2


import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class GalleryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TopsFragment()
            1 -> BottomsFragment()
            2 -> ShoesFragment()
            3 -> AccessoriesFragment()
            else -> TopsFragment()
        }
    }
}
