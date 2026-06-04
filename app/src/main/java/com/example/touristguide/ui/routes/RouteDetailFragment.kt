package com.example.touristguide.ui.routes

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.touristguide.R

class RouteDetailFragment : Fragment(R.layout.fragment_route_detail) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.backButton).setOnClickListener {
            findNavController().navigateUp()
        }
        view.findViewById<View>(R.id.startRouteButton).setOnClickListener {
            findNavController().navigate(R.id.routeMapFragment)
        }
        view.findViewById<View>(R.id.showAllButton).setOnClickListener {
            findNavController().navigate(R.id.routeMapFragment)
        }
    }
}
