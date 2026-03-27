package com.devinfantary.khatapr

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class CustomerDetailActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: TransactionAdapter
    private lateinit var rvTransactions: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var tvCustomerName: TextView
    private lateinit var tvOutstanding: TextView
    private lateinit var tvTotalPaid: TextView
    private var customerId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_detail)

        dbHelper = DatabaseHelper(this)
        customerId = intent.getIntExtra("customer_id", -1)
        val customerName = intent.getStringExtra("customer_name") ?: "Customer"

        // Link views
        tvCustomerName = findViewById(R.id.tvCustomerName)
        tvOutstanding = findViewById(R.id.tvOutstanding)
        tvTotalPaid = findViewById(R.id.tvTotalPaid)
        rvTransactions = findViewById(R.id.rvTransactions)
        layoutEmpty = findViewById(R.id.layoutEmpty)

        tvCustomerName.text = customerName

        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Setup adapter
        adapter = TransactionAdapter(
            transactions = emptyList(),
            onDeleteClick = { transaction ->
                showDeleteTransactionDialog(transaction)
            }
        )

        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = adapter

        // Add transaction button
        findViewById<FloatingActionButton>(R.id.fabAddTransaction).setOnClickListener {
            showAddTransactionDialog()
        }

        loadTransactions()
    }

    private fun loadTransactions() {
        val transactions = dbHelper.getTransactionsForCustomer(customerId)
        adapter.updateList(transactions)

        if (transactions.isEmpty()) {
            rvTransactions.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
        } else {
            rvTransactions.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
        }

        val outstanding = dbHelper.getTotalOutstanding(customerId)
        val paid = dbHelper.getTotalPaid(customerId)
        tvOutstanding.text = "Rs. ${String.format("%.0f", outstanding)}"
        tvTotalPaid.text = "Rs. ${String.format("%.0f", paid)}"
    }

    private fun showAddTransactionDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_transaction, null)
        val etItem = view.findViewById<EditText>(R.id.etItemName)
        val etQty = view.findViewById<EditText>(R.id.etQuantity)
        val etUnitPrice = view.findViewById<EditText>(R.id.etUnitPrice)
        val etAmountPaid = view.findViewById<EditText>(R.id.etAmountPaid)
        val etNotes = view.findViewById<EditText>(R.id.etNotes)
        val tvTotal = view.findViewById<TextView>(R.id.tvCalculatedTotal)
        val spinnerStatus = view.findViewById<Spinner>(R.id.spinnerStatus)

        // Auto calculate total
        val watcher = object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                val qty = etQty.text.toString().toDoubleOrNull() ?: 0.0
                val price = etUnitPrice.text.toString().toDoubleOrNull() ?: 0.0
                val total = qty * price
                tvTotal.text = "Total: Rs. ${String.format("%.0f", total)}"
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        etQty.addTextChangedListener(watcher)
        etUnitPrice.addTextChangedListener(watcher)

        // Status spinner
        val statuses = arrayOf("Unpaid", "Partial", "Paid")
        spinnerStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statuses)

        AlertDialog.Builder(this)
            .setTitle("Add Transaction")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val itemName = etItem.text.toString().trim()
                val qty = etQty.text.toString().toDoubleOrNull() ?: 0.0
                val unitPrice = etUnitPrice.text.toString().toDoubleOrNull() ?: 0.0
                val total = qty * unitPrice
                val amountPaid = etAmountPaid.text.toString().toDoubleOrNull() ?: 0.0
                val notes = etNotes.text.toString().trim()
                val status = spinnerStatus.selectedItem.toString()
                val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

                if (itemName.isNotEmpty() && qty > 0 && unitPrice > 0) {
                    dbHelper.addTransaction(
                        customerId, date, itemName,
                        qty, unitPrice, total,
                        amountPaid, status, notes
                    )
                    loadTransactions()
                } else {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteTransactionDialog(transaction: Transaction) {
        AlertDialog.Builder(this)
            .setTitle("Delete Transaction")
            .setMessage("Delete this transaction for ${transaction.itemName}?")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteTransaction(transaction.id)
                loadTransactions()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}