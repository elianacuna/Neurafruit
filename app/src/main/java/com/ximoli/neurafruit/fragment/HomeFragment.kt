package com.ximoli.neurafruit.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.ximoli.neurafruit.R
import com.ximoli.neurafruit.bottomSheet.SelectionPhoto
import com.ximoli.neurafruit.databinding.FragmentHomeBinding
import com.ximoli.neurafruit.user.ImageActivity
import com.ximoli.neurafruit.util.ThemeUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentPhotoPath: String
    private lateinit var database: DatabaseReference

    private val selectionPhoto = SelectionPhoto.newInstance()

    private val openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            binding.fruitIv.setImageURI(it)
            binding.fruitIv.tag = it  // Store URI in tag
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val uri = Uri.fromFile(File(currentPhotoPath))
            binding.fruitIv.setImageURI(uri)
            binding.fruitIv.tag = uri  // Store URI in tag
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize the binding
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ThemeUtils.applyTheme(requireContext())

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.searchBtn.setOnClickListener {
            searchFruit()
        }

        binding.fruitIv.setOnClickListener {
            showSelectionPhoto()
        }

        requestCameraPermission()
        infoUserProfile()
    }

    private fun infoUserProfile() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        database.child("Users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val name = dataSnapshot.child("username").getValue(String::class.java)
                    val profileLink = dataSnapshot.child("profile").getValue(String::class.java)

                    binding.nameTxt.text = name
                    val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                    binding.dateTxt.text = formattedDate

                    if (profileLink != null) {
                        Picasso.get().load(profileLink)
                            .error(R.drawable.logo)
                            .into(binding.profileIv)
                    }
                } else {
                    Toast.makeText(context, "User profile data not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "Error fetching user profile data: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun requestCameraPermission() {
        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permisos concedidos
            } else {
                Toast.makeText(context, "Se requieren permisos de cámara y almacenamiento para usar esta función", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun takePhoto() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(context, "Error creando archivo: ${ex.message}", Toast.LENGTH_SHORT).show()
            null
        }
        photoFile?.also {
            val photoURI: Uri? = context?.let { context ->
                FileProvider.getUriForFile(
                    context,
                    "com.ximoli.neurafruit.fileprovider",
                    it
                )
            }
            currentPhotoPath = it.absolutePath
            takePicture.launch(photoURI)
        } ?: run {
            Toast.makeText(context, "Error al crear el archivo de imagen", Toast.LENGTH_SHORT).show()
        }
    }

    fun selectGallery() {
        selectionPhoto.dismiss()
        openDocumentLauncher.launch(arrayOf("image/*"))
    }

    private fun showSelectionPhoto() {
        selectionPhoto.show(childFragmentManager, "selection_photo")
    }

    private fun searchFruit() {
        val imageUri = binding.fruitIv.tag as? Uri  // Retrieve the URI from the tag
        if (imageUri == null) {
            Toast.makeText(context, "Debes de tomar o seleccionar una foto", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("HomeFragment", "Image URI: $imageUri")
            binding.progressBar.visibility = View.VISIBLE
            val color = Color.parseColor("#1877F2")
            binding.progressBar.indeterminateDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)

            sendImageToApi(imageUri)
        }
    }

    private fun sendImageToApi(imageUri: Uri) {
        val database = FirebaseDatabase.getInstance().reference
        val apiUrlRef = database.child("Api").child("mMqIqrwJzBQlmkfRDJiU8TWMXLh1").child("apiname")

        apiUrlRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val apiUrl = dataSnapshot.getValue(String::class.java) ?: ""
                if (apiUrl.isNotEmpty()) {
                    sendImage(apiUrl, imageUri)
                } else {
                    Log.e("HomeFragment", "Error: API URL not found in Firebase.")
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Error: API URL not found in Firebase.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("HomeFragment", "Error fetching API URL from Firebase: ${databaseError.message}")
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error fetching API URL from Firebase: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun sendImage(apiUrl: String, imageUri: Uri) {
        val file = getFileFromUri(imageUri)
        if (file == null || !file.exists()) {
            Log.e("HomeFragment", "Error: File does not exist or cannot be retrieved.")
            activity?.runOnUiThread {
                Toast.makeText(context, "Por favor selecciona una imagen válida.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .addHeader("Authorization", Credentials.basic("admin", "password")) // Adding authorization header
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HomeFragment", "Error sending image: ${e.message}")
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error enviando la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("HomeFragment", "Response: $responseBody")

                    val json = JSONObject(responseBody)
                    val fruitName = json.getJSONObject("info").getString("name")

                    activity?.runOnUiThread {
                        goToImage(fruitName)
                    }
                } else {
                    Log.e("HomeFragment", "Error: ${response.code}")
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Error: ${response.code}", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        })
    }

    private fun getFileFromUri(uri: Uri): File? {
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().cacheDir, firebaseAuth.uid+".jpg")
        inputStream.use { input ->
            file.outputStream().use { output ->
                input?.copyTo(output)
            }
        }
        return file
    }

    private fun goToImage(name: String) {
        val intent = Intent(context, ImageActivity::class.java)
        val imageUri = binding.fruitIv.tag as? Uri  // Retrieve the URI from the tag
        if (imageUri == null) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(context, "Debes de tomar o seleccionar una foto", Toast.LENGTH_SHORT).show()
        } else {
            intent.putExtra("imageUri", imageUri.toString())
            intent.putExtra("fruitName", name)
            binding.progressBar.visibility = View.GONE

            startActivity(intent)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        selectionPhoto.dismiss()

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(null)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }
}
