package com.blinc.healingapps

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blinc.healingapps.databinding.ItemHealingCardBinding
import com.squareup.picasso.Picasso

class ExploreFragmentAdapter(
    private val context: Context,
    private val places: List<HealingPlace>,
    private val onItemClick: (HealingPlace) -> Unit,
    private val onReadMoreClick: (HealingPlace) -> Unit
) : RecyclerView.Adapter<ExploreFragmentAdapter.ExploreViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreViewHolder {
        val binding = ItemHealingCardBinding.inflate(LayoutInflater.from(context), parent, false)
        return ExploreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExploreViewHolder, position: Int) {
        holder.bind(places[position])
    }

    override fun getItemCount(): Int = places.size

    inner class ExploreViewHolder(private val binding: ItemHealingCardBinding) :
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
}