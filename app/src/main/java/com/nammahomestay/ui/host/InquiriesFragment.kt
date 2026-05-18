package com.nammahomestay.ui.host

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nammahomestay.R
import com.nammahomestay.adapter.InquiryAdapter
import com.nammahomestay.data.repository.InquiryRepository
import com.nammahomestay.databinding.FragmentInquiriesBinding
import com.nammahomestay.utils.SessionManager
import kotlinx.coroutines.launch

class InquiriesFragment : Fragment() {

    private var _binding: FragmentInquiriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: InquiryAdapter
    private val repository = InquiryRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInquiriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        loadInquiries()
    }

    private fun setupRecyclerView() {
        adapter = InquiryAdapter(
            onCall = { phone ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                startActivity(intent)
            },
            onWhatsApp = { phone ->
                try {
                    val url = "https://wa.me/${phone.replace("+", "")}"
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(url)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Snackbar.make(binding.root, "WhatsApp not installed", Snackbar.LENGTH_LONG).show()
                }
            }
        )
        binding.rvInquiries.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@InquiriesFragment.adapter
        }
    }

    private fun loadInquiries() {
        binding.shimmerView.visibility = View.VISIBLE
        binding.shimmerView.startShimmer()

        lifecycleScope.launch {
            val result = repository.getInquiriesForHost(sessionManager.userId)
            result.onSuccess { inquiries ->
                adapter.submitList(inquiries)
                if (inquiries.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvInquiries.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvInquiries.visibility = View.VISIBLE
                }
            }.onFailure {
                Snackbar.make(binding.root, "Failed to load inquiries", Snackbar.LENGTH_LONG).show()
            }
            binding.shimmerView.visibility = View.GONE
            binding.shimmerView.stopShimmer()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
