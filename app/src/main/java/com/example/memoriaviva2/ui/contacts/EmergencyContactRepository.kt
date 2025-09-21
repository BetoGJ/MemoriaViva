package com.example.memoriaviva2.ui.contacts
import android.content.Context
import android.content.SharedPreferences
import com.example.memoriaviva2.ui.contacts.EmergencyContact
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Locale

class EmergencyContactRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MemoriaVivaEmergencyContactsPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val contactsKey = "emergency_contacts_list"

    companion object {
        // Mapeamento simples de país para DDD. Pode ser expandido.
        private val COUNTRY_DDD_MAP = mapOf(
            "BR" to "55", // Brasil
            "US" to "1",  // Estados Unidos
            "PT" to "351" // Portugal
            // Adicione outros países e DDDs conforme necessário
        )

        fun getDefaultDDDForCurrentCountry(): String {
            val countryCode = Locale.getDefault().country // Obtém o código do país (ex: "BR", "US")
            return COUNTRY_DDD_MAP[countryCode.uppercase()] ?: "1" // Retorna "1" como padrão se não encontrado
        }
    }

    fun saveEmergencyContacts(contacts: List<EmergencyContact>) {
        val jsonContacts = gson.toJson(contacts)
        sharedPreferences.edit().putString(contactsKey, jsonContacts).apply()
    }

    fun getEmergencyContacts(): MutableList<EmergencyContact> {
        val jsonContacts = sharedPreferences.getString(contactsKey, null)
        return if (jsonContacts != null) {
            try {
                val type = object : TypeToken<MutableList<EmergencyContact>>() {}.type
                gson.fromJson(jsonContacts, type) ?: mutableListOf()
            } catch (e: Exception) {
                // Se falhar ao deserializar (contatos antigos sem description), limpa e retorna lista vazia
                sharedPreferences.edit().remove(contactsKey).apply()
                mutableListOf()
            }
        } else {
            mutableListOf()
        }
    }

    fun addEmergencyContact(contact: EmergencyContact) {
        val contacts = getEmergencyContacts()
        // Adiciona o DDD padrão se não estiver presente
        val contactWithDDD = if (contact.ddd.isBlank()) {
            contact.copy(ddd = getDefaultDDDForCurrentCountry())
        } else {
            contact
        }
        contacts.add(contactWithDDD)
        saveEmergencyContacts(contacts)
    }

    fun removeEmergencyContact(contactId: String) {
        val contacts = getEmergencyContacts()
        contacts.removeAll { it.id == contactId }
        saveEmergencyContacts(contacts)
    }

    fun updateEmergencyContact(updatedContact: EmergencyContact) {
        val contacts = getEmergencyContacts()
        val index = contacts.indexOfFirst { it.id == updatedContact.id }
        if (index != -1) {
            // Adiciona o DDD padrão se não estiver presente ao atualizar
            val contactWithDDD = if (updatedContact.ddd.isBlank() && contacts[index].ddd.isNotBlank()) {
                // Mantém o DDD existente se o atualizado não tiver um, mas o antigo tinha
                updatedContact.copy(ddd = contacts[index].ddd)
            } else if (updatedContact.ddd.isBlank()){
                updatedContact.copy(ddd = getDefaultDDDForCurrentCountry())
            }
            else {
                updatedContact
            }
            contacts[index] = contactWithDDD
            saveEmergencyContacts(contacts)
        }
    }
}