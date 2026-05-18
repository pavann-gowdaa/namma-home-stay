package com.nammahomestay.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.nammahomestay.R
import com.nammahomestay.data.repository.AuthRepository
import com.nammahomestay.databinding.FragmentLoginBinding
import com.nammahomestay.utils.Constants
import com.nammahomestay.utils.SessionManager
import com.nammahomestay.utils.ValidationUtils
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authRepository = AuthRepository()
    private lateinit var sessionManager: SessionManager
    private var googleSignInLauncher: androidx.activity.result.ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleSignInLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    handleGoogleSignIn(idToken)
                } else {
                    Snackbar.make(binding.root, "Google Sign-In failed", Snackbar.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                Snackbar.make(binding.root, "Google Sign-In cancelled", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        binding.btnSendOtp.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            if (ValidationUtils.isValidPhone(phone)) {
                sendOtp(phone)
            } else {
                binding.etPhone.error = "Enter a valid phone number"
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            startGoogleSignIn()
        }
    }

    private fun sendOtp(phone: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSendOtp.isEnabled = false

        val formattedPhone = if (phone.startsWith("+")) phone else "+91$phone"

        authRepository.sendOtp(
            activity = requireActivity(),
            phoneNumber = formattedPhone,
            onCodeSent = { verificationId ->
                binding.progressBar.visibility = View.GONE
                val bundle = Bundle().apply {
                    putString("verificationId", verificationId)
                    putString("phone", formattedPhone)
                }
                findNavController().navigate(R.id.action_login_to_otp, bundle)
            },
            onError = { error ->
                binding.progressBar.visibility = View.GONE
                binding.btnSendOtp.isEnabled = true
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun startGoogleSignIn() {
        val signInIntent = authRepository.getGoogleSignInClient(requireActivity()).signInIntent
        googleSignInLauncher?.launch(signInIntent)
    }

    private fun handleGoogleSignIn(idToken: String) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val result = authRepository.signInWithGoogle(idToken)
            if (result.isSuccess) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    saveGoogleUserAndProceed(user.uid, user.displayName ?: "User", user.email ?: "")
                }
            } else {
                binding.progressBar.visibility = View.GONE
                Snackbar.make(binding.root, result.exceptionOrNull()?.message ?: "Sign-In failed", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun saveGoogleUserAndProceed(uid: String, name: String, email: String) {
        lifecycleScope.launch {
            try {
                val existing = authRepository.getUserFromFirestore(uid)
                if (existing.isSuccess) {
                    val user = existing.getOrNull()!!
                    sessionManager.saveUser(uid, user.phone, user.name, user.role)
                    navigateToDashboard(user.role)
                } else {
                    val bundle = Bundle().apply {
                        putString("uid", uid)
                        putString("name", name)
                        putString("email", email)
                    }
                    findNavController().navigate(R.id.action_login_to_roleSelection, bundle)
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Snackbar.make(binding.root, e.message ?: "Error", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToDashboard(role: String) {
        when (role) {
            "guest" -> findNavController().navigate(R.id.action_login_to_guestHome)
            "host" -> findNavController().navigate(R.id.action_login_to_hostDashboard)
            "admin" -> findNavController().navigate(R.id.action_login_to_adminPanel)
            else -> findNavController().navigate(R.id.action_login_to_roleSelection)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}