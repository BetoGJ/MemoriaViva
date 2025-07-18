package com.example.memoriaviva2.ui.contacts // Ajuste o pacote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.memoriaviva2.ui.contacts.EmergencyContactRepository
import com.example.memoriaviva2.ui.contacts.EmergencyContact

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EmergencyContactRepository = EmergencyContactRepository(application)

    private val _emergencyContacts = MutableLiveData<List<EmergencyContact>>()
    val emergencyContacts: LiveData<List<EmergencyContact>> = _emergencyContacts

    init {
        loadContacts()
    }

    fun loadContacts() {
        _emergencyContacts.value = repository.getEmergencyContacts()
    }

    fun addContact(name: String, number: String) {
        // O número já vem limpo do input do usuário, sem DDD
        // O DDD será adicionado automaticamente pelo repositório
        val newContact = EmergencyContact(name = name, number = number)
        repository.addEmergencyContact(newContact)
        loadContacts() // Recarrega a lista
    }

    fun removeContact(contact: EmergencyContact) {
        repository.removeEmergencyContact(contact.id)
        loadContacts() // Recarrega a lista
    }

    fun updateContact(contact: EmergencyContact) {
        repository.updateEmergencyContact(contact)
        loadContacts()
    }
}