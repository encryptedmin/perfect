package com.odessy.srlaundry

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class admin_dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)


        val buttonRegisterAccount: Button = findViewById(R.id.buttonRegisterAccount)
        buttonRegisterAccount.setOnClickListener {
            val intent = Intent(this@admin_dashboard, admin_register::class.java)
            startActivity(intent)
        }

        val logoutButton: Button = findViewById(R.id.buttonLogout)
        logoutButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // Clear the back stack so that pressing back doesn't return to the dashboard
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

val buttonUpdateLaundryPrice: Button = findViewById(R.id.buttonUpdateLaundryPrice)
        buttonUpdateLaundryPrice.setOnClickListener{
            val intent = Intent(this@admin_dashboard, admin_edit_laundry_price::class.java)
            startActivity(intent)
        }

    }
}
