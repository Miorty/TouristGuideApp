package com.example.touristguide.ui.favorites

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.touristguide.R

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {
    private val viewModel: FavoritesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.backButton).setOnClickListener {
            findNavController().navigateUp()
        }

        val adapter = FavoritePlaceAdapter {
            findNavController().navigate(R.id.placeDetailFragment)
        }
        view.findViewById<RecyclerView>(R.id.favoritesRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
        viewModel.places.observe(viewLifecycleOwner) { places ->
            adapter.submitList(places)
        }
        viewModel.favoriteRows.observe(viewLifecycleOwner) {
            viewModel.refresh()
        }
    }
}
