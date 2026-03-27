package com.devinfantary.khatapr

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "khatapr.db"
        const val DATABASE_VERSION = 1

        // Customers table
        const val TABLE_CUSTOMERS = "customers"
        const val COL_CUSTOMER_ID = "id"
        const val COL_CUSTOMER_NAME = "name"
        const val COL_CUSTOMER_PHONE = "phone"
        const val COL_CUSTOMER_ADDRESS = "address"

        // Transactions table
        const val TABLE_TRANSACTIONS = "transactions"
        const val COL_TXN_ID = "id"
        const val COL_TXN_CUSTOMER_ID = "customer_id"
        const val COL_TXN_DATE = "date"
        const val COL_TXN_ITEM_NAME = "item_name"
        const val COL_TXN_QUANTITY = "quantity"
        const val COL_TXN_UNIT_PRICE = "unit_price"
        const val COL_TXN_TOTAL = "total_amount"
        const val COL_TXN_AMOUNT_PAID = "amount_paid"
        const val COL_TXN_STATUS = "status" // "Paid", "Unpaid", "Partial"
        const val COL_TXN_NOTES = "notes"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createCustomers = """
            CREATE TABLE $TABLE_CUSTOMERS (
                $COL_CUSTOMER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CUSTOMER_NAME TEXT NOT NULL,
                $COL_CUSTOMER_PHONE TEXT,
                $COL_CUSTOMER_ADDRESS TEXT
            )
        """.trimIndent()

        val createTransactions = """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COL_TXN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TXN_CUSTOMER_ID INTEGER NOT NULL,
                $COL_TXN_DATE TEXT NOT NULL,
                $COL_TXN_ITEM_NAME TEXT NOT NULL,
                $COL_TXN_QUANTITY REAL NOT NULL,
                $COL_TXN_UNIT_PRICE REAL NOT NULL,
                $COL_TXN_TOTAL REAL NOT NULL,
                $COL_TXN_AMOUNT_PAID REAL NOT NULL DEFAULT 0,
                $COL_TXN_STATUS TEXT NOT NULL DEFAULT 'Unpaid',
                $COL_TXN_NOTES TEXT,
                FOREIGN KEY($COL_TXN_CUSTOMER_ID) REFERENCES $TABLE_CUSTOMERS($COL_CUSTOMER_ID)
            )
        """.trimIndent()

        db.execSQL(createCustomers)
        db.execSQL(createTransactions)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CUSTOMERS")
        onCreate(db)
    }

    // ── CUSTOMER FUNCTIONS ──

    fun addCustomer(name: String, phone: String, address: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CUSTOMER_NAME, name)
            put(COL_CUSTOMER_PHONE, phone)
            put(COL_CUSTOMER_ADDRESS, address)
        }
        return db.insert(TABLE_CUSTOMERS, null, values)
    }

    fun getAllCustomers(): List<Customer> {
        val list = mutableListOf<Customer>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CUSTOMERS ORDER BY $COL_CUSTOMER_NAME ASC", null)
        while (cursor.moveToNext()) {
            list.add(
                Customer(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CUSTOMER_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_CUSTOMER_NAME)),
                    phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_CUSTOMER_PHONE)),
                    address = cursor.getString(cursor.getColumnIndexOrThrow(COL_CUSTOMER_ADDRESS))
                )
            )
        }
        cursor.close()
        return list
    }

    fun deleteCustomer(customerId: Int) {
        val db = writableDatabase
        db.delete(TABLE_TRANSACTIONS, "$COL_TXN_CUSTOMER_ID=?", arrayOf(customerId.toString()))
        db.delete(TABLE_CUSTOMERS, "$COL_CUSTOMER_ID=?", arrayOf(customerId.toString()))
    }

    // ── TRANSACTION FUNCTIONS ──

    fun addTransaction(
        customerId: Int, date: String, itemName: String,
        quantity: Double, unitPrice: Double, totalAmount: Double,
        amountPaid: Double, status: String, notes: String
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TXN_CUSTOMER_ID, customerId)
            put(COL_TXN_DATE, date)
            put(COL_TXN_ITEM_NAME, itemName)
            put(COL_TXN_QUANTITY, quantity)
            put(COL_TXN_UNIT_PRICE, unitPrice)
            put(COL_TXN_TOTAL, totalAmount)
            put(COL_TXN_AMOUNT_PAID, amountPaid)
            put(COL_TXN_STATUS, status)
            put(COL_TXN_NOTES, notes)
        }
        return db.insert(TABLE_TRANSACTIONS, null, values)
    }

    fun getTransactionsForCustomer(customerId: Int): List<Transaction> {
        val list = mutableListOf<Transaction>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_TRANSACTIONS WHERE $COL_TXN_CUSTOMER_ID=? ORDER BY $COL_TXN_DATE DESC",
            arrayOf(customerId.toString())
        )
        while (cursor.moveToNext()) {
            list.add(
                Transaction(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TXN_ID)),
                    customerId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TXN_CUSTOMER_ID)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COL_TXN_DATE)),
                    itemName = cursor.getString(cursor.getColumnIndexOrThrow(COL_TXN_ITEM_NAME)),
                    quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TXN_QUANTITY)),
                    unitPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TXN_UNIT_PRICE)),
                    totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TXN_TOTAL)),
                    amountPaid = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TXN_AMOUNT_PAID)),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(COL_TXN_STATUS)),
                    notes = cursor.getString(cursor.getColumnIndexOrThrow(COL_TXN_NOTES))
                )
            )
        }
        cursor.close()
        return list
    }

    fun deleteTransaction(transactionId: Int) {
        val db = writableDatabase
        db.delete(TABLE_TRANSACTIONS, "$COL_TXN_ID=?", arrayOf(transactionId.toString()))
    }

    fun getTotalOutstanding(customerId: Int): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COL_TXN_TOTAL - $COL_TXN_AMOUNT_PAID) FROM $TABLE_TRANSACTIONS WHERE $COL_TXN_CUSTOMER_ID=?",
            arrayOf(customerId.toString())
        )
        val result = if (cursor.moveToFirst()) cursor.getDouble(0) else 0.0
        cursor.close()
        return result
    }

    fun getTotalPaid(customerId: Int): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COL_TXN_AMOUNT_PAID) FROM $TABLE_TRANSACTIONS WHERE $COL_TXN_CUSTOMER_ID=?",
            arrayOf(customerId.toString())
        )
        val result = if (cursor.moveToFirst()) cursor.getDouble(0) else 0.0
        cursor.close()
        return result
    }

    fun getOverallOutstanding(): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COL_TXN_TOTAL - $COL_TXN_AMOUNT_PAID) FROM $TABLE_TRANSACTIONS", null
        )
        val result = if (cursor.moveToFirst()) cursor.getDouble(0) else 0.0
        cursor.close()
        return result
    }
}