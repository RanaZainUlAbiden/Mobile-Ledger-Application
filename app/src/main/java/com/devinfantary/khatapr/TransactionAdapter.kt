package com.devinfantary.khatapr

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onDeleteClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvItemName: TextView = view.findViewById(R.id.tvItemName)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvQtyPrice: TextView = view.findViewById(R.id.tvQtyPrice)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvAmountPaid: TextView = view.findViewById(R.id.tvAmountPaid)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val txn = transactions[position]

        holder.tvItemName.text = txn.itemName
        holder.tvQtyPrice.text = "${String.format("%.1f", txn.quantity)} x Rs.${String.format("%.0f", txn.unitPrice)} = Rs.${String.format("%.0f", txn.totalAmount)}"
        holder.tvDate.text = txn.date
        holder.tvAmountPaid.text = "Paid: Rs.${String.format("%.0f", txn.amountPaid)}"

        // Status badge color
        when (txn.status) {
            "Paid" -> {
                holder.tvStatus.text = "Paid"
                holder.tvStatus.setBackgroundColor(Color.parseColor("#16A34A"))
            }
            "Partial" -> {
                holder.tvStatus.text = "Partial"
                holder.tvStatus.setBackgroundColor(Color.parseColor("#D97706"))
            }
            else -> {
                holder.tvStatus.text = "Unpaid"
                holder.tvStatus.setBackgroundColor(Color.parseColor("#EF4444"))
            }
        }

        holder.btnDelete.setOnClickListener { onDeleteClick(txn) }
    }

    override fun getItemCount() = transactions.size

    fun updateList(newList: List<Transaction>) {
        transactions = newList
        notifyDataSetChanged()
    }
}