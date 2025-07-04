package com.blinc.healingapps

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blinc.healingapps.databinding.ActivityDetailHealingBinding
import com.squareup.picasso.Picasso
import org.json.JSONObject

class ActivityDetailHealing : AppCompatActivity() {
    private lateinit var binding: ActivityDetailHealingBinding
    private lateinit var healingPlace: HealingPlace
    private var isFavoriteView: Boolean = false
    private lateinit var apiService: ApiService
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailHealingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        apiService = ApiService(this)
        sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)

        healingPlace = intent.getParcelableExtra("EXTRA_HEALING_PLACE")
            ?: throw NullPointerException("HealingPlace data is missing!")
        isFavoriteView = intent.getBooleanExtra("EXTRA_IS_FAVORITE_VIEW", false)

        setupViews()
        setupFavoriteButton()
        setupBackButton()
    }

    private fun setupViews() {
        binding.apply {
            tvDetailName.text = healingPlace.placeName
            tvDetailCategory.text = healingPlace.placeCategory.displayName
            tvShortDescription.text = healingPlace.briefInfo
            tvFullDescription.text = healingPlace.detailedInfo

            Picasso.get()
                .load(healingPlace.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(ivHealingDetail)
        }
    }

    private fun setupFavoriteButton() {
        binding.btnFavourite.isSelected = healingPlace.isBookmarked

        val userId = sharedPref.getInt("user_id", -1)
        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        if (healingPlace.isBookmarked) {
            // Disable tombol kalau sudah difavoritkan
            binding.btnFavourite.apply {
                isSelected = true
                isEnabled = false
                isClickable = false
            }
            return
        }

        binding.btnFavourite.setOnClickListener {
            apiService.addFavorite(
                userId,
                healingPlace.placeId,
                object : ApiService.ApiResponseListener {
                    override fun onSuccess(response: String) {
                        try {
                            val json = JSONObject(response)
                            val status = json.getString("status")
                            val message = json.getString("message")

                            runOnUiThread {
                                if (status == "success") {
                                    healingPlace = healingPlace.copy(isBookmarked = true)
                                    binding.btnFavourite.apply {
                                        isSelected = true
                                        isEnabled = false
                                        isClickable = false
                                    }
                                    Toast.makeText(
                                        this@ActivityDetailHealing,
                                        "Added to favorites",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (status == "fail" && message.contains("already", ignoreCase = true)) {
                                    healingPlace = healingPlace.copy(isBookmarked = true)
                                    binding.btnFavourite.apply {
                                        isSelected = true
                                        isEnabled = false
                                        isClickable = false
                                    }
                                    Toast.makeText(
                                        this@ActivityDetailHealing,
                                        "Already in favorites",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@ActivityDetailHealing,
                                        "Failed: $message",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@ActivityDetailHealing,
                                    "Parse error: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onError(error: String) {
                        runOnUiThread {
                            Toast.makeText(
                                this@ActivityDetailHealing,
                                error,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
