package com.nammahomestay.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nammahomestay.R
import com.nammahomestay.data.model.HomeStay
import com.nammahomestay.databinding.ItemAdminHomeStayBinding

class AdminHomeStayAdapter(
    private val onVerify: (HomeStay) -> Unit,
    private val onReject: (HomeStay) -> Unit,
    private val onDelete: (HomeStay) -> Unit
) : ListAdapter<HomeStay, AdminHomeStayAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminHomeStayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemAdminHomeStayBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(homestay: HomeStay) {
            binding.tvName.text = homestay.name
            binding.tvHostName.text = "Host: ${homestay.hostName}"
            binding.tvLocation.text = homestay.location
            binding.tvPrice.text = "₹${String.format("%.0f", homestay.rate)}/night"

            if (homestay.isVerified) {
                binding.tvStatus.text = "Verified"
                binding.tvStatus.setTextColor(binding.root.context.getColor(R.color.available_green))
                binding.btnVerify.isEnabled = false
                binding.btnReject.isEnabled = true
            } else {
                binding.tvStatus.text = "Pending"
                binding.tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_orange_dark))
                binding.btnVerify.isEnabled = true
                binding.btnReject.isEnabled = false
            }

            binding.btnVerify.setOnClickListener { onVerify(homestay) }
            binding.btnReject.setOnClickListener { onReject(homestay) }
            binding.btnDelete.setOnClickListener { onDelete(homestay) }
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
