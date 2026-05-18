package com.nammahomestay.ui.guest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nammahomestay.R
import com.nammahomestay.databinding.FragmentInquiryDialogBinding

class InquiryDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentInquiryDialogBinding? = null
    private val binding get() = _binding!!
    var onSendInquiry: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInquiryDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotBlank()) {
                onSendInquiry?.invoke(message)
                dismiss()
            } else {
                binding.etMessage.error = "Please enter a message"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
