package com.nammahomestay.ui.guest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.nammahomestay.R
import com.nammahomestay.adapter.HomeStayAdapter
import com.nammahomestay.data.model.FilterOptions
import com.nammahomestay.data.model.HomeStay
import com.nammahomestay.data.repository.HomeStayRepository
import com.nammahomestay.databinding.FragmentGuestHomeBinding
import com.nammahomestay.utils.SessionManager
import kotlinx.coroutines.launch

class GuestHomeFragment : Fragment() {

    private var _binding: FragmentGuestHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HomeStayAdapter
    private val repository = HomeStayRepository()
    private lateinit var sessionManager: SessionManager
    private var allHomeStays: List<HomeStay> = emptyList()
    private var searchJob: kotlinx.coroutines.Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuestHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        setupSwipeRefresh()
        setupFilterChips()
        setupSearch()
        loadHomeStays()

        binding.fabFilter.setOnClickListener {
            showFilterDialog()
        }

        binding.toolbar.inflateMenu(R.menu.guest_toolbar_menu)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    findNavController().navigate(R.id.action_guestHome_to_settings)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = HomeStayAdapter { homestay ->
            val bundle = Bundle().apply {
                putString("homestayId", homestay.id)
            }
            findNavController().navigate(R.id.action_guestHome_to_homeStayDetail, bundle)
        }
        binding.rvHomeStays.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@GuestHomeFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadHomeStays()
        }
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener {
            loadHomeStays()
            updateChipSelection(binding.chipAll)
        }
        binding.chipAvailable.setOnClickListener {
            val filtered = allHomeStays.filter { it.availability }
            adapter.submitList(filtered)
            updateChipSelection(binding.chipAvailable)
            updateUI(filtered)
        }
        binding.chipVerified.setOnClickListener {
            val filtered = allHomeStays.filter { it.isVerified }
            adapter.submitList(filtered)
            updateChipSelection(binding.chipVerified)
            updateUI(filtered)
        }
        binding.chipLowPrice.setOnClickListener {
            val filtered = allHomeStays.sortedBy { it.rate }
            adapter.submitList(filtered)
            updateChipSelection(binding.chipLowPrice)
            updateUI(filtered)
        }
    }

    private fun updateChipSelection(selected: Chip) {
        val chips = listOf(binding.chipAll, binding.chipAvailable, binding.chipVerified, binding.chipLowPrice)
        chips.forEach { it.isChecked = it == selected }
    }

    private fun setupSearch() {
        binding.searchView.editText?.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                searchJob?.cancel()
                val query = s?.toString()?.trim() ?: ""
                if (query.length < 2) {
                    loadHomeStays()
                    return
                }
                searchJob = lifecycleScope.launch {
                    kotlinx.coroutines.delay(300)
                    val result = repository.searchHomeStays(query)
                    result.onSuccess { homestays ->
                        allHomeStays = homestays
                        adapter.submitList(homestays)
                        updateUI(homestays)
                    }
                }
            }
        })
    }

    private fun loadHomeStays() {
        binding.swipeRefresh.isRefreshing = true
        binding.shimmerView.visibility = View.VISIBLE
        binding.shimmerView.startShimmer()
        binding.rvHomeStays.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE

        lifecycleScope.launch {
            val result = repository.getVerifiedHomeStays()
            result.onSuccess { homestays ->
                allHomeStays = homestays
                adapter.submitList(homestays)
                updateUI(homestays)
            }.onFailure { error ->
                Snackbar.make(binding.root, "Failed to load: ${error.message}", Snackbar.LENGTH_LONG).show()
                binding.tvEmpty.visibility = View.VISIBLE
            }
            binding.swipeRefresh.isRefreshing = false
            binding.shimmerView.visibility = View.GONE
            binding.shimmerView.stopShimmer()
        }
    }

    private fun updateUI(homestays: List<HomeStay>) {
        if (homestays.isEmpty()) {
            binding.rvHomeStays.visibility = View.GONE
            binding.tvEmpty.visibility = View.VISIBLE
        } else {
            binding.rvHomeStays.visibility = View.VISIBLE
            binding.tvEmpty.visibility = View.GONE
        }
    }

    private fun showFilterDialog() {
        val dialog = FilterDialogFragment()
        dialog.onFilterApplied = { filters ->
            applyFilters(filters)
        }
        dialog.show(parentFragmentManager, "FilterDialog")
    }

    private fun applyFilters(filters: FilterOptions) {
        lifecycleScope.launch {
            binding.shimmerView.visibility = View.VISIBLE
            binding.shimmerView.startShimmer()
            val result = repository.getHomeStaysWithFilters(filters)
            result.onSuccess { filtered ->
                adapter.submitList(filtered)
                updateUI(filtered)
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
