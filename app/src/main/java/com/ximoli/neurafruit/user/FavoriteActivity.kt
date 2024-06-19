package com.ximoli.neurafruit.user

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ximoli.neurafruit.databinding.ActivityFavoriteBinding

class FavoriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var fruitId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("UsersFavorite").child(firebaseAuth.currentUser?.uid ?: "")

        fruitId = intent.getStringExtra("fruitId").toString()

        binding.savedIV.setOnClickListener {
            deleteFavorite()
        }


        getFruitDetails()
    }

    private fun deleteFavorite() {
        if (fruitId.isNotEmpty()) {
            database.child(fruitId).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Favorito borrado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al borrar el favorito: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "ID de fruta no proporcionado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFruitDetails() {
        if (fruitId.isNotEmpty()) {
            database.child(fruitId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").value.toString()
                        val family = snapshot.child("family").value.toString()
                        val order = snapshot.child("order").value.toString()
                        val genus = snapshot.child("genus").value.toString()
                        val calories = snapshot.child("calories").value.toString()
                        val fat = snapshot.child("fat").value.toString()
                        val sugar = snapshot.child("sugar").value.toString()
                        val carbohydrates = snapshot.child("carbohydrates").value.toString()
                        val imageUrl = snapshot.child("image_fruit").value.toString()

                        // Actualiza la UI con los datos obtenidos
                        binding.nameFruit.text = name
                        binding.familyTxt.text = "Familia: $family"
                        binding.orderTxt.text = "Orden: $order"
                        binding.genusTxt.text = "Género: $genus"
                        binding.caloriesTxt.text = calories
                        binding.fatTxt.text = fat
                        binding.sugarTxt.text = sugar
                        binding.carbohydratesTxt.text = carbohydrates

                        // Cargar la imagen con una biblioteca como Glide o Picasso
                        Glide.with(this@FavoriteActivity).load(imageUrl).into(binding.imageFruit)

                    } else {
                        Toast.makeText(this@FavoriteActivity, "Datos no encontrados", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@FavoriteActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "ID de fruta no proporcionado", Toast.LENGTH_SHORT).show()
        }
    }
}
