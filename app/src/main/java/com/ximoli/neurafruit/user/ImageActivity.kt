package com.ximoli.neurafruit.user

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ximoli.neurafruit.databinding.ActivityImageBinding
import com.ximoli.neurafruit.util.ThemeUtils
import java.net.URLEncoder

class ImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageBinding
    private lateinit var queue: com.android.volley.RequestQueue
    private lateinit var firebaseAuth: FirebaseAuth
    private var imageUri: Uri? = null
    val timestamp = System.currentTimeMillis()

    var name = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeUtils.applyTheme(this)

        firebaseAuth = FirebaseAuth.getInstance()

        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        queue = Volley.newRequestQueue(this)

        name = intent.getStringExtra("fruitName").toString()



        // Retrieve the image URI from the intent
        val imageUriString = intent.getStringExtra("imageUri")
        if (imageUriString != null) {
            try {
                imageUri = Uri.parse(imageUriString)
                binding.imageFruit.setImageURI(imageUri)
            } catch (e: Exception) {
                Toast.makeText(this, "Error al configurar el URI de la imagen", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No se proporcionó ningún URI de imagen", Toast.LENGTH_SHORT).show()
        }

        binding.backIv.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        infoApiFruit()
    }

    private fun infoApiFruit() {
        val encodedFruitName = URLEncoder.encode(name, "UTF-8")
        val url = "https://www.fruityvice.com/api/fruit/$encodedFruitName"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val name = response.getString("name")
                val family = response.getString("family")
                val order = response.getString("order")
                val genus = response.getString("genus")

                val nutritions = response.getJSONObject("nutritions")
                val calories = nutritions.getString("calories")
                val fat = nutritions.getString("fat")
                val sugar = nutritions.getString("sugar")
                val carbohydrates = nutritions.getString("carbohydrates")

                binding.nameFruit.text = name
                binding.familyTxt.text = "Familia: $family"
                binding.orderTxt.text = "Orden: $order"
                binding.genusTxt.text = "Género: $genus"
                binding.caloriesTxt.text = calories
                binding.fatTxt.text = fat
                binding.sugarTxt.text = sugar
                binding.carbohydratesTxt.text = carbohydrates

                binding.saveIV.setOnClickListener {
                    saveInfo(name, family, order, genus, calories, fat, sugar, carbohydrates)
                }
            },
            { error ->
                finish()
                Toast.makeText(this, "No se detecto la imagen correctamente", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }

    private fun saveInfo(name: String, family: String, order: String, genus: String, calories: String, fat: String, sugar: String, carbohydrates: String) {
        imageUri?.let {
            val filePathAndName = "Fruit/" + timestamp // Guardar en una carpeta llamada "Fruit"

            val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
            val uploadTask = reference.putFile(it)

            // Mostrar ProgressBar
            binding.progressBar.visibility = View.VISIBLE
            val color = Color.parseColor("#FFFFFF")
            binding.progressBar.indeterminateDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)


            uploadTask.addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                uriTask.addOnCompleteListener { downloadTask ->
                    if (downloadTask.isSuccessful) {
                        val downloadUrl = downloadTask.result.toString()
                        saveImageUrlToDatabase(downloadUrl, name, family, order, genus, calories, fat, sugar, carbohydrates)
                    } else {
                        Toast.makeText(this, "Error al obtener URL de descarga", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
            }

        } ?: run {
            Toast.makeText(this, "No se proporcionó ningún URI de imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageUrlToDatabase(downloadUrl: String, name: String, family: String, order: String, genus: String, calories: String, fat: String, sugar: String, carbohydrates: String) {
        val id = firebaseAuth.uid


        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["id"] = "$timestamp"
        hashMap["image_fruit"] = downloadUrl
        hashMap["name"] = name
        hashMap["family"] = family
        hashMap["order"] = order
        hashMap["genus"] = genus
        hashMap["calories"] = calories
        hashMap["fat"] = fat
        hashMap["sugar"] = sugar
        hashMap["carbohydrates"] = carbohydrates
        hashMap["uid"] = id

        val ref = FirebaseDatabase.getInstance().getReference("UsersFavorite")
        ref.child(firebaseAuth.uid!!).child("" + timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                // Verificar si la actividad está en un estado adecuado antes de actualizar la UI
                if (!isFinishing) {
                    runOnUiThread {
                        Toast.makeText(this, "Se guardó en favorito correctamente", Toast.LENGTH_SHORT).show()
                        binding.saveIV.visibility = View.GONE
                        binding.savedIV.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }.addOnFailureListener { e ->
                if (!isFinishing) {
                    runOnUiThread {
                        Toast.makeText(this, "Falló: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
