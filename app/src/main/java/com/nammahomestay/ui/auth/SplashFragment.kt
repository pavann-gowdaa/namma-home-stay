package com.nammahomestay.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nammahomestay.R
import com.nammahomestay.utils.Constants
import com.nammahomestay.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        navigateToNext()
    }

    private fun navigateToNext() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null && sessionManager.isLoggedIn) {
            val role = sessionManager.getUserRole()
            when (role) {
                "guest" -> findNavController().navigate(R.id.action_splash_to_guestHome)
                "host" -> findNavController().navigate(R.id.action_splash_to_hostDashboard)
                "admin" -> findNavController().navigate(R.id.action_splash_to_adminPanel)
                else -> findNavController().navigate(R.id.action_splash_to_login)
            }
        } else {
            findNavController().navigate(R.id.action_splash_to_login)
        }
    }
}
