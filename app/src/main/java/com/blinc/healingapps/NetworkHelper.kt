package com.blinc.healingapps

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class NetworkHelper(context: Context) {
    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)
    private val baseUrl = "https://ubaya.xyz/native/160722022/"

    fun makeRequest(
        endpoint: String,
        method: Int,
        params: JSONObject?,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = JsonObjectRequest(
            method,
            baseUrl + endpoint,
            params,
            { response -> onSuccess(response) },
            { error -> onError(error.message ?: "Unknown error") }
        )
        requestQueue.add(request)
    }
}