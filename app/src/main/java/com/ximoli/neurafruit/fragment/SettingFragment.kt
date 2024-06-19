package com.ximoli.neurafruit.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.ximoli.neurafruit.SignIn.MainActivity
import com.ximoli.neurafruit.bottomSheet.SignOut
import com.ximoli.neurafruit.databinding.FragmentSettingBinding
import com.ximoli.neurafruit.user.EditProfileActivity
import com.ximoli.neurafruit.util.ThemeUtils // Ensure ThemeUtils is imported
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val signOut = SignOut.newInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        setupThemeSelection()
        setupProfileInfo()
    }

    private fun setupThemeSelection() {
        val sharedPreferences = requireContext().getSharedPreferences(ThemeUtils.PREFS_NAME, Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean(ThemeUtils.PREF_DARK_MODE, false)
        val isSystemMode = sharedPreferences.getBoolean(ThemeUtils.PREF_SYSTEM_MODE, false)

        // Set initial selection
        when {
            isSystemMode -> binding.systemRb.isChecked = true
            isDarkMode -> binding.darkRb.isChecked = true
            else -> binding.dayRb.isChecked = true
        }

        binding.dayRb.setOnClickListener {
            updateTheme(false, false)
        }

        binding.darkRb.setOnClickListener {
            updateTheme(true, false)
        }

        binding.systemRb.setOnClickListener {
            updateTheme(false, true)
        }
    }

    private fun updateTheme(isDarkMode: Boolean, isSystemMode: Boolean) {
        ThemeUtils.saveThemePreference(requireContext(), isDarkMode, isSystemMode)
        ThemeUtils.setAppTheme(isDarkMode, isSystemMode)
        restartApp()
    }

    private fun restartApp() {
        val intent = requireActivity().intent
        requireActivity().finish()
        startActivity(intent)
    }

    private fun setupProfileInfo() {
        binding.editBtn.setOnClickListener {
            startActivity(Intent(context, EditProfileActivity::class.java))
        }

        binding.signBtn.setOnClickListener {
            signOut.show(childFragmentManager, "sign_out")
        }

        infoUserProfile()
    }

    public fun cancelSignOut() {
        signOut.dismiss()
    }

    public fun signOut() {
        firebaseAuth.signOut()
        startActivity(Intent(context, MainActivity::class.java))
        activity?.finish()
        signOut.dismiss()
    }

    private fun infoUserProfile() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        database.child("Users").child(userId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val name = dataSnapshot.child("username").getValue(String::class.java)
                    val profileLink = dataSnapshot.child("profile").getValue(String::class.java)

                    binding.nameTxt.text = name
                    val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(
                        Date()
                    )
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
