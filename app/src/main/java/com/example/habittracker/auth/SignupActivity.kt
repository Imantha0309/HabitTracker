package com.example.habittracker.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.MainActivity
import com.example.habittracker.R

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val btnGoSignup2 = findViewById<TextView>(R.id.tvLoginLink)

        btnSignup.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || pass.length < 6) {
                Toast.makeText(this, "Fill all fields (password ≥ 6)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            SessionManager.register(this, name, email, pass)
            SessionManager.login(this, email, pass) // ✅ auto-login

            Toast.makeText(this, "Welcome $name!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        btnGoSignup2.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
