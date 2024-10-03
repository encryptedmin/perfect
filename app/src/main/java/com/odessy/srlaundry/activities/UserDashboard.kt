package com.odessy.srlaundry.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.odessy.srlaundry.R

class UserDashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        // Find the views
        val laundryServiceButton: ImageButton = findViewById(R.id.buttonLaundryService)
        val storeButton: ImageButton = findViewById(R.id.buttonMiniStore)
        val logoutButton: Button = findViewById(R.id.buttonLogout)

        // Set up intent for Laundry Service button
        laundryServiceButton.setOnClickListener {
            val intent = Intent(this@UserDashboard, UserLaundry::class.java)
            startActivity(intent)
        }

        // Set up intent for Store button
        storeButton.setOnClickListener {
            val intent = Intent(this@UserDashboard, StoreActivity::class.java)
            startActivity(intent)
        }

        // Set up intent for Logout button
        logoutButton.setOnClickListener {
            val intent = Intent(this@UserDashboard, MainActivity::class.java)
            // Clear the back stack to prevent navigating back to the dashboard
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
