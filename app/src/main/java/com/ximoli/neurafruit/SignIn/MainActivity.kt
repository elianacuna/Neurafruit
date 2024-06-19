package com.ximoli.neurafruit.SignIn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ximoli.neurafruit.R
import com.ximoli.neurafruit.SignUp.RegisterActivity
import com.ximoli.neurafruit.databinding.ActivityMainBinding
import com.ximoli.neurafruit.user.HomeActivity
import com.ximoli.neurafruit.user.ProfileAddActivity
import com.ximoli.neurafruit.util.ThemeUtils

private lateinit var binding: ActivityMainBinding
private lateinit var firebaseAuth: FirebaseAuth
lateinit var mGoogleSignInClient: GoogleSignInClient
val RC_SIGN_IN: Int = 1
lateinit var gso:GoogleSignInOptions

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ThemeUtils.applyTheme(this)


        // Configurar Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        createRequest()

        binding.googleBtn.setOnClickListener {
            signIn();
        }

        binding.RegisterBtn.setOnClickListener {

            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        binding.loginBtn.setOnClickListener {

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }


    }

    private fun createRequest() {
        // Configure Google Sign In
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception=task.exception

            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            }
            catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT)
                    .show()
            }

        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val firebaseUser = firebaseAuth.currentUser
                    if (firebaseUser != null) {
                        checkUserProfile(firebaseUser.uid)
                    } else {
                        Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Login Failed: ", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserProfile(uid: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("Users").child(uid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Si el nodo existe, el perfil está completo
                    startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                    finish()
                } else {
                    // Si el nodo no existe, redirigir a EditProfileActivity
                   updateUserInfo(firebaseAuth)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error al verificar la cuenta: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUserInfo(firebaseAuth: FirebaseAuth) {

        val firebaseUser = firebaseAuth.currentUser
        val email = firebaseUser?.email
        val uid = firebaseUser?.uid
        val  name = firebaseUser?.displayName

        val timestamp = System.currentTimeMillis()


        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["username"] = name
        hashMap["profile"] = ""
        hashMap["auth"] = "Google"
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
                            startActivity(Intent(this, ProfileAddActivity::class.java))
                            finish()

                        }.addOnFailureListener {e->
                            Toast.makeText(this, "Fallio: ${e.message}", Toast.LENGTH_SHORT).show()
                        }


                }.addOnFailureListener {e->
                    Toast.makeText(this, "Fallio: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }



}