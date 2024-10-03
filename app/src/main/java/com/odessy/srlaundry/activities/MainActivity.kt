package com.odessy.srlaundry.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.odessy.srlaundry.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.Accounts

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var exitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the database with lifecycleScope
        db = AppDatabase.getDatabase(this, lifecycleScope)

        // Find views
        emailInput = findViewById(R.id.editTextEmail)
        passwordInput = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.buttonLogin)
        exitButton = findViewById(R.id.exitButton)

        // Insert default admin account if no accounts exist
        insertDefaultAdminAccount()

        // Handle Login (Querying the database)
        loginButton.setOnClickListener {
            val username = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (username.isNotBlank() && password.isNotBlank()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val account = db.accountsDao().login(username, password)
                    launch(Dispatchers.Main) {
                        if (account != null) {
                            // Check the role of the account and redirect accordingly
                            if (account.role == "admin") {
                                val intent = Intent(this@MainActivity, AdminDashboard::class.java)
                                startActivity(intent)
                                finish()
                            } else if (account.role == "user") {
                                val intent = Intent(this@MainActivity, UserDashboard::class.java)
                                startActivity(intent)
                                finish()
                            }
                            Toast.makeText(this@MainActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Invalid username or password.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_SHORT).show()
            }
        }

        exitButton.setOnClickListener {
            finish()
        }
    }

    // Function to insert a default admin account into the database
    private fun insertDefaultAdminAccount() {
        lifecycleScope.launch(Dispatchers.IO) {
            val accountsList = db.accountsDao().getAllAccounts()  // Check if there are any accounts
            if (accountsList.isEmpty()) {
                // Insert default admin account if no accounts exist
                val defaultAdmin = Accounts(
                    username = "admin",
                    password = "admin123",
                    role = "admin"
                )
                db.accountsDao().insert(defaultAdmin)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Default admin account created.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
