package com.odessy.srlaundry.activities

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.odessy.srlaundry.R
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.Accounts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminRegister : AppCompatActivity() {

    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var roleSpinner: Spinner
    private lateinit var registerButton: Button
    private lateinit var buttonCancelRegister: Button
    private var selectedRole: String = "user"

    // Firestore instance
    private val firestoreDb = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_register)

        usernameInput = findViewById(R.id.editTextUsername)
        passwordInput = findViewById(R.id.editTextPassword)
        roleSpinner = findViewById(R.id.spinnerRole)
        registerButton = findViewById(R.id.buttonRegister)
        buttonCancelRegister = findViewById(R.id.buttonCancelRegister)

        val roles = arrayOf("admin", "user")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter

        roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View, position: Int, id: Long) {
                selectedRole = roles[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedRole = "user"
            }
        }

        registerButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val account = Accounts(username = username, password = password, role = selectedRole)
                registerAccount(account)
            } else {
                Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_SHORT).show()
            }
        }

        buttonCancelRegister.setOnClickListener {
            val intent = Intent(this@AdminRegister, AdminDashboard::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Register the account in both Room and Firestore
    private fun registerAccount(account: Accounts) {
        val db = AppDatabase.getDatabase(this, lifecycleScope)

        lifecycleScope.launch(Dispatchers.IO) {
            // Insert into Room database
            db.accountsDao().insert(account)

            // Save to Firestore
            saveAccountToFirestore(account)

            // Run the success message on the main thread
            runOnUiThread {
                Toast.makeText(this@AdminRegister, "Registration successful!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // Function to save the account to Firestore
    private fun saveAccountToFirestore(account: Accounts) {
        val accountData = hashMapOf(
            "username" to account.username,
            "password" to account.password, // In production, passwords should be encrypted!
            "role" to account.role
        )

        firestoreDb.collection("accounts")
            .add(accountData)
            .addOnSuccessListener {
                // Success logging
                runOnUiThread {
                    Toast.makeText(this@AdminRegister, "Account saved to Firestore!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // Error logging
                runOnUiThread {
                    Toast.makeText(this@AdminRegister, "Error saving account to Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
