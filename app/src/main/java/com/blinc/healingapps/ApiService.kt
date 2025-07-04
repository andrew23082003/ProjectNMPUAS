package com.blinc.healingapps

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject

class ApiService(private val context: Context) {
    private val volleyHelper = VolleyHelper.getInstance(context)

    interface ApiResponseListener {
        fun onSuccess(response: String)
        fun onError(error: String)
    }

    fun login(email: String, password: String, listener: ApiResponseListener) {
        val url = volleyHelper.getAbsoluteUrl("login.php")
        val jsonBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                listener.onSuccess(response.toString())
            },
            { error ->
                listener.onError(error.message ?: "Login failed")
            }
        )

        volleyHelper.addToRequestQueue(request)
    }

    fun signup(name: String, email: String, password: String, listener: ApiResponseListener) {
        val url = volleyHelper.getAbsoluteUrl("signup.php")
        val jsonBody = JSONObject().apply {
            put("name", name)
            put("email", email)
            put("password", password)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                listener.onSuccess(response.toString())
            },
            { error ->
                listener.onError(error.message ?: "Signup failed")
            }
        )

        volleyHelper.addToRequestQueue(request)
    }

    fun changePassword(userId: Int, oldPassword: String, newPassword: String, repeatPassword: String, listener: ApiResponseListener) {
        val url = volleyHelper.getAbsoluteUrl("change_pass.php")
        val jsonBody = JSONObject().apply {
            put("user_id", userId)
            put("old_password", oldPassword)
            put("new_password", newPassword)
            put("repeat_password", repeatPassword)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                listener.onSuccess(response.toString())
            },
            { error ->
                listener.onError(error.message ?: "Change password failed")
            }
        )

        volleyHelper.addToRequestQueue(request)
    }

    fun getCategories(listener: ApiResponseListener) {
        val url = volleyHelper.getAbsoluteUrl("get_categories.php")

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                listener.onSuccess(response)
            },
            { error ->
                listener.onError(error.message ?: "Failed to get categories")
            }
        )

        volleyHelper.addToRequestQueue(request)
    }

    fun getProfile(userId: Int, listener: ApiResponseListener) {
        val url = "${volleyHelper.getAbsoluteUrl("get_profile.php")}?user_id=$userId"

        Log.d("ApiService", "GET Profile URL: $url") // debug

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                Log.d("ApiService", "Profile response: $response") // debug
                listener.onSuccess(response)
            },
            { error ->
                val errorMsg = error.networkResponse?.let {
                    "Status Code: ${it.statusCode}, Body: ${String(it.data, Charsets.UTF_8)}"
                } ?: error.message ?: "Unknown error"
                Log.e("ApiService", "Profile Error: $errorMsg")
                listener.onError(errorMsg)
            }
        )

        volleyHelper.addToRequestQueue(request)
    }

    fun getPlaces(userId: Int? = null, categoryId: Int? = null, listener: ApiResponseListener) {
        var url = volleyHelper.getAbsoluteUrl("get_location.php")

        if (userId != null || categoryId != null) {
            url += "?"

            if (userId != null) {
                url += "user_id=$userId"
            }

            if (categoryId != null) {
                if (userId != null) {
                    url += "&"
                }
                url += "category_id=$categoryId"
            }
        }

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                listener.onSuccess(response)
            },
            { error ->
                listener.onError(error.message ?: "Failed to get places")
            }
        )

        volleyHelper.addToRequestQueue(request)
    }

    fun getUserFavorites(userId: Int, listener: ApiResponseListener) {
        val url = "${volleyHelper.getAbsoluteUrl("get_fav.php")}?user_id=$userId"

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                listener.onSuccess(response)
            },
            { error ->
                listener.onError(error.message ?: "Failed to get favorites")
            }
        )

        volleyHelper.addToRequestQueue(request)
    }

    fun addFavorite(userId: Int, locationId: Int, listener: ApiResponseListener) {
        val url = volleyHelper.getAbsoluteUrl("add_fav.php")
        val jsonBody = JSONObject().apply {
            put("user_id", userId)
            put("location_id", locationId)
        }

        Log.d("ApiService", "Add Favorite URL: $url")
        Log.d("ApiService", "Add Favorite Data: ${jsonBody.toString()}")

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                Log.d("ApiService", "Add Favorite Response: ${response.toString()}")
                listener.onSuccess(response.toString())
            },
            { error ->
                val errorMsg = error.networkResponse?.let {
                    "Status ${it.statusCode}: ${String(it.data, Charsets.UTF_8)}"
                } ?: error.message ?: "Unknown error"
                Log.e("ApiService", "Add Favorite Error: $errorMsg")
                listener.onError(errorMsg)
            }
        ).apply {
            setShouldCache(false)
        }

        volleyHelper.addToRequestQueue(request)
    }

    fun removeFavorite(userId: Int, locationId: Int, listener: ApiResponseListener) {
        val url = "${volleyHelper.getAbsoluteUrl("remove_fav.php")}?user_id=$userId&location_id=$locationId"

        Log.d("ApiService", "DELETE Request to: $url")

        val request = object : StringRequest(
            Method.DELETE, url,
            { response ->
                Log.d("ApiService", "DELETE Response: $response")
                listener.onSuccess(response)
            },
            { error ->
                val errorMsg = error.networkResponse?.let {
                    "Status Code: ${it.statusCode}, Body: ${String(it.data, Charsets.UTF_8)}"
                } ?: error.message ?: "Unknown error"
                Log.e("ApiService", "DELETE Error: $errorMsg")
                listener.onError(errorMsg)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Accept"] = "application/json"
                return headers
            }
        }

        volleyHelper.addToRequestQueue(request)
    }

    fun addPlace(
        locationData: String,
        listener: ApiResponseListener
    ) {
        val url = volleyHelper.getAbsoluteUrl("add_location.php")
        val jsonObject = try {
            JSONObject(locationData)
        } catch (e: Exception) {
            listener.onError("Invalid location data format")
            return
        }

        Log.d("ApiService", "Add Place URL: $url")
        Log.d("ApiService", "Add Place Data: $jsonObject")

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                Log.d("ApiService", "Add Place Response: ${response.toString()}")
                listener.onSuccess(response.toString())
            },
            { error ->
                val errorMsg = error.networkResponse?.let {
                    val errorBody = String(it.data, Charsets.UTF_8)
                    Log.e("ApiService", "Error Response Body: $errorBody")
                    Log.e("ApiService", "Error Response Code: ${it.statusCode}")
                    errorBody
                } ?: error.message ?: "Unknown error"
                Log.e("ApiService", "Add Place Error: $errorMsg")
                listener.onError(errorMsg)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Accept"] = "application/json"
                return headers
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
        }

        volleyHelper.addToRequestQueue(request)
    }
}