package com.ximoli.neurafruit.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.ximoli.neurafruit.R
import com.ximoli.neurafruit.databinding.ActivityEditProfileBinding
import com.ximoli.neurafruit.util.ThemeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private lateinit var binding: ActivityEditProfileBinding
private lateinit var firebaseAuth: FirebaseAuth
private lateinit var database: DatabaseReference
private var selectedImageView: ImageView? = null
private var selectedAvatarUrl: String? = null

private val avatars = arrayOf(
    "https://firebasestorage.googleapis.com/v0/b/neurafruit.appspot.com/o/avatars%2Fprofile_1.png?alt=media&token=4edca07f-f8a2-499d-9685-187e644fc8b7",
    "https://firebasestorage.googleapis.com/v0/b/neurafruit.appspot.com/o/avatars%2Fprofile_2.png?alt=media&token=29e58fb5-d188-4ca1-afaf-5f1ca6e639c8",
    "https://firebasestorage.googleapis.com/v0/b/neurafruit.appspot.com/o/avatars%2Fprofile_3.png?alt=media&token=676c31e4-8b53-4d31-a812-a8e6263c5af2",
    "https://firebasestorage.googleapis.com/v0/b/neurafruit.appspot.com/o/avatars%2Fprofile_4.png?alt=media&token=c5bca756-2759-4caf-8e03-838bb5f60a89",
    "https://firebasestorage.googleapis.com/v0/b/neurafruit.appspot.com/o/avatars%2Fprofile_5.png?alt=media&token=0cba1150-828e-40a3-9bdb-76b3c5ada4c6",
    "https://firebasestorage.googleapis.com/v0/b/neurafruit.appspot.com/o/avatars%2Fprofile_6.png?alt=media&token=0cba1150-828e-40a3-9bdb-76b3c5ada4c6"
)

var username = ""

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ThemeUtils.applyTheme(this)


        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.backIv.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.saveNameBtn.setOnClickListener {
            updateName()
        }

        binding.saveAvatarBtn.setOnClickListener {
            updateAvatar()
        }


        infoUserProfile()

    }

    private fun updateAvatar() {
        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["profile"] = selectedAvatarUrl

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        if (uid != null) {
            ref.child(uid)
                .updateChildren(hashMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Se actualizo su Avatar correctamente", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Falló: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateName() {
        username = binding.nameEt.text.toString().trim()
        if (!username.isEmpty()){
            updateUsername()
        }else{
            Toast.makeText(this, "El campo esta vacío", Toast.LENGTH_SHORT).show()

        }
    }

    private fun updateUsername() {

        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["username"] = username

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        if (uid != null) {
            ref.child(uid)
                .updateChildren(hashMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Se actualizo su nombre de usuario correctamente", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Falló: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun infoUserProfile() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        database.child("Users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val name = dataSnapshot.child("username").getValue(String::class.java)
                    val profileLink = dataSnapshot.child("profile").getValue(String::class.java)

                    binding.nameEt.setText(name)

                    // Seleccionar la imagen de perfil correspondiente
                    if (profileLink != null) {
                        setSelectedImageView(profileLink)
                    }

                } else {
                    Toast.makeText(this@EditProfileActivity, "User profile data not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@EditProfileActivity, "Error fetching user profile data: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setSelectedImageView(profileLink: String) {
        val selectedIndex = avatars.indexOf(profileLink)
        if (selectedIndex != -1) {
            when (selectedIndex) {
                0 -> selectedImageView = findViewById(R.id.image1)
                1 -> selectedImageView = findViewById(R.id.image2)
                2 -> selectedImageView = findViewById(R.id.image3)
                3 -> selectedImageView = findViewById(R.id.image4)
                4 -> selectedImageView = findViewById(R.id.image5)
                5 -> selectedImageView = findViewById(R.id.image6)
            }
            selectedImageView?.isSelected = true
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
