package com.example.touristguide.ui.places

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.touristguide.R

class PlacesListFragment : Fragment(R.layout.fragment_places_list) {
    private val viewModel: PlacesListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PlaceCardAdapter {
            findNavController().navigate(R.id.placeDetailFragment)
        }
        view.findViewById<RecyclerView>(R.id.placesRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        view.findViewById<EditText>(R.id.searchEditText).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        viewModel.places.observe(viewLifecycleOwner) { places ->
            adapter.submitList(places)
        }
    }
}
