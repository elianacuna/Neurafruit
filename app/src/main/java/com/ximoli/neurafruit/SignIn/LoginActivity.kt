package com.ximoli.neurafruit.SignIn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.ximoli.neurafruit.databinding.ActivityLoginBinding
import com.ximoli.neurafruit.user.HomeActivity
import com.ximoli.neurafruit.user.RecoverPasswordActivity
import com.ximoli.neurafruit.util.ThemeUtils

private lateinit var binding: ActivityLoginBinding
var email = ""
var password = ""
private lateinit var firebaseAuth: FirebaseAuth
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ThemeUtils.applyTheme(this)


        firebaseAuth = FirebaseAuth.getInstance()

        binding.backIv.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.loginBtn.setOnClickListener {
            checkLogin()
        }

        binding.forgetBtn.setOnClickListener{
            startActivity(Intent(this, RecoverPasswordActivity::class.java))
            finish()
        }

    }

    // Suponiendo que tienes un método para registrar al usuario


    private fun checkLogin() {
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            if (!password.isEmpty()){
                login()
            }else{
                Toast.makeText(this, "Digite la contraseña", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, "Digite el correo electrónico", Toast.LENGTH_SHORT).show()
        }

    }

    private fun login() {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    val errorMessage = getSpanishErrorMessage(task.exception)
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {e->


            }
    }

    private fun getSpanishErrorMessage(e: Exception?): String {
        return when (e) {
            is FirebaseAuthInvalidUserException -> "Usuario no válido."
            is FirebaseAuthInvalidCredentialsException -> "Credenciales no válidas."
            else -> "Error desconocido: ${e?.message}"
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}