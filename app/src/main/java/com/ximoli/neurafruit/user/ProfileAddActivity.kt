package com.ximoli.neurafruit.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ximoli.neurafruit.R
import com.ximoli.neurafruit.SignUp.email
import com.ximoli.neurafruit.databinding.ActivityProfileAddBinding
import com.ximoli.neurafruit.util.ThemeUtils

class ProfileAddActivity : AppCompatActivity() {

    private var selectedImageView: ImageView? = null

    private lateinit var binding: ActivityProfileAddBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private val avatars = arrayOf(
        "https://firebasestorage.googleapis.com/v0/b/neurafruit.appspot.com/o/avatars%2Fprofile_1.png?alt=media&token=4edca07f-f8a2-499d-9685-187e644fc8b7",
        "https://firebasestorage.googleapis.com/v0/b/neurafruit.appspot.com/o/avatars%2Fprofile_2.png?alt=media&token=29e58fb5-d188-4ca1-afaf-5f1ca6e639c8",
        "https://firebasestorage.googleapis.com/v0/b/neurafruit.appspot.com/o/avatars%2Fprofile_3.png?alt=media&token=676c31e4-8b53-4d31-a812-a8e6263c5af2",
        "https://firebasestorage.googleapis.com/v0/b/neurafruit.appspot.com/o/avatars%2Fprofile_4.png?alt=media&token=c5bca756-2759-4caf-8e03-838bb5f60a89",
        "https://firebasestorage.googleapis.com/v0/b/neurafruit.appspot.com/o/avatars%2Fprofile_5.png?alt=media&token=0cba1150-828e-40a3-9bdb-76b3c5ada4c6",
        "https://firebasestorage.googleapis.com/v0/b/neurafruit.appspot.com/o/avatars%2Fprofile_6.png?alt=media&token=0cba1150-828e-40a3-9bdb-76b3c5ada4c6"
    )

    private var selectedAvatarUrl: String? = null

    var name = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ThemeUtils.applyTheme(this)


        firebaseAuth = FirebaseAuth.getInstance()

        binding.saveBtn.setOnClickListener {
            updateProfile()
        }

    }

    private fun updateProfile() {
        name = binding.nameEt.text.toString().trim()

        if (name.isNotEmpty()) {
            if (selectedAvatarUrl?.isNotEmpty() == true) {
                updateProfileDb()
            } else {
                Toast.makeText(this, "Seleccione un avatar", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Digite su nombre de usuario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfileDb() {


        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["username"] = name
        hashMap["profile"] = selectedAvatarUrl

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        if (uid != null) {
            ref.child(uid)
                .updateChildren(hashMap)
                .addOnSuccessListener {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Falló: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    fun onImageClick(view: View) {
        selectedImageView?.isSelected = false

        selectedImageView = view as ImageView
        selectedImageView!!.isSelected = true

        val selectedIndex = when (view.id) {
            R.id.image1 -> 0
            R.id.image2 -> 1
            R.id.image3 -> 2
            R.id.image4 -> 3
            R.id.image5 -> 4
            R.id.image6 -> 5
            else -> -1
        }

        if (selectedIndex != -1) {
            selectedAvatarUrl = avatars[selectedIndex]
        }
    }
}