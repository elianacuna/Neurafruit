package com.ximoli.neurafruit

import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ximoli.neurafruit.SignIn.MainActivity
import com.ximoli.neurafruit.Utils.NetworkUtils
import com.ximoli.neurafruit.user.HomeActivity
import com.ximoli.neurafruit.user.ImageActivity
import com.ximoli.neurafruit.util.ThemeUtils

private lateinit var firebaseAuth: FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen()
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        ThemeUtils.applyTheme(this)

        screenSplash.setKeepOnScreenCondition { true }

        if (NetworkUtils.isConnectedToInternet(this)) {
            Handler(Looper.getMainLooper()).postDelayed({
                checkUser()
            }, 1000)
        } else {
            // Mostrar una actividad o mensaje de error indicando que no hay conexión a Internet
            startActivity(Intent(this, NoInternetActivity::class.java))
            finish()
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val auth = snapshot.child("auth").value

                        if (auth == "mail" || auth == "Google") {
                            startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Manejar error de base de datos
                    }
                })
        }
    }
}
