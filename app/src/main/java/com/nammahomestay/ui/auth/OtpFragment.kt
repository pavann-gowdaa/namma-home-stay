package com.nammahomestay.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.nammahomestay.R
import com.nammahomestay.data.repository.AuthRepository
import com.nammahomestay.databinding.FragmentOtpBinding

class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
    private val authRepository = AuthRepository()
    private var verificationId: String = ""
    private var phone: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        verificationId = arguments?.getString("verificationId") ?: ""
        phone = arguments?.getString("phone") ?: ""

        binding.tvPhone.text = "OTP sent to $phone"

        binding.btnVerify.setOnClickListener {
            val otp = binding.etOtp.text.toString().trim()
            if (otp.length == 6) {
                verifyOtp(otp)
            } else {
                binding.etOtp.error = "Enter 6-digit OTP"
            }
        }

        binding.btnResend.setOnClickListener {
            resendOtp()
        }
    }

    private fun verifyOtp(otp: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnVerify.isEnabled = false

        authRepository.verifyOtp(
            verificationId = verificationId,
            otp = otp,
            onSuccess = {
                binding.progressBar.visibility = View.GONE
                val bundle = Bundle().apply {
                    putString("phone", phone)
                }
                findNavController().navigate(R.id.action_otp_to_roleSelection, bundle)
            },
            onError = { error ->
                binding.progressBar.visibility = View.GONE
                binding.btnVerify.isEnabled = true
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun resendOtp() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnResend.isEnabled = false

        authRepository.sendOtp(
            activity = requireActivity(),
            phoneNumber = phone,
            onCodeSent = { newVerificationId ->
                verificationId = newVerificationId
                binding.progressBar.visibility = View.GONE
                binding.btnResend.isEnabled = true
                Snackbar.make(binding.root, "OTP resent", Snackbar.LENGTH_SHORT).show()
            },
            onError = { error ->
                binding.progressBar.visibility = View.GONE
                binding.btnResend.isEnabled = true
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
