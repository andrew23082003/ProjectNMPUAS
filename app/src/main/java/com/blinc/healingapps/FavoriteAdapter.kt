package com.blinc.healingapps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blinc.healingapps.databinding.ItemFavoriteCardBinding
import com.squareup.picasso.Picasso

class FavoriteAdapter(
    private val favorites: List<HealingPlace>,
    private val onItemClick: (HealingPlace) -> Unit,
    private val onRemoveFavorite: (HealingPlace) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    inner class FavoriteViewHolder(private val binding: ItemFavoriteCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(place: HealingPlace) {
            binding.apply {
                tvFavoriteName.text = place.placeName
                tvFavoriteCategory.text = place.placeCategory.displayName
                tvFavoriteShortDesc.text = place.briefInfo

                Picasso.get()
                    .load(place.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(ivFavoriteImage)

                ivFavoriteIcon.setImageResource(R.drawable.ic_love_filled)
                ivFavoriteIcon.setOnClickListener { onRemoveFavorite(place) }

                root.setOnClickListener { onItemClick(place) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favorites[position])
    }

    override fun getItemCount(): Int = favorites.size
}