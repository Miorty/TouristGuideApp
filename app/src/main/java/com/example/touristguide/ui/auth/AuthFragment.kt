package com.example.touristguide.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.touristguide.R

class AuthFragment : Fragment(R.layout.fragment_auth) {
    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.guestLoginButton).setOnClickListener {
            viewModel.loginAsGuest()
        }
        view.findViewById<Button>(R.id.openLoginButton).setOnClickListener {
            findNavController().navigate(R.id.loginRegisterFragment)
        }

        viewModel.authResult.observe(viewLifecycleOwner) { result ->
            if (result.isSuccess) {
                findNavController().navigate(R.id.homeFragment)
            } else {
                Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
