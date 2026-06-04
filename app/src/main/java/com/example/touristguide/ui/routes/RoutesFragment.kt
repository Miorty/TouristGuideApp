package com.example.touristguide.ui.routes

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.touristguide.R

class RoutesFragment : Fragment(R.layout.fragment_routes) {
    private val viewModel: RoutesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.backButton).setOnClickListener {
            findNavController().navigateUp()
        }

        val adapter = RouteAdapter {
            findNavController().navigate(R.id.routeDetailFragment)
        }
        view.findViewById<RecyclerView>(R.id.routesRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
        viewModel.routes.observe(viewLifecycleOwner) { routes ->
            adapter.submitList(routes)
        }
    }
}
