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
    private val SMS_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = AppDatabase.getDatabase(this, lifecycleScope)
        smsMessageViewModel = ViewModelProvider(this).get(SmsMessageViewModel::class.java)
        promoViewModel = ViewModelProvider(this).get(PromoViewModel::class.java)
        emailInput = findViewById(R.id.editTextEmail)
        passwordInput = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.buttonLogin)
        exitButton = findViewById(R.id.exitButton)

        insertDefaultAdminAccount()
        syncAccountsFromFirestoreToRoom()
        syncPromoFromFirestore()
        syncSmsMessagesFromFirestore()
        requestSmsPermission()

        loginButton.setOnClickListener {
            val username = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (username.isNotBlank() && password.isNotBlank()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val account = db.accountsDao().login(username, password)
                    launch(Dispatchers.Main) {
                        if (account != null) {
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
    private fun requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                showPermissionRationaleDialog()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SMS_PERMISSION_CODE)
            }
        }
    }
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
    private fun insertDefaultAdminAccount() {
        lifecycleScope.launch(Dispatchers.IO) {
            val accountsList = db.accountsDao().getAllAccounts()
            if (accountsList.isEmpty()) {
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
                    val existingAccount = db.accountsDao().getAccountByUsername(username)
                    if (existingAccount == null) {
                        Log.d("Sync", "Account does not exist in Room. Inserting $username")
                        val account =
                            Accounts(username = username, password = password, role = role)
                        db.accountsDao().insert(account)
                    } else {
                        Log.d("Sync", "Account $username already exists in Room")
                    }
                }
            } catch (e: Exception) {
                Log.e("Sync", "Failed to sync accounts: ${e.message}")
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to sync accounts: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    private fun syncPromoFromFirestore() {
        promoViewModel.syncPromoFromFirestore()
    }
    private fun syncSmsMessagesFromFirestore() {
        smsMessageViewModel.syncSmsMessagesFromFirestore() 
    }
}
