package com.blinc.healingapps

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blinc.healingapps.databinding.ActivitySignUpBinding
import org.json.JSONObject

class ActivitySignUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        apiService = ApiService(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSignUp.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateSignUp(name, email, password)) {
                binding.btnSignUp.isEnabled = false

                apiService.signup(name, email, password, object : ApiService.ApiResponseListener {
                    override fun onSuccess(response: String) {
                        runOnUiThread {
                            binding.btnSignUp.isEnabled = true

                            try {
                                val json = JSONObject(response)
                                if (json.getString("status") == "success") {
                                    val userData = json.getJSONObject("data")

                                    Toast.makeText(
                                        this@ActivitySignUp,
                                        "Registration successful",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val intent = Intent(this@ActivitySignUp, ActivityLogin::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@ActivitySignUp,
                                        json.getString("message"),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@ActivitySignUp,
                                    "Failed to parse response",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onError(error: String) {
                        runOnUiThread {
                            binding.btnSignUp.isEnabled = true
                            Toast.makeText(this@ActivitySignUp, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
        }

        binding.tvSignIn.setOnClickListener {
            val intent = Intent(this, ActivityLogin::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateSignUp(name: String, email: String, password: String): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}