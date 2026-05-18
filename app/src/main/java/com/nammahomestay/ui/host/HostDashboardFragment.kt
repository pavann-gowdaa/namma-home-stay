package com.nammahomestay.ui.host

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nammahomestay.R
import com.nammahomestay.adapter.HostHomeStayAdapter
import com.nammahomestay.data.repository.HomeStayRepository
import com.nammahomestay.databinding.FragmentHostDashboardBinding
import com.nammahomestay.utils.SessionManager
import kotlinx.coroutines.launch

class HostDashboardFragment : Fragment() {

    private var _binding: FragmentHostDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HostHomeStayAdapter
    private val repository = HomeStayRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHostDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        setupSwipeRefresh()
        loadMyHomeStays()

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_hostDashboard_to_addHomeStay)
        }

        binding.btnViewInquiries.setOnClickListener {
            findNavController().navigate(R.id.action_hostDashboard_to_inquiries)
        }

        binding.btnManageMenu.setOnClickListener {
            findNavController().navigate(R.id.action_hostDashboard_to_dailyMenu)
        }

        binding.btnAddGuidePlace.setOnClickListener {
            findNavController().navigate(R.id.action_hostDashboard_to_addGuidePlace)
        }

        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_hostDashboard_to_settings)
        }
    }

    private fun setupRecyclerView() {
        adapter = HostHomeStayAdapter(
            onEdit = { homestay ->
                val bundle = Bundle().apply { putString("homestayId", homestay.id) }
                findNavController().navigate(R.id.action_hostDashboard_to_addHomeStay, bundle)
            },
            onDelete = { homestay ->
                deleteHomeStay(homestay.id)
            }
        )
        binding.rvMyHomeStays.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HostDashboardFragment.adapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener { loadMyHomeStays() }
    }

    private fun loadMyHomeStays() {
        binding.shimmerView.visibility = View.VISIBLE
        binding.shimmerView.startShimmer()
        binding.swipeRefresh.isRefreshing = true

        lifecycleScope.launch {
            val result = repository.getHomeStaysByHost(sessionManager.userId)
            result.onSuccess { homestays ->
                adapter.submitList(homestays)
                if (homestays.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvMyHomeStays.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvMyHomeStays.visibility = View.VISIBLE
                }
            }.onFailure {
                Snackbar.make(binding.root, "Failed to load listings", Snackbar.LENGTH_LONG).show()
            }
            binding.shimmerView.visibility = View.GONE
            binding.shimmerView.stopShimmer()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun deleteHomeStay(id: String) {
        lifecycleScope.launch {
            val result = repository.deleteHomeStay(id)
            result.onSuccess {
                Snackbar.make(binding.root, "Listing deleted", Snackbar.LENGTH_SHORT).show()
                loadMyHomeStays()
            }.onFailure {
                Snackbar.make(binding.root, "Failed to delete", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadMyHomeStays()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
