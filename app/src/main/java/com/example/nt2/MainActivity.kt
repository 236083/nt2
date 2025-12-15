package com.example.nt2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nt2_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nt2_search -> {
                    replaceFragment(SearchFragment())
                    true
                }
                R.id.nt2_add -> {
                    replaceFragment(AddFragment())
                    true
                }
                R.id.nt2_reels -> {
                    replaceFragment(ReelsFragment())
                    true
                }
                R.id.nt2_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nt2_home
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }
}