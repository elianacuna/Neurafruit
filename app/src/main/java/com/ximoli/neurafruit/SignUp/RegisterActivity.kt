package com.ximoli.neurafruit.SignUp

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ximoli.neurafruit.R
import com.ximoli.neurafruit.SignIn.MainActivity
import com.ximoli.neurafruit.databinding.ActivityRegisterBinding
import com.ximoli.neurafruit.user.HomeActivity
import com.ximoli.neurafruit.user.ProfileAddActivity
import com.ximoli.neurafruit.util.ThemeUtils
import java.sql.Timestamp


private lateinit var binding: ActivityRegisterBinding

var email  = ""
var password = ""
var confirm_password = ""
var  nextPage = ""
var nextData  = ""

private lateinit var firebaseAuth: FirebaseAuth
private  lateinit var progressDialog: ProgressDialog
class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ThemeUtils.applyTheme(this)


        firebaseAuth = FirebaseAuth.getInstance()
        nextData = intent.getStringExtra("backId").toString()


        //ProgressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere un momento...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backIv.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.nextBtn.setOnClickListener {
            validateData();
        }

        binding.agreeBtn.setOnClickListener {
            createaccount();

        }

        binding.backIv2.setOnClickListener {
            binding.addinfoRl.visibility = View.VISIBLE
            binding.termsRl.visibility = View.GONE
            nextPage = "Data"
        }

        colorTextTermino()

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePassword()

            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        binding.passwordEt.addTextChangedListener(textWatcher)



    }

    private fun validateData() {
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        confirm_password = binding.confirmPasswordEt.text.toString().trim()

        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (!password.isEmpty() && !confirm_password.isEmpty()) {
                if (password.length in 8..12 && confirm_password.length in 8..12) {
                    if (password == confirm_password) {
                        binding.addinfoRl.visibility = View.GONE
                        binding.termsRl.visibility = View.VISIBLE
                        nextPage = "Terms"

                    } else {
                        Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    // Handle invalid password length
                    Toast.makeText(
                        this,
                        "Las contraseñas deben tener entre 8 y 12 caracteres",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "Digite la contraseña", Toast.LENGTH_SHORT).show()

            }

        } else {
            Toast.makeText(this, "Digite bien el correo electronico", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createaccount() {

        progressDialog.setMessage("Creando Cuenta...")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
            updateUserInfo()
        }.addOnFailureListener {e->
            progressDialog.dismiss()
            Toast.makeText(this, "Fallio: ${e.message}", Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateUserInfo(){
        progressDialog.setMessage("Guardando su información")

        val timestamp = System.currentTimeMillis()

        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["username"] = ""
        hashMap["profile"] = ""
        hashMap["auth"] = "mail"
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        if (uid != null) {
            ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener {

                    val refFavorite = FirebaseDatabase.getInstance().getReference("UsersFavorite")

                    val hashMapFavorite: HashMap<String, Any?> = HashMap()
                    hashMap["uid"] = uid

                    refFavorite.child(uid)
                        .setValue(hashMapFavorite)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            startActivity(Intent(this, ProfileAddActivity::class.java))
                            finish()
                        }.addOnFailureListener {e->
                            progressDialog.dismiss()
                            Toast.makeText(this, "Fallio: ${e.message}", Toast.LENGTH_SHORT).show()
                        }


                }.addOnFailureListener {e->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Fallio: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun colorTextTermino() {
        val text = getString(R.string.termino)
        val spannableString = SpannableString(text)

        val colorPrimary = Color.parseColor("#056BEF")
        val colorSecondary = Color.parseColor("#949494")

        val wordsToColor = arrayOf(
            "Acepto",
            "Términos",
            "Política de Privacidad",
            "Política de Cookies",
            "Política de privacidad"
        )

        spannableString.setSpan(
            ForegroundColorSpan(colorSecondary),
            0,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        for (word in wordsToColor) {
            var startIndex = text.indexOf(word)
            while (startIndex != -1) {
                val endIndex = startIndex + word.length
                spannableString.setSpan(
                    ForegroundColorSpan(colorPrimary),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                startIndex = text.indexOf(word, endIndex)
            }
        }

        binding.textDescription.text = spannableString
    }

    private fun validatePassword() {
        password = binding.passwordEt.text.toString().trim()

        //para 8 a 12 caracteres
        if (password.length in 8..12) {
            binding.iv1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.check_blue))
        } else {
            binding.iv1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.check_gray))
        }

        //Mayuscila
        if (password.contains(Regex(".*[A-Z].*"))) {
            binding.iv2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.check_blue))
        } else {
            binding.iv2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.check_gray))
        }

        //Minuscula
        if (password.contains(Regex(".*[a-z].*"))) {
            binding.iv3.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.check_blue))
        } else {
            binding.iv3.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.check_gray))
        }

        //para numeros
        if (password.matches(Regex(".*[0-9].*"))) {
            binding.iv4.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.check_blue))
        } else {
            binding.iv4.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.check_gray))
        }

        //para simbolo
        val symbolRegex = Regex("^(?=.*[!@#\$%^&*()_+\\[\\]{}|;':\",./<>?`~\\-]).*$")
        if (password.matches(symbolRegex)) {
            binding.iv5.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.check_blue))
        } else {
            binding.iv5.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.check_gray))
        }
    }

    override fun onBackPressed() {
        if (nextPage == "Terms") {
            binding.addinfoRl.visibility = View.VISIBLE
            binding.termsRl.visibility = View.GONE
            nextPage = "Data"
        } else if (nextPage == "Data") {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }



}