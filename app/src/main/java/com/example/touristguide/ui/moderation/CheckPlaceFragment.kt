package com.example.touristguide.ui.moderation

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.touristguide.R

class CheckPlaceFragment : Fragment(R.layout.fragment_check_place) {
    private val viewModel: CheckPlaceViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emptyStateText = view.findViewById<TextView>(R.id.emptyStateText)
        view.findViewById<View>(R.id.backButton).setOnClickListener {
            findNavController().navigateUp()
        }

        val adapter = ModerationAdapter(
            onApprove = { item, comment -> viewModel.approve(item.id, comment) },
            onReject = { item, comment -> viewModel.reject(item.id, comment) }
        )

        view.findViewById<RecyclerView>(R.id.moderationRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        viewModel.pendingItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            emptyStateText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.decisionResult.observe(viewLifecycleOwner) { result ->
            Toast.makeText(
                requireContext(),
                result.error ?: "Решение сохранено",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
