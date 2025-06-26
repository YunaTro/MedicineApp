package com.example.medicineapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.view.View
import android.widget.AdapterView
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.widget.Button
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: MedicineAdapter
    private var fullList: List<Medicine> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.medicineRecyclerView)
        val searchEditText = findViewById<EditText>(R.id.searchEditText)

        recyclerView.layoutManager = LinearLayoutManager(this)


        adapter = MedicineAdapter(fullList) { selectedMedicine ->
            showMedicineOptionsDialog(selectedMedicine)
        }
        recyclerView.adapter = adapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                val filtered = fullList.filter {
                    it.name.contains(text.toString(), ignoreCase = true) ||
                            it.substance.contains(text.toString(), ignoreCase = true)
                }
                adapter.updateList(filtered)
            }
        })

        val sortSpinner = findViewById<Spinner>(R.id.sortSpinner)
        val sortAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.sort_options,
            android.R.layout.simple_spinner_item
        )
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = sortAdapter

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val sortedList = when (position) {
                    0 -> fullList.sortedBy { it.expirationDate }
                    1 -> fullList.sortedByDescending { it.expirationDate }
                    else -> fullList
                }
                adapter.updateList(sortedList)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        val addButton = findViewById<Button>(R.id.addButton)
        addButton.setOnClickListener {
            showAddMedicineDialog()
        }
        MedicineRepository.listen { list ->
            fullList = list
            adapter.updateList(fullList)
        }
    }

    private fun showAddMedicineDialog(editMedicine: Medicine? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_medicine, null)
        val nameField = dialogView.findViewById<EditText>(R.id.editName)
        val substanceField = dialogView.findViewById<EditText>(R.id.editSubstance)
        val quantityField = dialogView.findViewById<EditText>(R.id.editQuantity)

        val calendar = Calendar.getInstance()
        var expirationTimestamp = System.currentTimeMillis()

        val daySpinner = dialogView.findViewById<Spinner>(R.id.daySpinner)
        val monthSpinner = dialogView.findViewById<Spinner>(R.id.monthSpinner)
        val yearSpinner = dialogView.findViewById<Spinner>(R.id.yearSpinner)

        // Дни от 1 до 31
        val days = (1..31).map { it.toString() }
        daySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, days)

        // Месяцы по-русски
        val months = listOf(
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        )
        monthSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)

        // Годы от текущего до +10
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear..(currentYear + 10)).map { it.toString() }
        yearSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)

        if (editMedicine != null) {
            nameField.setText(editMedicine.name)
            substanceField.setText(editMedicine.substance)
            quantityField.setText(editMedicine.quantity)

            val cal = Calendar.getInstance()
            cal.timeInMillis = editMedicine.expirationDate
            daySpinner.setSelection(cal.get(Calendar.DAY_OF_MONTH) - 1)
            monthSpinner.setSelection(cal.get(Calendar.MONTH))
            yearSpinner.setSelection(years.indexOf(cal.get(Calendar.YEAR).toString()))
        }

        AlertDialog.Builder(this)
            .setTitle(if (editMedicine != null) "Изменить лекарство" else "Новое лекарство")
            .setView(dialogView)
            .setPositiveButton(if (editMedicine != null) "Сохранить" else "Добавить") { _, _ ->
                val name = nameField.text.toString()
                val substance = substanceField.text.toString()
                val quantity = quantityField.text.toString()
                val day = daySpinner.selectedItemPosition + 1
                val month = monthSpinner.selectedItemPosition // 0–11
                val year = years[yearSpinner.selectedItemPosition].toInt()

                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                expirationTimestamp = calendar.timeInMillis

                val newMedicine = Medicine(name, substance, quantity, expirationTimestamp)
                fullList = if (editMedicine != null) {
                    fullList.map { if (it == editMedicine) newMedicine else it }
                } else {
                    fullList + newMedicine
                }
                adapter.updateList(fullList)
                MedicineRepository.addOrUpdate(newMedicine)
            }
            .setNegativeButton("Отмена", null)
            .show()

    }
    private fun showMedicineOptionsDialog(medicine: Medicine) {
        AlertDialog.Builder(this)
            .setTitle(medicine.name)
            .setMessage("Что сделать с этим лекарством?")
            .setPositiveButton("Изменить") { _, _ ->
                showAddMedicineDialog(editMedicine = medicine)
            }
            .setNegativeButton("Удалить") { _, _ ->
                fullList = fullList.filter { it != medicine }
                adapter.updateList(fullList)
                MedicineRepository.delete(medicine)
            }
            .setNeutralButton("Отмена", null)
            .show()

    }
}
