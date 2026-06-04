package com.example.touristguide.ui.place_detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.touristguide.R
import com.example.touristguide.TouristGuideApp
import com.example.touristguide.core.enums.ReportType
import kotlinx.coroutines.launch

class PlaceDetailFragment : Fragment(R.layout.fragment_place_detail) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.backButton).setOnClickListener {
            findNavController().navigateUp()
        }
        view.findViewById<View>(R.id.showOnMapButton).setOnClickListener {
            findNavController().navigate(R.id.placesMapFragment)
        }
        view.findViewById<View>(R.id.addContentButton).setOnClickListener {
            findNavController().navigate(R.id.addReviewFragment)
        }
        view.findViewById<View>(R.id.reportButton).setOnClickListener {
            val container = (requireActivity().application as TouristGuideApp).appContainer
            viewLifecycleOwner.lifecycleScope.launch {
                val result = container.reportRepository.createReport(
                    userId = container.sessionManager.currentUserIdValue(),
                    reportType = ReportType.OTHER,
                    comment = "Жалоба на информацию о месте",
                    placeId = 1L
                )
                Toast.makeText(
                    requireContext(),
                    result.error ?: "Жалоба отправлена на модерацию",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
