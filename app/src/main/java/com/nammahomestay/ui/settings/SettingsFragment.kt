package com.nammahomestay.ui.settings

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nammahomestay.R
import com.nammahomestay.data.repository.AuthRepository
import com.nammahomestay.databinding.FragmentSettingsBinding
import com.nammahomestay.utils.Constants
import com.nammahomestay.utils.SessionManager
import com.nammahomestay.utils.ValidationUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val authRepository = AuthRepository()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        loadUserInfo()
        loadUserFromFirestore()
        setupVersion()
        setupDarkMode()
        setupSaveButton()
        setupLogoutButton()
    }

    private fun loadUserInfo() {
        binding.tvUserName.text = sessionManager.userName.ifBlank { "User" }
        binding.etName.setText(sessionManager.userName)
        binding.etPhone.setText(sessionManager.userPhone)
    }

    private fun loadUserFromFirestore() {
        val uid = auth.currentUser?.uid ?: return
        lifecycleScope.launch {
            val result = authRepository.getUserFromFirestore(uid)
            result.onSuccess { user ->
                binding.tvUserName.text = user.name.ifBlank { "User" }
                binding.tvUserEmail.text = user.phone
                binding.etName.setText(user.name)
                binding.etPhone.setText(user.phone)
            }
        }
    }

    private fun setupVersion() {
        try {
            val pkg = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            binding.tvVersion.text = "Version ${pkg.versionName}"
        } catch (e: PackageManager.NameNotFoundException) {
            binding.tvVersion.text = "Version 1.0.0"
        }
    }

    private fun setupDarkMode() {
        binding.switchDarkMode.isChecked = sessionManager.isDarkMode
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            sessionManager.isDarkMode = isChecked
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            if (!ValidationUtils.isNotEmpty(name, phone)) {
                Snackbar.make(binding.root, "Please fill in all fields", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidPhone(phone)) {
                binding.tilPhone.error = "Enter a valid phone number"
                return@setOnClickListener
            }

            binding.tilPhone.error = null
            binding.btnSave.isEnabled = false
            binding.btnSave.text = "Saving..."

            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            lifecycleScope.launch {
                try {
                    firestore.collection(Constants.USERS_COLLECTION)
                        .document(uid)
                        .update(
                            mapOf(
                                "name" to name,
                                "phone" to phone
                            )
                        )
                        .await()

                    sessionManager.userName = name
                    sessionManager.userPhone = phone
                    binding.tvUserName.text = name
                    binding.tvUserEmail.text = phone

                    Snackbar.make(binding.root, "Profile updated", Snackbar.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Snackbar.make(binding.root, "Failed to update: ${e.message}", Snackbar.LENGTH_LONG).show()
                } finally {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Save Changes"
                }
            }
        }
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            authRepository.logout()
            sessionManager.clearSession()
            findNavController().navigate(R.id.splashFragment, null,
                navOptions { popUpTo(0) { inclusive = true } })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
