package com.nammahomestay.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.nammahomestay.R
import com.nammahomestay.data.model.HomeStay
import com.nammahomestay.databinding.ItemHomeStayBinding

class HomeStayAdapter(
    private val onClick: (HomeStay) -> Unit
) : ListAdapter<HomeStay, HomeStayAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeStayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemHomeStayBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(homestay: HomeStay) {
            binding.tvName.text = homestay.name
            binding.tvLocation.text = homestay.location
            binding.tvPrice.text = "₹${String.format("%.0f", homestay.rate)} / night"

            if (homestay.availability) {
                binding.tvAvailability.text = "Available"
                binding.tvAvailability.setTextColor(
                    binding.root.context.getColor(R.color.available_green)
                )
            } else {
                binding.tvAvailability.text = "Booked"
                binding.tvAvailability.setTextColor(
                    binding.root.context.getColor(R.color.booked_red)
                )
            }

            if (homestay.isVerified) {
                binding.ivVerified.visibility = android.view.View.VISIBLE
            } else {
                binding.ivVerified.visibility = android.view.View.GONE
            }

            if (homestay.photos.isNotEmpty()) {
                binding.ivImage.load(homestay.photos.first()) {
                    crossfade(true)
                    transformations(RoundedCornersTransformation(12f))
                    placeholder(R.drawable.placeholder_image)
                    error(R.drawable.placeholder_image)
                }
            } else {
                binding.ivImage.setImageResource(R.drawable.placeholder_image)
            }

            binding.root.setOnClickListener { onClick(homestay) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HomeStay>() {
        override fun areItemsTheSame(oldItem: HomeStay, newItem: HomeStay): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HomeStay, newItem: HomeStay): Boolean {
            return oldItem == newItem
        }
    }
}
