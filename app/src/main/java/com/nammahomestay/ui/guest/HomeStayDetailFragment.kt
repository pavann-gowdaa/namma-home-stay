package com.nammahomestay.ui.guest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.nammahomestay.R
import com.nammahomestay.adapter.GuidePlaceAdapter
import com.nammahomestay.data.model.HomeStay
import com.nammahomestay.data.repository.GuideRepository
import com.nammahomestay.data.repository.HomeStayRepository
import com.nammahomestay.data.repository.FavoriteRepository
import com.nammahomestay.data.repository.MenuRepository
import com.nammahomestay.databinding.FragmentHomeStayDetailBinding
import com.nammahomestay.utils.Constants
import com.nammahomestay.utils.SessionManager
import coil.load
import coil.transform.RoundedCornersTransformation
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeStayDetailFragment : Fragment() {

    private var _binding: FragmentHomeStayDetailBinding? = null
    private val binding get() = _binding!!
    private val homestayRepository = HomeStayRepository()
    private val menuRepository = MenuRepository()
    private val guideRepository = GuideRepository()
    private val favoriteRepository = FavoriteRepository()
    private var isFavorited = false
    private lateinit var sessionManager: SessionManager
    private var homestay: HomeStay? = null
    private lateinit var placeAdapter: GuidePlaceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeStayDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        val homestayId = arguments?.getString("homestayId") ?: return
        setupGuideRecyclerView()
        loadHomeStayDetail(homestayId)
        loadTodayMenu(homestayId)
        loadGuidePlaces(homestayId)

        binding.btnSendInquiry.setOnClickListener {
            showInquiryDialog()
        }

        binding.btnOpenMaps.setOnClickListener {
            openInGoogleMaps()
        }

        binding.btnFavorite.setOnClickListener {
            toggleFavoriteAction()
        }

        binding.btnShare.setOnClickListener {
            shareHomeStay()
        }
    }

    private fun setupGuideRecyclerView() {
        placeAdapter = GuidePlaceAdapter { place ->
            openPlaceInMaps(place.latitude, place.longitude, place.name)
        }
        binding.rvNearbyPlaces.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = placeAdapter
        }
    }

    private fun loadHomeStayDetail(homestayId: String) {
        lifecycleScope.launch {
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection(Constants.HOMESTAYS_COLLECTION)
                    .document(homestayId)
                    .get()
                    .await()
                val data = doc.data
                if (data != null) {
                    homestay = HomeStay.fromMap(data)
                    bindHomeStayData(homestay!!)
                    checkFavoriteStatus(homestayId)
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Failed to load details", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindHomeStayData(homestay: HomeStay) {
        binding.tvName.text = homestay.name
        binding.tvLocation.text = homestay.location
        binding.tvPrice.text = "₹${String.format("%.0f", homestay.rate)}/night"
        binding.tvDescription.text = homestay.description
        binding.tvHostName.text = "Host: ${homestay.hostName}"
        binding.tvHostPhone.text = homestay.hostPhone

        if (homestay.availability) {
            binding.chipAvailability.text = "Available"
            binding.chipAvailability.setChipBackgroundColorResource(R.color.available_green)
        } else {
            binding.chipAvailability.text = "Booked"
            binding.chipAvailability.setChipBackgroundColorResource(R.color.booked_red)
        }

        if (homestay.isVerified) {
            binding.chipVerified.visibility = View.VISIBLE
        } else {
            binding.chipVerified.visibility = View.GONE
        }

        if (homestay.photos.isNotEmpty()) {
            binding.ivHomeStay.load(homestay.photos.first()) {
                crossfade(true)
                transformations(RoundedCornersTransformation(16f))
                placeholder(R.drawable.placeholder_image)
                error(R.drawable.placeholder_image)
            }
        }
    }

    private fun checkFavoriteStatus(homestayId: String) {
        lifecycleScope.launch {
            try {
                val favorited = favoriteRepository.isFavorite(sessionManager.userId, homestayId)
                isFavorited = favorited
                val icon = if (favorited) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
                binding.btnFavorite.setImageDrawable(ContextCompat.getDrawable(requireContext(), icon))
            } catch (e: Exception) {
                binding.btnFavorite.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_favorite_border))
            }
        }
    }

    private fun toggleFavoriteAction() {
        val homestayId = homestay?.id ?: return
        lifecycleScope.launch {
            try {
                val result = favoriteRepository.toggleFavorite(sessionManager.userId, homestayId)
                result.onSuccess { added ->
                    isFavorited = added
                    val icon = if (added) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
                    binding.btnFavorite.setImageDrawable(ContextCompat.getDrawable(requireContext(), icon))
                }.onFailure {
                    Snackbar.make(binding.root, "Failed to update favorite", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareHomeStay() {
        homestay?.let { h ->
            val shareText = """
Check out this HomeStay: ${h.name}
📍 Location: ${h.location}
💰 Price: ₹${String.format("%.0f", h.rate)}/night
${h.description}
            """.trimIndent()
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, "Share HomeStay"))
        }
    }

    private fun loadTodayMenu(homestayId: String) {
        lifecycleScope.launch {
            val result = menuRepository.getTodayMenu(homestayId)
            result.onSuccess { menu ->
                if (menu != null) {
                    binding.cardMenu.visibility = View.VISIBLE
                    binding.tvBreakfast.text = menu.breakfast.joinToString(", ")
                    binding.tvLunch.text = menu.lunch.joinToString(", ")
                    binding.tvDinner.text = menu.dinner.joinToString(", ")
                    if (menu.description.isNotBlank()) {
                        binding.tvMenuDescription.text = menu.description
                        binding.tvMenuDescription.visibility = View.VISIBLE
                    }
                } else {
                    binding.cardMenu.visibility = View.GONE
                }
            }.onFailure {
                binding.cardMenu.visibility = View.GONE
            }
        }
    }

    private fun loadGuidePlaces(homestayId: String) {
        lifecycleScope.launch {
            val result = guideRepository.getPlacesForHomeStay(homestayId)
            result.onSuccess { places ->
                if (places.isNotEmpty()) {
                    binding.tvSectionGuide.visibility = View.VISIBLE
                    binding.rvNearbyPlaces.visibility = View.VISIBLE
                    placeAdapter.submitList(places)
                }
            }
        }
    }

    private fun showInquiryDialog() {
        val dialog = InquiryDialogFragment()
        dialog.onSendInquiry = { message ->
            sendInquiry(message)
        }
        dialog.show(parentFragmentManager, "InquiryDialog")
    }

    private fun sendInquiry(message: String) {
        lifecycleScope.launch {
            try {
                val inquiry = com.nammahomestay.data.model.Inquiry(
                    homestayId = homestay?.id ?: "",
                    hostId = homestay?.hostId ?: "",
                    guestId = sessionManager.userId,
                    guestName = sessionManager.userName,
                    guestPhone = sessionManager.userPhone,
                    message = message
                )
                val repo = com.nammahomestay.data.repository.InquiryRepository()
                val result = repo.sendInquiry(inquiry)
                result.onSuccess {
                    Snackbar.make(binding.root, "Inquiry sent successfully!", Snackbar.LENGTH_LONG).show()
                }.onFailure {
                    Snackbar.make(binding.root, "Failed to send inquiry", Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun openInGoogleMaps() {
        homestay?.let { h ->
            if (h.latitude != 0.0 && h.longitude != 0.0) {
                val uri = Uri.parse("geo:${h.latitude},${h.longitude}?q=${h.latitude},${h.longitude}(${h.name})")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
            } else {
                val uri = Uri.parse("geo:0,0?q=${h.location}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }
    }

    private fun openPlaceInMaps(lat: Double, lng: Double, name: String) {
        if (lat != 0.0 && lng != 0.0) {
            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($name)")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        } else {
            Snackbar.make(binding.root, "Location not available", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
