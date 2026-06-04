package com.example.touristguide.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.touristguide.R

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val viewModel: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileInfoText = view.findViewById<TextView>(R.id.profileInfoText)
        viewModel.profile.observe(viewLifecycleOwner) { user ->
            profileInfoText.text = if (user == null) {
                "Профиль не найден"
            } else {
                val status = if (user.level <= 1) "Новичок" else "Уровень ${user.level} • ${user.points} баллов"
                "${user.username}\n$status"
            }
        }

        view.findViewById<Button>(R.id.addPlaceButton).setOnClickListener {
            findNavController().navigate(R.id.addPlaceFragment)
        }
        view.findViewById<Button>(R.id.checkPlaceButton).setOnClickListener {
            findNavController().navigate(R.id.checkPlaceFragment)
        }
        view.findViewById<Button>(R.id.achievementsButton).setOnClickListener {
            findNavController().navigate(R.id.achievementsFragment)
        }
        view.findViewById<Button>(R.id.favoritesButton).setOnClickListener {
            findNavController().navigate(R.id.favoritesFragment)
        }
        view.findViewById<Button>(R.id.myRoutesButton).setOnClickListener {
            findNavController().navigate(R.id.myRoutesFragment)
        }
        view.findViewById<Button>(R.id.tasksButton).setOnClickListener {
            findNavController().navigate(R.id.weeklyTaskFragment)
        }
    }
}
