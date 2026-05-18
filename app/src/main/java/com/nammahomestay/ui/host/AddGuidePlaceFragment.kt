package com.nammahomestay.ui.host

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.nammahomestay.R
import com.nammahomestay.data.model.GuidePlace
import com.nammahomestay.data.repository.GuideRepository
import com.nammahomestay.data.repository.HomeStayRepository
import com.nammahomestay.databinding.FragmentAddGuidePlaceBinding
import com.nammahomestay.utils.SessionManager
import com.nammahomestay.utils.ValidationUtils
import kotlinx.coroutines.launch

class AddGuidePlaceFragment : Fragment() {

    private var _binding: FragmentAddGuidePlaceBinding? = null
    private val binding get() = _binding!!
    private val guideRepository = GuideRepository()
    private val homestayRepository = HomeStayRepository()
    private lateinit var sessionManager: SessionManager
    private var selectedHomeStayId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddGuidePlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        loadFirstHomeStay()

        binding.btnSave.setOnClickListener { savePlace() }
    }

    private fun loadFirstHomeStay() {
        lifecycleScope.launch {
            val result = homestayRepository.getHomeStaysByHost(sessionManager.userId)
            result.onSuccess { homestays ->
                if (homestays.isNotEmpty()) {
                    selectedHomeStayId = homestays.first().id
                } else {
                    Snackbar.make(binding.root, "Create a HomeStay first", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun savePlace() {
        if (selectedHomeStayId.isBlank()) {
            Snackbar.make(binding.root, "No HomeStay found", Snackbar.LENGTH_SHORT).show()
            return
        }

        val name = binding.etName.text.toString().trim()
        val distance = binding.etDistance.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val lat = binding.etLatitude.text.toString().toDoubleOrNull() ?: 0.0
        val lng = binding.etLongitude.text.toString().toDoubleOrNull() ?: 0.0
        val category = when (binding.toggleCategory.checkedChipId) {
            R.id.chipTemple -> "temple"
            R.id.chipWaterfall -> "waterfall"
            R.id.chipTrek -> "trek"
            R.id.chipRestaurant -> "restaurant"
            else -> "other"
        }

        if (!ValidationUtils.isNotEmpty(name, distance)) {
            Snackbar.make(binding.root, "Name and distance required", Snackbar.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        val place = GuidePlace(
            homestayId = selectedHomeStayId,
            name = name,
            distance = distance,
            description = description,
            latitude = lat,
            longitude = lng,
            category = category
        )

        lifecycleScope.launch {
            val result = guideRepository.addPlace(place)
            result.onSuccess {
                Snackbar.make(binding.root, "Place added!", Snackbar.LENGTH_SHORT).show()
                clearFields()
            }.onFailure {
                Snackbar.make(binding.root, "Failed to add place", Snackbar.LENGTH_LONG).show()
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun clearFields() {
        binding.etName.text?.clear()
        binding.etDistance.text?.clear()
        binding.etDescription.text?.clear()
        binding.etLatitude.text?.clear()
        binding.etLongitude.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
