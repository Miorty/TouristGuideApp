package com.example.touristguide.ui.review

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.touristguide.R

class AddReviewFragment : Fragment(R.layout.fragment_add_review) {
    private val viewModel: AddReviewViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reviewEditText = view.findViewById<EditText>(R.id.reviewEditText)
        view.findViewById<View>(R.id.backButton).setOnClickListener {
            findNavController().navigateUp()
        }
        view.findViewById<View>(R.id.addReviewButton).setOnClickListener {
            findNavController().navigate(R.id.addPhotoFragment)
        }
        view.findViewById<View>(R.id.submitReviewButton).setOnClickListener {
            viewModel.addReview(placeId = 1L, text = reviewEditText.text.toString())
        }
        viewModel.result.observe(viewLifecycleOwner) { result ->
            Toast.makeText(
                requireContext(),
                result.error ?: "Отзыв отправлен на модерацию",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
