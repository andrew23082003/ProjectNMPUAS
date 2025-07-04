package com.blinc.healingapps

import android.R
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blinc.healingapps.databinding.ActivityNewLocationBinding
import org.json.JSONObject

class ActivityNewLocation : AppCompatActivity() {
    private lateinit var binding: ActivityNewLocationBinding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        apiService = ApiService(this)

        setupSpinner()
        setupClickListeners()
    }

    private fun setupSpinner() {
        apiService.getCategories(object : ApiService.ApiResponseListener {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val json = JSONObject(response)
                        if (json.getString("status") == "success") {
                            val categoriesArray = json.getJSONArray("data")
                            val categoryNames = mutableListOf<String>()

                            for (i in 0 until categoriesArray.length()) {
                                val category = categoriesArray.getJSONObject(i)
                                categoryNames.add(category.getString("display_name"))
                            }

                            val adapter = ArrayAdapter(
                                this@ActivityNewLocation,
                                R.layout.simple_spinner_item,
                                categoryNames
                            )
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            binding.spinnerCategory.adapter = adapter
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@ActivityNewLocation,
                            "Gagal memuat kategori",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Toast.makeText(
                        this@ActivityNewLocation,
                        "Gagal memuat kategori: $error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnAddLocation.setOnClickListener {
            handleAddLocation()
        }
    }

    private fun handleAddLocation() {
        val locationName = binding.etLocationName.text.toString().trim()
        val selectedCategory = binding.spinnerCategory.selectedItem.toString()
        val photoUrl = binding.etPhotoUrl.text.toString().trim()
        val shortDescription = binding.etShortDescription.text.toString().trim()
        val fullDescription = binding.etFullDescription.text.toString().trim()

        if (validateInputs(locationName, photoUrl, shortDescription, fullDescription)) {
            val categoryId = when (selectedCategory) {
                "Cafe" -> 1
                "Restaurant" -> 2
                "Hotel" -> 3
                "Karaoke" -> 4
                "Game Arcade" -> 5
                "Playground" -> 6
                "Billiard" -> 7
                "Bowling" -> 8
                "Bar" -> 9
                "Warkop" -> 10
                else -> 1
            }

            binding.btnAddLocation.isEnabled = false

            val locationData = JSONObject().apply {
                put("name", locationName)
                put("category_id", categoryId)
                put("image_url", photoUrl)
                put("short_description", shortDescription)
                put("full_description", fullDescription)
            }

            apiService.addPlace(locationData.toString(), object : ApiService.ApiResponseListener {
                override fun onSuccess(response: String) {
                    runOnUiThread {
                        binding.btnAddLocation.isEnabled = true
                        try {
                            val json = JSONObject(response)
                            if (json.getString("status") == "success") {
                                val placeData = json.getJSONObject("data")
                                Toast.makeText(
                                    this@ActivityNewLocation,
                                    "Lokasi '${placeData.getString("name")}' berhasil ditambahkan!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                setResult(RESULT_OK)
                                finish()
                            } else {
                                Toast.makeText(
                                    this@ActivityNewLocation,
                                    json.getString("message"),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@ActivityNewLocation,
                                "Gagal menambahkan lokasi",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("AddLocation", "Error: ${e.message}")
                        }
                    }
                }

                override fun onError(error: String) {
                    runOnUiThread {
                        binding.btnAddLocation.isEnabled = true
                        Toast.makeText(
                            this@ActivityNewLocation,
                            "Gagal menambahkan lokasi: $error",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("AddLocation", "API Error: $error")
                    }
                }
            })
        }
    }

    private fun validateInputs(
        name: String,
        url: String,
        shortDesc: String,
        fullDesc: String
    ): Boolean {
        return when {
            name.isEmpty() -> {
                showError("Nama lokasi tidak boleh kosong")
                false
            }
            url.isEmpty() -> {
                showError("URL foto tidak boleh kosong")
                false
            }
            shortDesc.isEmpty() -> {
                showError("Deskripsi singkat tidak boleh kosong")
                false
            }
            fullDesc.isEmpty() -> {
                showError("Deskripsi lengkap tidak boleh kosong")
                false
            }
            !isValidUrl(url) -> {
                showError("Masukkan URL yang valid (http:// atau https://)")
                false
            }
            else -> true
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}