package com.ximoli.neurafruit.bottomSheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ximoli.neurafruit.databinding.SignOutSheetBinding
import com.ximoli.neurafruit.fragment.HomeFragment
import com.ximoli.neurafruit.fragment.SettingFragment
import com.ximoli.neurafruit.user.HomeActivity
import com.ximoli.neurafruit.util.ThemeUtils

class SignOut : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(): SignOut {
            return SignOut()
        }
    }

    private var _binding: SignOutSheetBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = SignOutSheetBinding.inflate(inflater, container, false)

        ThemeUtils.applyTheme(requireContext())


        binding.cancelBtn.setOnClickListener {
            val parent = parentFragment
            if (parent is SettingFragment) {
                parent.cancelSignOut()
            }
        }

        binding.signBtn.setOnClickListener {
            val parent = parentFragment
            if (parent is SettingFragment) {
                parent.signOut()
            }
        }


        return binding.root
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
