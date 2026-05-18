package com.nammahomestay.ui.host

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.nammahomestay.R
import com.nammahomestay.data.model.DailyMenu
import com.nammahomestay.data.repository.GeminiRepository
import com.nammahomestay.data.repository.HomeStayRepository
import com.nammahomestay.data.repository.MenuRepository
import com.nammahomestay.databinding.FragmentDailyMenuBinding
import com.nammahomestay.utils.DateUtils
import com.nammahomestay.utils.SessionManager
import kotlinx.coroutines.launch

class DailyMenuFragment : Fragment() {

    private var _binding: FragmentDailyMenuBinding? = null
    private val binding get() = _binding!!
    private val menuRepository = MenuRepository()
    private val geminiRepository = GeminiRepository()
    private val homestayRepository = HomeStayRepository()
    private lateinit var sessionManager: SessionManager
    private var selectedHomeStayId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        binding.tvDate.text = "Menu for ${DateUtils.formatForDisplay(DateUtils.getTodayDate())}"

        binding.btnGenerateBreakfast.setOnClickListener { generateMenuItems("breakfast") }
        binding.btnGenerateLunch.setOnClickListener { generateMenuItems("lunch") }
        binding.btnGenerateDinner.setOnClickListener { generateMenuItems("dinner") }
        binding.btnGenerateDescription.setOnClickListener { generateDescription() }
        binding.btnGenerateAll.setOnClickListener { generateFullMenu() }

        binding.btnSave.setOnClickListener { saveMenu() }

        loadHostHomeStays()
    }

    private fun loadHostHomeStays() {
        lifecycleScope.launch {
            val result = homestayRepository.getHomeStaysByHost(sessionManager.userId)
            result.onSuccess { homestays ->
                if (homestays.isNotEmpty()) {
                    selectedHomeStayId = homestays.first().id
                    binding.tvSelectedHomeStay.text = "HomeStay: ${homestays.first().name}"
                    loadExistingMenu()
                } else {
                    Snackbar.make(binding.root, "Create a HomeStay listing first", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loadExistingMenu() {
        lifecycleScope.launch {
            val result = menuRepository.getTodayMenu(selectedHomeStayId)
            result.onSuccess { menu ->
                if (menu != null) {
                    binding.etBreakfast.setText(menu.breakfast.joinToString(", "))
                    binding.etLunch.setText(menu.lunch.joinToString(", "))
                    binding.etDinner.setText(menu.dinner.joinToString(", "))
                    binding.etDescription.setText(menu.description)
                }
            }
        }
    }

    private fun generateMenuItems(mealType: String) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = geminiRepository.generateFoodSuggestions("Karnataka", mealType)
            result.onSuccess { items ->
                val text = items.joinToString(", ")
                when (mealType) {
                    "breakfast" -> binding.etBreakfast.setText(text)
                    "lunch" -> binding.etLunch.setText(text)
                    "dinner" -> binding.etDinner.setText(text)
                }
            }.onFailure {
                Snackbar.make(binding.root, "AI generation failed. Please add manually.", Snackbar.LENGTH_LONG).show()
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun generateDescription() {
        val items = listOf(
            binding.etBreakfast.text.toString(),
            binding.etLunch.text.toString(),
            binding.etDinner.text.toString()
        ).filter { it.isNotBlank() }
            .flatMap { it.split(",").map { s -> s.trim() } }

        if (items.isEmpty()) {
            Snackbar.make(binding.root, "Add menu items first", Snackbar.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = geminiRepository.generateMenuDescription(items)
            result.onSuccess { description ->
                binding.etDescription.setText(description)
            }.onFailure {
                Snackbar.make(binding.root, "Failed to generate description", Snackbar.LENGTH_LONG).show()
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun generateFullMenu() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = geminiRepository.generateMenuIdeas("Karnataka")
            result.onSuccess { fullMenu ->
                parseAndSetMenu(fullMenu)
            }.onFailure {
                Snackbar.make(binding.root, "AI generation failed", Snackbar.LENGTH_LONG).show()
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun parseAndSetMenu(fullMenu: String) {
        val lines = fullMenu.lines()
        var currentSection = ""
        val breakfast = mutableListOf<String>()
        val lunch = mutableListOf<String>()
        val dinner = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.contains("BREAKFAST", ignoreCase = true) -> currentSection = "breakfast"
                trimmed.contains("LUNCH", ignoreCase = true) -> currentSection = "lunch"
                trimmed.contains("DINNER", ignoreCase = true) -> currentSection = "dinner"
                trimmed.startsWith("-") || trimmed.startsWith("*") -> {
                    val item = trimmed.removePrefix("-").removePrefix("*").trim()
                    if (item.isNotBlank()) {
                        when (currentSection) {
                            "breakfast" -> breakfast.add(item)
                            "lunch" -> lunch.add(item)
                            "dinner" -> dinner.add(item)
                        }
                    }
                }
            }
        }

        if (breakfast.isNotEmpty()) binding.etBreakfast.setText(breakfast.joinToString(", "))
        if (lunch.isNotEmpty()) binding.etLunch.setText(lunch.joinToString(", "))
        if (dinner.isNotEmpty()) binding.etDinner.setText(dinner.joinToString(", "))

        Snackbar.make(binding.root, "AI menu generated! Review and save.", Snackbar.LENGTH_LONG).show()
    }

    private fun saveMenu() {
        if (selectedHomeStayId.isBlank()) {
            Snackbar.make(binding.root, "No HomeStay selected", Snackbar.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        val menu = DailyMenu(
            homestayId = selectedHomeStayId,
            date = DateUtils.getTodayDate(),
            breakfast = binding.etBreakfast.text.toString().split(",").map { it.trim() }.filter { it.isNotBlank() },
            lunch = binding.etLunch.text.toString().split(",").map { it.trim() }.filter { it.isNotBlank() },
            dinner = binding.etDinner.text.toString().split(",").map { it.trim() }.filter { it.isNotBlank() },
            description = binding.etDescription.text.toString().trim()
        )

        lifecycleScope.launch {
            val result = menuRepository.addDailyMenu(menu)
            result.onSuccess {
                Snackbar.make(binding.root, "Menu saved for today!", Snackbar.LENGTH_SHORT).show()
            }.onFailure {
                Snackbar.make(binding.root, "Failed to save menu", Snackbar.LENGTH_LONG).show()
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
