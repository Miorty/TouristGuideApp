package com.example.touristguide.ui.feed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.touristguide.R

class FeedFragment : Fragment(R.layout.fragment_feed) {
    private val viewModel: FeedViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = FeedAdapter()
        view.findViewById<RecyclerView>(R.id.feedRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
        viewModel.feed.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }
}
