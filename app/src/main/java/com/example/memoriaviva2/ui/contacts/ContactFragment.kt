package com.example.memoriaviva2.ui.contacts // Ajuste o pacote

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.memoriaviva2.R // Verifique se este é o pacote correto do seu app
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriaviva2.ui.contacts.EmergencyContact
// Importe seu Adapter (você precisará criar um)
// import com.example.memoriaviva2.ui.contacts.EmergencyContactAdapter
// Removed FloatingActionButton import
import kotlin.text.isNotEmpty
import kotlin.text.trim

class ContactFragment : Fragment() {

    private val contactViewModel: ContactViewModel by viewModels()
    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var contactsAdapter: EmergencyContactAdapter // Você precisará criar este adapter
    private lateinit var btnAddContact: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_contact, container, false) // Crie este layout

        contactsRecyclerView = view.findViewById(R.id.recycler_view_contacts)
        btnAddContact = view.findViewById(R.id.fab_add_contact)

        setupRecyclerView()

        btnAddContact.setOnClickListener {
            showAddEditContactDialog(null) // null para adicionar novo contato
        }

        contactViewModel.emergencyContacts.observe(viewLifecycleOwner) { contacts ->
            contactsAdapter.submitList(contacts)
        }

        return view
    }
    private fun openDialerWithNumber(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erro ao abrir discador: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        contactsAdapter = EmergencyContactAdapter(
            onEditClick = { contact ->
                showAddEditContactDialog(contact)
            },
            onRemoveClick = { contact -> // 'contact' é o parâmetro do lambda
                showRemoveContactConfirmationDialog(contact) // Apenas a chamada da função é necessária
            },
            onCallClick = { contact ->
                openDialerWithNumber(contact.fullNumber)
            }
        )
        contactsRecyclerView.apply {
            adapter = contactsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun showAddEditContactDialog(contactToEdit: EmergencyContact?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_edit_contact, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.edit_text_contact_name)
        val editTextNumber = dialogView.findViewById<EditText>(R.id.edit_text_contact_number)
        val editTextDescription = dialogView.findViewById<EditText>(R.id.edit_text_contact_description)


        val dialogTitle = if (contactToEdit == null) "Adicionar Contato" else "Editar Contato"

        if (contactToEdit != null) {
            editTextName.setText(contactToEdit.name)
            editTextNumber.setText(contactToEdit.number)
            editTextDescription.setText(contactToEdit.description)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton(if (contactToEdit == null) "Adicionar" else "Salvar") { dialog, _ ->
                val name = editTextName.text.toString().trim()
                val number = editTextNumber.text.toString().trim()
                val description = editTextDescription.text.toString().trim()

                if (name.isNotEmpty() && number.isNotEmpty()) {
                    if (contactToEdit == null) {
                        contactViewModel.addContact(name, number, description)
                    } else {
                        val updatedContact = contactToEdit.copy(name = name, number = number, description = description)
                        contactViewModel.updateContact(updatedContact)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private fun showRemoveContactConfirmationDialog(contact: EmergencyContact) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remover Contato")
            .setMessage("Tem certeza que deseja remover ${contact.name}?")
            .setPositiveButton("Remover") { dialog, _ ->
                contactViewModel.removeContact(contact)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        contactViewModel.refreshContacts()
    }
}