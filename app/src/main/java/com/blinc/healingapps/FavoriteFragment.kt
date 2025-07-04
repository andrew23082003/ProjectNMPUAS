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
import androidx.recyclerview.widget.GridLayoutManager
import com.blinc.healingapps.databinding.FragmentFavoriteBinding
import org.json.JSONObject

class FavoriteFragment : Fragment() {
    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private lateinit var sharedPref: SharedPreferences
    private lateinit var adapter: FavoriteAdapter
    private val favoritePlaces = mutableListOf<HealingPlace>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = ApiService(requireContext())
        sharedPref = requireContext().getSharedPreferences("user_prefs", 0)

        setupRecyclerView()
        loadFavorites()
    }

    private fun setupRecyclerView() {
        adapter = FavoriteAdapter(
            favorites = favoritePlaces,
            onItemClick = { place ->
                navigateToDetail(place)
            },
            onRemoveFavorite = { place ->
                removeFavorite(place)
            }
        )

        binding.rvFavourite.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@FavoriteFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun loadFavorites() {
        val userId = sharedPref.getInt("user_id", -1)
        if (userId == -1) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        apiService.getUserFavorites(userId, object : ApiService.ApiResponseListener {
            override fun onSuccess(response: String) {
                activity?.runOnUiThread {
                    try {
                        val json = JSONObject(response)
                        if (json.getString("status") == "success") {
                            favoritePlaces.clear()
                            val favoritesArray = json.getJSONArray("data")

                            for (i in 0 until favoritesArray.length()) {
                                val place = favoritesArray.getJSONObject(i)
                                favoritePlaces.add(
                                    PlaceBuilder.createPlace(
                                        id = place.getInt("id"),
                                        name = place.getString("name"),
                                        category = place.getString("category_name"),
                                        imageUrl = place.getString("image_url"),
                                        brief = place.getString("short_description"),
                                        detailed = place.getString("full_description"),
                                        bookmarked = true
                                    )
                                )
                            }
                            adapter.notifyDataSetChanged()

                            if (favoritePlaces.isEmpty()) {
                                Toast.makeText(
                                    requireContext(),
                                    "No favorites yet",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FavoriteFragment", "Error parsing favorites: ${e.message}")
                        Toast.makeText(
                            requireContext(),
                            "Failed to load favorites",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onError(error: String) {
                activity?.runOnUiThread {
                    Log.e("FavoriteFragment", "Load favorites error: $error")
                    Toast.makeText(
                        requireContext(),
                        "Failed to load favorites: $error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun removeFavorite(place: HealingPlace) {
        val userId = sharedPref.getInt("user_id", -1)
        if (userId == -1) return

        binding.progressBar.visibility = View.VISIBLE

        apiService.removeFavorite(
            userId,
            place.placeId,
            object : ApiService.ApiResponseListener {
                override fun onSuccess(response: String) {
                    activity?.runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        try {
                            val json = JSONObject(response)
                            if (json.getString("status") == "success") {
                                val index = favoritePlaces.indexOfFirst { it.placeId == place.placeId }
                                if (index != -1) {
                                    favoritePlaces.removeAt(index)
                                    adapter.notifyItemRemoved(index)

                                    Toast.makeText(
                                        requireContext(),
                                        "Removed from favorites",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    if (favoritePlaces.isEmpty()) {
                                        parentFragmentManager.popBackStack()
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    json.optString("message", "Failed to remove favorite"),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Log.e("FavoriteFragment", "Error parsing response", e)
                            Toast.makeText(
                                requireContext(),
                                "Error: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onError(error: String) {
                    activity?.runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        Log.e("FavoriteFragment", "Remove favorite error: $error")

                        try {
                            val errorJson = JSONObject(error)
                            Toast.makeText(
                                requireContext(),
                                errorJson.optString("message", "Failed to remove favorite"),
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Error: $error",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            })
    }

    private fun navigateToDetail(place: HealingPlace) {
        val detailIntent = Intent(requireContext(), ActivityDetailHealing::class.java).apply {
            putExtra("EXTRA_HEALING_PLACE", place)
            putExtra("EXTRA_IS_FAVORITE_VIEW", true)
        }
        startActivity(detailIntent)
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = FavoriteFragment()
    }
}