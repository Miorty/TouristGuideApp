package com.example.touristguide.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.touristguide.R

class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.regionCard).setOnClickListener {
            findNavController().navigate(R.id.regionInfoFragment)
        }
        view.findViewById<View>(R.id.placesCard).setOnClickListener {
            findNavController().navigate(R.id.placesListFragment)
        }
        view.findViewById<View>(R.id.routesCard).setOnClickListener {
            findNavController().navigate(R.id.routesFragment)
        }
        view.findViewById<View>(R.id.mapCard).setOnClickListener {
            findNavController().navigate(R.id.placesMapFragment)
        }
        view.findViewById<View>(R.id.foodCard).setOnClickListener {
            findNavController().navigate(R.id.placesListFragment)
        }
    }
}
