package com.ximoli.neurafruit.user

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.squareup.picasso.Picasso
import com.ximoli.neurafruit.R
import com.ximoli.neurafruit.databinding.ActivityHomeBinding
import com.ximoli.neurafruit.fragment.FavoriteFragment
import com.ximoli.neurafruit.fragment.HomeFragment
import com.ximoli.neurafruit.fragment.SettingFragment
import com.ximoli.neurafruit.util.ThemeUtils
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ThemeUtils.applyTheme(this)


        firebaseAuth = FirebaseAuth.getInstance()

        setupNavigationBar()
    }

    private fun setupNavigationBar() {
        val chipNavigationBar = findViewById<ChipNavigationBar>(R.id.chip_navigation_bar)
        chipNavigationBar.setOnItemSelectedListener { id ->
            when (id) {
                R.id.bar_home -> {
                    replaceFragment(HomeFragment())
                }
                R.id.bar_favorite -> {
                    replaceFragment(FavoriteFragment())
                }
                R.id.bar_settings -> {
                    replaceFragment(SettingFragment())
                }
            }
        }

        // Selecciona el primer fragmento por defecto
        chipNavigationBar.setItemSelected(R.id.bar_home, true)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

}
