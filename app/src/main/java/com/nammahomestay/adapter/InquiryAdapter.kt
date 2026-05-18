package com.nammahomestay.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nammahomestay.data.model.Inquiry
import com.nammahomestay.databinding.ItemInquiryBinding

class InquiryAdapter(
    private val onCall: (String) -> Unit,
    private val onWhatsApp: (String) -> Unit
) : ListAdapter<Inquiry, InquiryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInquiryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemInquiryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(inquiry: Inquiry) {
            binding.tvGuestName.text = inquiry.guestName.ifBlank { "Guest" }
            binding.tvGuestPhone.text = inquiry.guestPhone
            binding.tvMessage.text = inquiry.message
            binding.tvTimestamp.text = java.text.SimpleDateFormat(
                "dd MMM yyyy, hh:mm a", java.util.Locale.ENGLISH
            ).format(java.util.Date(inquiry.timestamp))

            binding.btnCall.setOnClickListener { onCall(inquiry.guestPhone) }
            binding.btnWhatsApp.setOnClickListener { onWhatsApp(inquiry.guestPhone) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Inquiry>() {
        override fun areItemsTheSame(oldItem: Inquiry, newItem: Inquiry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Inquiry, newItem: Inquiry): Boolean {
            return oldItem == newItem
        }
    }
}
