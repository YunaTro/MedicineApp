package com.example.medicineapp

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject

object MedicineRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("medicines")

    private var listener: ListenerRegistration? = null

    fun listen(onChange: (List<Medicine>) -> Unit) {
        listener?.remove() // чтобы не дублировалось
        listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                onChange(emptyList())
                return@addSnapshotListener
            }

            val medicines = snapshot.documents.mapNotNull { it.toObject<Medicine>() }
            onChange(medicines)
        }
    }

    fun addOrUpdate(medicine: Medicine) {
        // используем имя лекарства как ключ (можно заменить на UUID)
        collection.document(medicine.name).set(medicine)
    }

    fun delete(medicine: Medicine) {
        collection.document(medicine.name).delete()
    }

    fun stopListening() {
        listener?.remove()
    }
}