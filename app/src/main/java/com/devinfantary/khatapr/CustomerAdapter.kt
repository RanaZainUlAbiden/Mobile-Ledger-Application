package com.devinfantary.khatapr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomerAdapter(
    private var customers: List<Customer>,
    private val dbHelper: DatabaseHelper,
    private val onItemClick: (Customer) -> Unit,
    private val onItemLongClick: (Customer) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatar: TextView = view.findViewById(R.id.tvAvatar)
        val tvName: TextView = view.findViewById(R.id.tvCustomerName)
        val tvPhone: TextView = view.findViewById(R.id.tvCustomerPhone)
        val tvOutstanding: TextView = view.findViewById(R.id.tvOutstanding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val customer = customers[position]

        // Set avatar as first letter of name
        holder.tvAvatar.text = customer.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        holder.tvName.text = customer.name
        holder.tvPhone.text = if (customer.phone.isNotEmpty()) customer.phone else "No phone"

        // Get outstanding amount from DB
        val outstanding = dbHelper.getTotalOutstanding(customer.id)
        holder.tvOutstanding.text = "Rs. ${String.format("%.0f", outstanding)}"

        // Color based on outstanding
        if (outstanding > 0) {
            holder.tvOutstanding.setTextColor(
                holder.itemView.context.getColor(R.color.danger)
            )
        } else {
            holder.tvOutstanding.setTextColor(
                holder.itemView.context.getColor(R.color.success)
            )
        }

        holder.itemView.setOnClickListener { onItemClick(customer) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(customer)
            true
        }
    }

    override fun getItemCount() = customers.size

    fun updateList(newList: List<Customer>) {
        customers = newList
        notifyDataSetChanged()
    }
}