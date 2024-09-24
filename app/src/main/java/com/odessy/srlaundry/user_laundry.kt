package com.odessy.srlaundry

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class user_laundry : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_laundry)

        val buttonNewJobOrder: Button = findViewById(R.id.buttonNewJobOrder)
        val buttonLaundryFinish: Button = findViewById(R.id.buttonLaundryFinish)
        val buttonBack: Button = findViewById(R.id.buttonBack)
        buttonNewJobOrder.setOnClickListener{
            val intent = Intent (this@user_laundry, new_job_order::class.java)
            startActivity(intent)
        }
        buttonBack.setOnClickListener{
            val intent = Intent (this@user_laundry,user_dashboard::class.java)
            startActivity(intent)
        }

    }
}