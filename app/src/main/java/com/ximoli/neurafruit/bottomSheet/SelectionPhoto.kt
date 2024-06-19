package com.ximoli.neurafruit.bottomSheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ximoli.neurafruit.databinding.SelectionPhotoBinding
import com.ximoli.neurafruit.fragment.HomeFragment
import com.ximoli.neurafruit.util.ThemeUtils

class SelectionPhoto : BottomSheetDialogFragment() {

    private var _binding: SelectionPhotoBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(): SelectionPhoto {
            return SelectionPhoto()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = SelectionPhotoBinding.inflate(inflater, container, false)

        ThemeUtils.applyTheme(requireContext())


        binding.cardView1.setOnClickListener {
            camera()
        }

        binding.cardView2.setOnClickListener {
            gallery()
        }

        return binding.root
    }

    private fun camera() {
        val parent = parentFragment
        if (parent is HomeFragment) {
            parent.takePhoto()
        }
    }

    private fun gallery() {
        val parent = parentFragment
        if (parent is HomeFragment) {
            parent.selectGallery()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
