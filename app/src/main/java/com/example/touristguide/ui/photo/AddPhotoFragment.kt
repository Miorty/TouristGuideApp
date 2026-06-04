package com.example.touristguide.ui.photo

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.touristguide.R
import com.example.touristguide.core.storage.ImageStorageManager

class AddPhotoFragment : Fragment(R.layout.fragment_add_photo) {
    private val viewModel: AddPhotoViewModel by viewModels()
    private var preview: ImageView? = null
    private var selectedPhotoText: TextView? = null

    private val pickPhoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        val storedPath = ImageStorageManager(requireContext()).saveImage(uri)
        preview?.setImageURI(uri)
        selectedPhotoText?.text = "Фото выбрано и отправлено на модерацию"
        viewModel.addPhoto(placeId = 1L, filePath = storedPath)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preview = view.findViewById(R.id.selectPhotoButton)
        selectedPhotoText = view.findViewById(R.id.selectedPhotoText)

        view.findViewById<View>(R.id.backButton).setOnClickListener {
            findNavController().navigateUp()
        }
        view.findViewById<View>(R.id.selectPhotoButton).setOnClickListener {
            pickPhoto.launch("image/*")
        }
        viewModel.result.observe(viewLifecycleOwner) { result ->
            Toast.makeText(
                requireContext(),
                result.error ?: "Фото сохранено в place_photos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        preview = null
        selectedPhotoText = null
        super.onDestroyView()
    }
}
