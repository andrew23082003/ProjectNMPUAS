package com.blinc.healingapps

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.blinc.healingapps.databinding.FragmentProfileBinding
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = ApiService(requireContext())
        sharedPref = requireContext().getSharedPreferences("user_prefs", 0)

        setupProfileData()
    }

    private fun setupProfileData() {
        val userId = sharedPref.getInt("user_id", -1)
        if (userId == -1) {
            binding.etName.setText("Guest")
            binding.etEmail.setText("guest@example.com")
            binding.etJoinedDate.setText("Not logged in")
            binding.etTotalFavourites.setText("0")
            return
        }

        apiService.getProfile(userId, object : ApiService.ApiResponseListener {
            override fun onSuccess(response: String) {
                activity?.runOnUiThread {
                    try {
                        val json = JSONObject(response)

                        if (json.getString("status") != "success") {
                            Toast.makeText(
                                requireContext(),
                                json.optString("message", "Failed to load profile"),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@runOnUiThread
                        }

                        val data = json.getJSONObject("data")
                        binding.etName.setText(data.getString("name"))
                        binding.etEmail.setText(data.getString("email"))

                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val date = dateFormat.parse(data.getString("created_at"))
                        val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        binding.etJoinedDate.setText(displayFormat.format(date ?: Date()))

                        binding.etTotalFavourites.setText(data.getInt("total_favorite").toString())

                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Failed to parse profile data: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onError(error: String) {
                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load profile: $error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        setupProfileData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ProfileFragment()
    }
}
