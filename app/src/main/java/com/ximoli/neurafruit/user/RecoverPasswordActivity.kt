package com.ximoli.neurafruit.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.ximoli.neurafruit.R
import com.ximoli.neurafruit.SignIn.LoginActivity
import com.ximoli.neurafruit.databinding.ActivityRecoverPasswordBinding
import com.ximoli.neurafruit.util.ThemeUtils

class RecoverPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecoverPasswordBinding
    var email  = ""
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoverPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ThemeUtils.applyTheme(this)


        firebaseAuth = FirebaseAuth.getInstance()

        binding.sendBtn.setOnClickListener {
            sendEmailRecoverPassword()
        }

        binding.backIv.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun sendEmailRecoverPassword() {
        email = binding.emailEt.text.toString().trim()
        if (email.isNotEmpty()) {
            resetPassword()
        } else {
            Toast.makeText(this, "Por favor ingrese su correo electrónico", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun resetPassword() {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Correo de restablecimiento enviado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al enviar el correo de restablecimiento", Toast.LENGTH_SHORT).show()
                }
            }
    }
}