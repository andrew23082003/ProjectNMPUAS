package com.blinc.healingapps

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blinc.healingapps.databinding.ActivityChangePasswordBinding
import org.json.JSONObject

class ActivityChangePassword : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var apiService: ApiService
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        apiService = ApiService(this)
        sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnChangePassword.setOnClickListener {
            val oldPassword = binding.etOldPassword.text.toString()
            val newPassword = binding.etNewPassword.text.toString()
            val repeatPassword = binding.etRepeatNewPassword.text.toString()

            if (validatePasswords(oldPassword, newPassword, repeatPassword)) {
                val userId = sharedPref.getInt("user_id", -1)
                if (userId == -1) {
                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                binding.btnChangePassword.isEnabled = false

                // Kirim permintaan GET ke API
                apiService.changePassword(
                    userId,
                    oldPassword,
                    newPassword,
                    repeatPassword,
                    object : ApiService.ApiResponseListener {
                        override fun onSuccess(response: String) {
                            runOnUiThread {
                                binding.btnChangePassword.isEnabled = true

                                try {
                                    val json = JSONObject(response)
                                    if (json.getString("status") == "success") {
                                        Toast.makeText(
                                            this@ActivityChangePassword,
                                            json.getString("message"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this@ActivityChangePassword,
                                            json.getString("message"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        this@ActivityChangePassword,
                                        "Failed to parse response",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        override fun onError(error: String) {
                            runOnUiThread {
                                binding.btnChangePassword.isEnabled = true
                                Toast.makeText(
                                    this@ActivityChangePassword,
                                    "Error: $error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    })
            }
        }
    }

    private fun validatePasswords(old: String, new: String, repeat: String): Boolean {
        if (old.isEmpty() || new.isEmpty() || repeat.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (new != repeat) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        if (new.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
