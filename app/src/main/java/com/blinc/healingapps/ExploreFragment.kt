package com.blinc.healingapps

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.blinc.healingapps.databinding.FragmentExploreBinding
import org.json.JSONObject

class ExploreFragment : Fragment() {
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = ApiService(requireContext())
        sharedPref = requireContext().getSharedPreferences("user_prefs", 0)

        setupRecyclerView()
        setupFabButton()
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val userId = sharedPref.getInt("user_id", -1)
        val usedUserId = if (userId != -1) userId else null

        Log.d("ExploreFragment", "Calling getPlaces with userId=$usedUserId")

        apiService.getPlaces(usedUserId, null, object : ApiService.ApiResponseListener {
            override fun onSuccess(response: String) {
                Log.d("ExploreFragment", "API Success Response: $response")

                activity?.runOnUiThread {
                    try {
                        val json = JSONObject(response)
                        if (json.optBoolean("success", false))
                        {
                            val placesArray = json.getJSONArray("data")
                            val healingPlaces = mutableListOf<HealingPlace>()

                            for (i in 0 until placesArray.length()) {
                                val place = placesArray.getJSONObject(i)
                                healingPlaces.add(
                                    PlaceBuilder.createPlace(
                                        id = place.getInt("id"),
                                        name = place.getString("name"),
                                        category = place.getString("category_name"),
                                        imageUrl = place.getString("image_url"),
                                        brief = place.getString("short_desc"),
                                        detailed = place.getString("full_desc"),
                                        bookmarked = false
                                    )
                                )
                            }

                            Log.d("ExploreFragment", "Loaded ${healingPlaces.size} places")

                            binding.rvHealing.apply {
                                layoutManager = LinearLayoutManager(requireContext())
                                adapter = PlaceAdapter(
                                    places = healingPlaces,
                                    onItemClick = { place -> navigateToDetail(place) },
                                    onReadMoreClick = { place -> navigateToDetail(place) }
                                )
                            }
                        } else {
                            Log.e("ExploreFragment", "API returned result != success")
                            Toast.makeText(
                                requireContext(),
                                "Gagal memuat lokasi",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Log.e("ExploreFragment", "Exception parsing response: ${e.message}")
                        Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onError(error: String) {
                Log.e("ExploreFragment", "API Error: $error")
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Gagal memuat data: $error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setupFabButton() {
        binding.fabAddLocation.setOnClickListener {
            val intent = Intent(requireContext(), ActivityNewLocation::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToDetail(place: HealingPlace) {
        val detailIntent = Intent(requireContext(), ActivityDetailHealing::class.java).apply {
            putExtra("EXTRA_HEALING_PLACE", place)
            putExtra("EXTRA_IS_FAVORITE_VIEW", false)
        }
        startActivity(detailIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ExploreFragment()
    }
}
