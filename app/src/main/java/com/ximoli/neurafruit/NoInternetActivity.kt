package com.ximoli.neurafruit

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.ximoli.neurafruit.SignIn.MainActivity
import com.ximoli.neurafruit.Utils.NetworkUtils
import com.ximoli.neurafruit.databinding.ActivityNoInternetBinding
import com.ximoli.neurafruit.user.HomeActivity
import com.ximoli.neurafruit.util.ThemeUtils

class NoInternetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoInternetBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoInternetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ThemeUtils.applyTheme(this)


        binding.retryBtn.setOnClickListener {
            if (NetworkUtils.isConnectedToInternet(this)) {
               goToAppropriateActivity()
            } else {

                Snackbar.make(binding.root, "No hay conexión a Internet", Snackbar.LENGTH_SHORT).show()
            }
        }


    }

    private fun goToAppropriateActivity() {
        // Aquí debes definir la lógica para determinar a qué actividad ir.
        // Supongamos que usas una variable boolean para la decisión.
        val goToHome = true // Reemplaza esto con tu lógica

        val intent = if (goToHome) {
            Intent(this, HomeActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        finish() // Opcional: finalizar la actividad actual si es necesario
    }

}
