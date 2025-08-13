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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.text.isNotEmpty
import kotlin.text.trim

class ContactFragment : Fragment() {

    private val contactViewModel: ContactViewModel by viewModels()
    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var contactsAdapter: EmergencyContactAdapter // Você precisará criar este adapter
    private lateinit var fabAddContact: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_contact, container, false) // Crie este layout

        contactsRecyclerView = view.findViewById(R.id.recycler_view_contacts)
        fabAddContact = view.findViewById(R.id.fab_add_contact)

        setupRecyclerView()

        fabAddContact.setOnClickListener {
            showAddEditContactDialog(null) // null para adicionar novo contato
        }

        contactViewModel.emergencyContacts.observe(viewLifecycleOwner) { contacts ->
            contactsAdapter.submitList(contacts)
        }

        return view
    }
    private fun openDialerWithNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "Nenhum aplicativo de discagem encontrado.", Toast.LENGTH_SHORT).show()
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
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_edit_contact, null) // Crie este layout de diálogo
        val editTextName = dialogView.findViewById<EditText>(R.id.edit_text_contact_name)
        val editTextNumber = dialogView.findViewById<EditText>(R.id.edit_text_contact_number)
        // Opcional: Se você quiser editar o DDD manualmente, adicione um EditText para ele.
        // val editTextDDD = dialogView.findViewById<EditText>(R.id.edit_text_contact_ddd)


        val dialogTitle = if (contactToEdit == null) "Adicionar Contato" else "Editar Contato"

        if (contactToEdit != null) {
            editTextName.setText(contactToEdit.name)
            editTextNumber.setText(contactToEdit.number) // Mostra apenas o número sem DDD para edição
            // if (editTextDDD != null) editTextDDD.setText(contactToEdit.ddd)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton(if (contactToEdit == null) "Adicionar" else "Salvar") { dialog, _ ->
                val name = editTextName.text.toString().trim()
                val number = editTextNumber.text.toString().trim()
                // val ddd = if (editTextDDD != null) editTextDDD.text.toString().trim() else ""


                if (name.isNotEmpty() && number.isNotEmpty()) {
                    if (contactToEdit == null) {
                        // DDD será adicionado automaticamente pelo ViewModel/Repository
                        contactViewModel.addContact(name, number)
                    } else {
                        // Se você tem um campo DDD no diálogo:
                        // val updatedContact = contactToEdit.copy(name = name, ddd = ddd, number = number)
                        // Se não, o DDD existente será mantido ou o padrão será usado se estava em branco
                        val updatedContact = contactToEdit.copy(name = name, number = number)
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