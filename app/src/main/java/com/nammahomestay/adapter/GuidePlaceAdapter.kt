package com.nammahomestay.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nammahomestay.data.model.GuidePlace
import com.nammahomestay.databinding.ItemGuidePlaceBinding

class GuidePlaceAdapter(
    private val onClick: (GuidePlace) -> Unit
) : ListAdapter<GuidePlace, GuidePlaceAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGuidePlaceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemGuidePlaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(place: GuidePlace) {
            binding.tvName.text = place.name
            binding.tvDistance.text = "~${place.distance}"
            binding.tvDescription.text = place.description
            binding.tvCategory.text = place.category.replaceFirstChar { it.uppercase() }

            val icon = when (place.category) {
                "temple" -> com.nammahomestay.R.drawable.ic_temple
                "waterfall" -> com.nammahomestay.R.drawable.ic_waterfall
                "trek" -> com.nammahomestay.R.drawable.ic_trek
                "restaurant" -> com.nammahomestay.R.drawable.ic_restaurant
                else -> com.nammahomestay.R.drawable.ic_place
            }
            binding.ivIcon.setImageResource(icon)

            binding.root.setOnClickListener { onClick(place) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GuidePlace>() {
        override fun areItemsTheSame(oldItem: GuidePlace, newItem: GuidePlace): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GuidePlace, newItem: GuidePlace): Boolean {
            return oldItem == newItem
        }
    }
}
