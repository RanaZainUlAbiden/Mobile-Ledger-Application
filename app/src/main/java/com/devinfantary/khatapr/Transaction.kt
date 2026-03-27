package com.devinfantary.khatapr

data class Transaction(
    val id: Int = 0,
    val customerId: Int = 0,
    val date: String = "",
    val itemName: String = "",
    val quantity: Double = 0.0,
    val unitPrice: Double = 0.0,
    val totalAmount: Double = 0.0,
    val amountPaid: Double = 0.0,
    val status: String = "Unpaid",
    val notes: String = ""
)