package com.example.habittracker.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.habittracker.auth.LoginActivity
import com.example.habittracker.auth.SessionManager
import com.example.habittracker.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root

        // ✅ Logout button
        view.findViewById<Button>(com.example.habittracker.R.id.btnLogout)?.setOnClickListener {
            SessionManager.logout(requireContext())

            // jump back to LoginActivity and clear back stack
            val i = Intent(requireContext(), LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = SessionManager.currentUserName(requireContext())
        binding.textSettings.text = "Welcome, $name 👋"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
