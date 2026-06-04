package com.example.touristguide.ui.achievements

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.touristguide.R

class AchievementsFragment : Fragment(R.layout.fragment_achievements) {
    private val viewModel: AchievementsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.backButton).setOnClickListener {
            findNavController().navigateUp()
        }

        val adapter = AchievementAdapter()
        view.findViewById<RecyclerView>(R.id.achievementsRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
        viewModel.allAchievements.observe(viewLifecycleOwner) { achievements ->
            adapter.submitList(achievements)
        }
    }
}
