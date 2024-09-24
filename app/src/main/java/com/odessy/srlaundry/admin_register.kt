package com.odessy.srlaundry

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
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.Accounts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class admin_register : AppCompatActivity() {

    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var roleSpinner: Spinner
    private lateinit var registerButton: Button
    private lateinit var buttonCancelRegister: Button // Declare the cancel button
    private var selectedRole: String = "user" // Default role

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_register)

        // Initialize views
        usernameInput = findViewById(R.id.editTextUsername)
        passwordInput = findViewById(R.id.editTextPassword)
        roleSpinner = findViewById(R.id.spinnerRole)
        registerButton = findViewById(R.id.buttonRegister)
        buttonCancelRegister = findViewById(R.id.buttonCancelRegister) // Initialize the cancel button

        // Setup spinner
        val roles = arrayOf("admin", "user")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter

        roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View, position: Int, id: Long) {
                selectedRole = roles[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedRole = "user" // Default value
            }
        }

        // Handle Register button click
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

        // Handle Cancel button click
        buttonCancelRegister.setOnClickListener {
            // Redirect to admin_dashboard activity
            val intent = Intent(this@admin_register, admin_dashboard::class.java)
            startActivity(intent)
            finish() // Optionally close the registration activity
        }
    }

    private fun registerAccount(account: Accounts) {
        val db = AppDatabase.getDatabase(this, lifecycleScope)

        lifecycleScope.launch(Dispatchers.IO) {
            db.accountsDao().insert(account) // Assuming you have an insert method in your AccountsDao
            runOnUiThread {
                Toast.makeText(this@admin_register, "Registration successful!", Toast.LENGTH_SHORT).show()
                finish() // Close the registration activity
            }
        }
    }
}
