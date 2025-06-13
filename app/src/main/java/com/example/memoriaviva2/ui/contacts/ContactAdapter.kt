package com.example.memoriaviva2.ui.contacts // Ajuste o pacote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriaviva2.R // Importe seu R
import com.example.memoriaviva2.ui.contacts.EmergencyContact

class EmergencyContactAdapter(
    private val onEditClick: (EmergencyContact) -> Unit,
    private val onRemoveClick: (EmergencyContact) -> Unit,
    private val onCallClick: (EmergencyContact) -> Unit // NOVO CALLBACK
) : ListAdapter<EmergencyContact, EmergencyContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_emergency_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position)
        holder.bind(contact, onEditClick, onRemoveClick, onCallClick) // PASSE O NOVO CALLBACK
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.text_view_contact_name)
        private val numberTextView: TextView = itemView.findViewById(R.id.text_view_contact_number)
        private val callButton: ImageButton = itemView.findViewById(R.id.button_call_contact) // NOVO BOTÃO
        private val editButton: ImageButton = itemView.findViewById(R.id.button_edit_contact)
        private val removeButton: ImageButton = itemView.findViewById(R.id.button_remove_contact)

        fun bind(
            contact: EmergencyContact,
            onEditClick: (EmergencyContact) -> Unit,
            onRemoveClick: (EmergencyContact) -> Unit,
            onCallClick: (EmergencyContact) -> Unit // NOVO CALLBACK
        ) {
            nameTextView.text = contact.name
            numberTextView.text = contact.fullNumber // Mostra +DDD Numero

            callButton.setOnClickListener { onCallClick(contact) } // CONFIGURA LISTENER
            editButton.setOnClickListener { onEditClick(contact) }
            removeButton.setOnClickListener { onRemoveClick(contact) }
        }
    }

    // ContactDiffCallback permanece o mesmo
    class ContactDiffCallback : DiffUtil.ItemCallback<EmergencyContact>() {
        override fun areItemsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean {
            return oldItem == newItem
        }
    }
}