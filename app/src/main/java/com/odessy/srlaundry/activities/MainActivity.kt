package com.odessy.srlaundry.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.odessy.srlaundry.R
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.Accounts
import com.odessy.srlaundry.others.PromoViewModel
import com.odessy.srlaundry.others.SmsMessageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var exitButton: Button
    private lateinit var smsMessageViewModel: SmsMessageViewModel
    private lateinit var promoViewModel: PromoViewModel
    private val firestoreDb = FirebaseFirestore.getInstance()

    // SMS Permission Request Code
    private val SMS_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the database with lifecycleScope
        db = AppDatabase.getDatabase(this, lifecycleScope)

        // Initialize the ViewModel for SMS messages and promotions
        smsMessageViewModel = ViewModelProvider(this).get(SmsMessageViewModel::class.java)
        promoViewModel = ViewModelProvider(this).get(PromoViewModel::class.java)

        // Find views
        emailInput = findViewById(R.id.editTextEmail)
        passwordInput = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.buttonLogin)
        exitButton = findViewById(R.id.exitButton)

        // Insert default admin account if no accounts exist
        insertDefaultAdminAccount()

        // Sync accounts and promotions from Firestore to Room when app starts
        syncAccountsFromFirestoreToRoom()
        syncPromoFromFirestore()
        syncSmsMessagesFromFirestore()

        // Request SMS permission at startup
        requestSmsPermission()

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

    // Function to request SMS permission
    private fun requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // If the permission is not granted, check if we should show a rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                // Show an explanation to the user
                showPermissionRationaleDialog()
            } else {
                // Directly request the permission
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SMS_PERMISSION_CODE)
            }
        }
    }

    // Dialog to explain why the SMS permission is needed
    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("SMS Permission Required")
            .setMessage("This app needs SMS permission to send notifications to customers.")
            .setPositiveButton("Grant") { _, _ ->
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SMS_PERMISSION_CODE)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "SMS permission denied. You won't be able to send SMS notifications.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Function to insert a default admin account into the Room database (locally only)
    private fun insertDefaultAdminAccount() {
        lifecycleScope.launch(Dispatchers.IO) {
            val accountsList = db.accountsDao().getAllAccounts()  // Check if there are any accounts
            if (accountsList.isEmpty()) {
                // Insert default admin account if no accounts exist (locally only)
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

    // Sync accounts from Firestore to Room
    private fun syncAccountsFromFirestoreToRoom() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val firestoreAccounts = firestoreDb.collection("accounts").get().await()
                Log.d("Sync", "Accounts fetched from Firestore: ${firestoreAccounts.size()}")

                for (document in firestoreAccounts.documents) {
                    val username = document.getString("username") ?: continue
                    val password = document.getString("password") ?: continue
                    val role = document.getString("role") ?: continue

                    Log.d("Sync", "Account fetched: $username")

                    // Check if the account already exists in Room
                    val existingAccount = db.accountsDao().getAccountByUsername(username)
                    if (existingAccount == null) {
                        Log.d("Sync", "Account does not exist in Room. Inserting $username")

                        // Insert account into Room if it doesn't exist
                        val account = Accounts(username = username, password = password, role = role)
                        db.accountsDao().insert(account)
                    } else {
                        Log.d("Sync", "Account $username already exists in Room")
                    }
                }
            } catch (e: Exception) {
                Log.e("Sync", "Failed to sync accounts: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Failed to sync accounts: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Sync promotions from Firestore to Room
    private fun syncPromoFromFirestore() {
        promoViewModel.syncPromoFromFirestore() // Use the ViewModel method to sync promotions
    }

    // Sync SMS messages from Firestore to Room
    private fun syncSmsMessagesFromFirestore() {
        smsMessageViewModel.syncSmsMessagesFromFirestore() // Use the ViewModel method to sync SMS messages
    }
}
