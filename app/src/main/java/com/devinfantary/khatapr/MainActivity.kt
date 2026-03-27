package com.devinfantary.khatapr

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: CustomerAdapter
    private lateinit var rvCustomers: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var tvTotalOutstanding: TextView
    private lateinit var tvTotalCustomers: TextView
    private lateinit var tvTotalPaid: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)

        // Link views
        rvCustomers = findViewById(R.id.rvCustomers)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        tvTotalOutstanding = findViewById(R.id.tvTotalOutstanding)
        tvTotalCustomers = findViewById(R.id.tvTotalCustomers)
        tvTotalPaid = findViewById(R.id.tvTotalPaid)

        // Setup RecyclerView
        adapter = CustomerAdapter(
            customers = emptyList(),
            dbHelper = dbHelper,
            onItemClick = { customer ->
                val intent = Intent(this, CustomerDetailActivity::class.java)
                intent.putExtra("customer_id", customer.id)
                intent.putExtra("customer_name", customer.name)
                startActivity(intent)
            },
            onItemLongClick = { customer ->
                showDeleteCustomerDialog(customer)
            }
        )

        rvCustomers.layoutManager = LinearLayoutManager(this)
        rvCustomers.adapter = adapter

        // Add customer button
        findViewById<FloatingActionButton>(R.id.fabAddCustomer).setOnClickListener {
            showAddCustomerDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadCustomers()
    }

    private fun loadCustomers() {
        val customers = dbHelper.getAllCustomers()
        adapter.updateList(customers)

        if (customers.isEmpty()) {
            rvCustomers.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
        } else {
            rvCustomers.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
        }

        // Update dashboard
        tvTotalCustomers.text = customers.size.toString()
        val outstanding = dbHelper.getOverallOutstanding()
        tvTotalOutstanding.text = "Rs. ${String.format("%.0f", outstanding)}"

        // Calculate total paid across all customers
        var totalPaid = 0.0
        customers.forEach { totalPaid += dbHelper.getTotalPaid(it.id) }
        tvTotalPaid.text = "Rs. ${String.format("%.0f", totalPaid)}"
    }

    private fun showAddCustomerDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_customer, null)
        val etName = view.findViewById<EditText>(R.id.etCustomerName)
        val etPhone = view.findViewById<EditText>(R.id.etCustomerPhone)
        val etAddress = view.findViewById<EditText>(R.id.etCustomerAddress)

        AlertDialog.Builder(this)
            .setTitle("Add Customer")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val address = etAddress.text.toString().trim()
                if (name.isNotEmpty()) {
                    dbHelper.addCustomer(name, phone, address)
                    loadCustomers()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteCustomerDialog(customer: Customer) {
        AlertDialog.Builder(this)
            .setTitle("Delete Customer")
            .setMessage("Delete ${customer.name}? All their transactions will also be deleted.")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteCustomer(customer.id)
                loadCustomers()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}