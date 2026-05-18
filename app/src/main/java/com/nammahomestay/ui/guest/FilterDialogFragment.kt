package com.nammahomestay.ui.guest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nammahomestay.data.model.FilterOptions
import com.nammahomestay.databinding.FragmentFilterDialogBinding

class FilterDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterDialogBinding? = null
    private val binding get() = _binding!!
    var onFilterApplied: ((FilterOptions) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnApply.setOnClickListener {
            val filters = FilterOptions(
                location = binding.etLocation.text.toString().trim(),
                minPrice = binding.etMinPrice.text.toString().toDoubleOrNull() ?: 0.0,
                maxPrice = binding.etMaxPrice.text.toString().toDoubleOrNull() ?: Double.MAX_VALUE,
                availabilityOnly = binding.cbAvailable.isChecked,
                verifiedOnly = binding.cbVerified.isChecked
            )
            onFilterApplied?.invoke(filters)
            dismiss()
        }

        binding.btnClear.setOnClickListener {
            onFilterApplied?.invoke(FilterOptions())
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
