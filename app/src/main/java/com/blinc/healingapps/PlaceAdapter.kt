package com.blinc.healingapps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blinc.healingapps.databinding.ItemHealingCardBinding
import com.squareup.picasso.Picasso

class PlaceAdapter(
    private val places: List<HealingPlace>,
    private val onItemClick: (HealingPlace) -> Unit,
    private val onReadMoreClick: (HealingPlace) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    inner class PlaceViewHolder(private val binding: ItemHealingCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(place: HealingPlace) {
            binding.apply {
                tvHealingName.text = place.placeName
                tvHealingDescription.text = place.briefInfo
                tvHealingCategory.text = place.placeCategory.displayName

                Picasso.get()
                    .load(place.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(ivHealingImage)

                btnReadMore.setOnClickListener {
                    onReadMoreClick(place)
                }

                root.setOnClickListener {
                    onItemClick(place)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemHealingCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(places[position])
    }

    override fun getItemCount(): Int = places.size
}

fun HealingPlace.isInCategory(category: PlaceCategory): Boolean {
    return this.placeCategory == category
}

fun List<HealingPlace>.filterByCategory(category: PlaceCategory): List<HealingPlace> {
    return this.filter { it.isInCategory(category) }
}

fun List<HealingPlace>.getFavorites(): List<HealingPlace> {
    return this.filter { it.isBookmarked }
}