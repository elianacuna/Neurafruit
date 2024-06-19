package com.ximoli.neurafruit.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.ximoli.neurafruit.adapters.AdapterFavorites
import com.ximoli.neurafruit.databinding.FragmentFavoriteBinding
import com.ximoli.neurafruit.models.Favorites
import com.ximoli.neurafruit.util.ThemeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val binding get() = _binding!!

    private lateinit var favoriteArrayList: ArrayList<Favorites>

    private lateinit var adapterFavorites: AdapterFavorites

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ThemeUtils.applyTheme(requireContext())

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        infoUserProfile()
        loadFavorite()

        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterFavorites.filter.filter(s)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })


    }

    private fun loadFavorite() {
        favoriteArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("UsersFavorite")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favoriteArrayList.clear() // Clear the list to avoid duplication
                for (ds in snapshot.children) {
                    val model = ds.getValue(Favorites::class.java)
                    if (model != null) {
                        favoriteArrayList.add(model)
                    }
                }

                adapterFavorites = AdapterFavorites(requireContext(), favoriteArrayList)
                binding.fruitRv.adapter = adapterFavorites
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load favorites: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
                            .error(com.ximoli.neurafruit.R.drawable.logo)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
