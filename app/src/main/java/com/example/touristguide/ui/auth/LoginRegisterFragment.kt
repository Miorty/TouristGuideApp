package com.example.touristguide.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.touristguide.R

class LoginRegisterFragment : Fragment(R.layout.fragment_login_register) {
    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailEditText = view.findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)

        view.findViewById<Button>(R.id.loginButton).setOnClickListener {
            viewModel.login(emailEditText.text.toString(), passwordEditText.text.toString())
        }
        view.findViewById<Button>(R.id.registerButton).setOnClickListener {
            Toast.makeText(requireContext(), "Версия 1.0", Toast.LENGTH_SHORT).show()
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
