package com.nammahomestay.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.nammahomestay.adapter.AdminHomeStayAdapter
import com.nammahomestay.data.model.HomeStay
import com.nammahomestay.data.repository.HomeStayRepository
import com.nammahomestay.databinding.FragmentAdminPanelBinding
import kotlinx.coroutines.launch

class AdminPanelFragment : Fragment() {

    private var _binding: FragmentAdminPanelBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminHomeStayAdapter
    private val repository = HomeStayRepository()
    private var allHomeStays: List<HomeStay> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPanelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
        setupFilterChips()
        loadHomeStays()
    }

    private fun setupRecyclerView() {
        adapter = AdminHomeStayAdapter(
            onVerify = { homestay ->
                lifecycleScope.launch {
                    val result = repository.verifyHomeStay(homestay.id, true)
                    result.onSuccess { loadHomeStays() }.onFailure { e ->
                        Snackbar.make(binding.root, "Verify failed: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            },
            onReject = { homestay ->
                lifecycleScope.launch {
                    val result = repository.verifyHomeStay(homestay.id, false)
                    result.onSuccess { loadHomeStays() }.onFailure { e ->
                        Snackbar.make(binding.root, "Reject failed: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            },
            onDelete = { homestay ->
                lifecycleScope.launch {
                    val result = repository.deleteHomeStay(homestay.id)
                    result.onSuccess { loadHomeStays() }.onFailure { e ->
                        Snackbar.make(binding.root, "Delete failed: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        )
        binding.rvListings.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AdminPanelFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener { loadHomeStays() }
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener {
            adapter.submitList(allHomeStays)
            updateChipSelection(binding.chipAll)
            updateUI(allHomeStays)
        }
        binding.chipVerified.setOnClickListener {
            val filtered = allHomeStays.filter { it.isVerified }
            adapter.submitList(filtered)
            updateChipSelection(binding.chipVerified)
            updateUI(filtered)
        }
        binding.chipPending.setOnClickListener {
            val filtered = allHomeStays.filter { !it.isVerified }
            adapter.submitList(filtered)
            updateChipSelection(binding.chipPending)
            updateUI(filtered)
        }
    }

    private fun updateChipSelection(selected: Chip) {
        listOf(binding.chipAll, binding.chipVerified, binding.chipPending)
            .forEach { it.isChecked = it == selected }
    }

    private fun loadHomeStays() {
        binding.swipeRefresh.isRefreshing = true
        binding.shimmerView.visibility = View.VISIBLE
        binding.shimmerView.startShimmer()
        binding.rvListings.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE

        lifecycleScope.launch {
            val result = repository.getAllHomeStaysAdmin()
            result.onSuccess { homestays ->
                allHomeStays = homestays
                adapter.submitList(homestays)
                updateUI(homestays)
            }.onFailure { e ->
                Snackbar.make(binding.root, "Failed to load: ${e.message}", Snackbar.LENGTH_LONG).show()
                binding.tvEmpty.visibility = View.VISIBLE
            }
            binding.swipeRefresh.isRefreshing = false
            binding.shimmerView.visibility = View.GONE
            binding.shimmerView.stopShimmer()
        }
    }

    private fun updateUI(homestays: List<HomeStay>) {
        if (homestays.isEmpty()) {
            binding.rvListings.visibility = View.GONE
            binding.tvEmpty.visibility = View.VISIBLE
        } else {
            binding.rvListings.visibility = View.VISIBLE
            binding.tvEmpty.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
