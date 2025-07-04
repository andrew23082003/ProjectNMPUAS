package com.blinc.healingapps

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blinc.healingapps.databinding.ActivityLoginBinding
import org.json.JSONObject

class ActivityLogin : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var apiService: ApiService
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        apiService = ApiService(this)
        sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)

        if (sharedPref.getInt("user_id", -1) != -1) {
            navigateToMainActivity()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.tvSignUp?.setOnClickListener {
            val intent = Intent(this, ActivitySignUp::class.java)
            startActivity(intent)
        }

        binding.btnSignIn?.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnSignIn.isEnabled = false

            apiService.login(email, password, object : ApiService.ApiResponseListener {
                override fun onSuccess(response: String) {
                    runOnUiThread {
                        binding.btnSignIn.isEnabled = true

                        try {
                            val json = JSONObject(response)
                            if (json.getString("status") == "success") {
                                val userData = json.getJSONObject("data")

                                with(sharedPref.edit()) {
                                    putInt("user_id", userData.getInt("id"))
                                    putString("user_name", userData.getString("name"))
                                    putString("user_email", userData.getString("email"))
                                    apply()
                                }

                                navigateToMainActivity()
                            } else {
                                Toast.makeText(
                                    this@ActivityLogin,
                                    json.getString("message"),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@ActivityLogin,
                                "Gagal memproses respons",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("LoginActivity", "Error parsing response: ${e.message}")
                        }
                    }
                }

                override fun onError(error: String) {
                    runOnUiThread {
                        binding.btnSignIn.isEnabled = true
                        Toast.makeText(this@ActivityLogin, "Error: $error", Toast.LENGTH_SHORT).show()
                        Log.e("LoginActivity", "Login error: $error")
                    }
                }
            })
        }

        binding.tvForgotPassword?.setOnClickListener {
            Toast.makeText(this, "Fitur lupa password akan datang", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}