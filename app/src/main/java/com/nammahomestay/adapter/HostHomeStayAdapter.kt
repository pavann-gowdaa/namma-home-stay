package com.nammahomestay.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nammahomestay.data.model.HomeStay
import com.nammahomestay.databinding.ItemHostHomeStayBinding

class HostHomeStayAdapter(
    private val onEdit: (HomeStay) -> Unit,
    private val onDelete: (HomeStay) -> Unit
) : ListAdapter<HomeStay, HostHomeStayAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHostHomeStayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemHostHomeStayBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(homestay: HomeStay) {
            binding.tvName.text = homestay.name
            binding.tvLocation.text = homestay.location
            binding.tvPrice.text = "₹${String.format("%.0f", homestay.rate)}/night"
            binding.tvStatus.text = if (homestay.isVerified) "Verified" else "Pending"
            binding.tvStatus.setTextColor(
                if (homestay.isVerified)
                    binding.root.context.getColor(android.R.color.holo_green_dark)
                else
                    binding.root.context.getColor(android.R.color.holo_orange_dark)
            )

            binding.btnEdit.setOnClickListener { onEdit(homestay) }
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
