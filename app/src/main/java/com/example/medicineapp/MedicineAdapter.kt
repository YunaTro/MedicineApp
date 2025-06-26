package com.example.medicineapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MedicineAdapter(
    private var items: List<Medicine>,
    private val onItemClick: (Medicine) -> Unit
) : RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {

    inner class MedicineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.nameText)
        val substance: TextView = view.findViewById(R.id.substanceText)
        val quantity: TextView = view.findViewById(R.id.quantityText)
        val date: TextView = view.findViewById(R.id.dateText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine, parent, false)
        return MedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val med = items[position]
        holder.name.text = med.name
        holder.substance.text = "Действующее вещество: ${med.substance}"
        holder.quantity.text = "Количество: ${med.quantity}"
        holder.date.text = "Годен до: ${
            java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale("ru"))
                .format(java.util.Date(med.expirationDate))
        }"

        holder.itemView.setOnClickListener {
            onItemClick(med)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<Medicine>) {
        items = newList
        notifyDataSetChanged()
    }
}

