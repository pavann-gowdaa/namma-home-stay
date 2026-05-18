package com.nammahomestay.ui.host

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.nammahomestay.R
import com.nammahomestay.data.model.HomeStay
import com.nammahomestay.data.repository.HomeStayRepository
import com.nammahomestay.databinding.FragmentAddHomeStayBinding
import com.nammahomestay.utils.SessionManager
import com.nammahomestay.utils.ValidationUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddHomeStayFragment : Fragment() {

    private var _binding: FragmentAddHomeStayBinding? = null
    private val binding get() = _binding!!
    private val repository = HomeStayRepository()
    private lateinit var sessionManager: SessionManager
    private var selectedImageUri: Uri? = null
    private var editHomeStayId: String? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            binding.ivPreview.setImageURI(selectedImageUri)
            binding.ivPreview.visibility = View.VISIBLE
            binding.tvAddPhoto.text = "Change Photo"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddHomeStayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        editHomeStayId = arguments?.getString("homestayId")
        if (editHomeStayId != null) {
            binding.tvTitle.text = "Edit HomeStay"
            loadExistingHomeStay(editHomeStayId!!)
        }

        binding.ivPreview.setOnClickListener { pickImage() }
        binding.tvAddPhoto.setOnClickListener { pickImage() }

        binding.btnSubmit.setOnClickListener {
            if (editHomeStayId != null) updateHomeStay() else addHomeStay()
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun loadExistingHomeStay(id: String) {
        lifecycleScope.launch {
            try {
                val doc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("homestays").document(id).get().await()
                val data = doc.data
                if (data != null) {
                    val homestay = HomeStay.fromMap(data)
                    binding.etName.setText(homestay.name)
                    binding.etLocation.setText(homestay.location)
                    binding.etLatitude.setText(if (homestay.latitude != 0.0) homestay.latitude.toString() else "")
                    binding.etLongitude.setText(if (homestay.longitude != 0.0) homestay.longitude.toString() else "")
                    binding.etPrice.setText(homestay.rate.toString())
                    binding.etDescription.setText(homestay.description)
                    binding.cbAvailability.isChecked = homestay.availability
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Failed to load existing data", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun addHomeStay() {
        if (!validateInputs()) return

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmit.isEnabled = false

        lifecycleScope.launch {
            try {
                var photoUrl = ""
                if (selectedImageUri != null) {
                    val uploadResult = repository.uploadImage(selectedImageUri!!)
                    uploadResult.onSuccess { url -> photoUrl = url }
                }

                val homestay = HomeStay(
                    hostId = sessionManager.userId,
                    hostName = sessionManager.userName,
                    hostPhone = sessionManager.userPhone,
                    name = ValidationUtils.sanitizeInput(binding.etName.text.toString().trim()),
                    location = ValidationUtils.sanitizeInput(binding.etLocation.text.toString().trim()),
                    latitude = binding.etLatitude.text.toString().toDoubleOrNull() ?: 0.0,
                    longitude = binding.etLongitude.text.toString().toDoubleOrNull() ?: 0.0,
                    rate = binding.etPrice.text.toString().toDouble(),
                    description = ValidationUtils.sanitizeInput(binding.etDescription.text.toString().trim()),
                    photos = if (photoUrl.isNotBlank()) listOf(photoUrl) else emptyList(),
                    availability = binding.cbAvailability.isChecked
                )

                val result = repository.addHomeStay(homestay)
                result.onSuccess {
                    Snackbar.make(binding.root, "HomeStay listed successfully!", Snackbar.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }.onFailure {
                    Snackbar.make(binding.root, "Failed to add listing", Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.isEnabled = true
            }
        }
    }

    private fun updateHomeStay() {
        if (!validateInputs()) return

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmit.isEnabled = false

        lifecycleScope.launch {
            try {
                var photoUrl = ""
                if (selectedImageUri != null) {
                    val uploadResult = repository.uploadImage(selectedImageUri!!)
                    uploadResult.onSuccess { url -> photoUrl = url }
                }

                val existingDoc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("homestays").document(editHomeStayId!!).get().await()
                val existingData = existingDoc.data?.let { HomeStay.fromMap(it) }

                val homestay = HomeStay(
                    id = editHomeStayId!!,
                    hostId = existingData?.hostId ?: sessionManager.userId,
                    hostName = existingData?.hostName ?: sessionManager.userName,
                    hostPhone = existingData?.hostPhone ?: sessionManager.userPhone,
                    name = ValidationUtils.sanitizeInput(binding.etName.text.toString().trim()),
                    location = ValidationUtils.sanitizeInput(binding.etLocation.text.toString().trim()),
                    latitude = binding.etLatitude.text.toString().toDoubleOrNull() ?: 0.0,
                    longitude = binding.etLongitude.text.toString().toDoubleOrNull() ?: 0.0,
                    rate = binding.etPrice.text.toString().toDouble(),
                    description = ValidationUtils.sanitizeInput(binding.etDescription.text.toString().trim()),
                    photos = if (photoUrl.isNotBlank()) listOf(photoUrl) else existingData?.photos ?: emptyList(),
                    amenities = existingData?.amenities ?: emptyList(),
                    availability = binding.cbAvailability.isChecked,
                    isVerified = existingData?.isVerified ?: false,
                    createdAt = existingData?.createdAt ?: System.currentTimeMillis()
                )

                val result = repository.updateHomeStay(homestay)
                result.onSuccess {
                    Snackbar.make(binding.root, "HomeStay updated!", Snackbar.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }.onFailure {
                    Snackbar.make(binding.root, "Failed to update", Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.isEnabled = true
            }
        }
    }

    private fun validateInputs(): Boolean {
        var valid = true
        if (binding.etName.text.isNullOrBlank()) {
            binding.etName.error = "Required"
            valid = false
        }
        if (binding.etLocation.text.isNullOrBlank()) {
            binding.etLocation.error = "Required"
            valid = false
        }
        if (!ValidationUtils.isValidPrice(binding.etPrice.text.toString())) {
            binding.etPrice.error = "Enter valid price"
            valid = false
        }
        if (binding.etDescription.text.isNullOrBlank()) {
            binding.etDescription.error = "Required"
            valid = false
        }
        return valid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
