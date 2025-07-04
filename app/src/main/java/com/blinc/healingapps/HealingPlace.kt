package com.blinc.healingapps

import android.os.Parcel
import android.os.Parcelable

sealed class PlaceCategory(val displayName: String) {
    object Cafe : PlaceCategory("Cafe")
    object Restaurant : PlaceCategory("Restaurant")
    object Hotel : PlaceCategory("Hotel")
    object Karaoke : PlaceCategory("Karaoke")
    object Arcade : PlaceCategory("Arcade")
    object Playground : PlaceCategory("Playground")
    object Billiard : PlaceCategory("Billiard")
    object Bowling : PlaceCategory("Bowling")
    object Bar : PlaceCategory("Bar")
    object Warkop : PlaceCategory("Warkop")

    companion object {
        fun fromString(categoryName: String): PlaceCategory {
            return when (categoryName.lowercase()) {
                "cafe" -> Cafe
                "restaurant" -> Restaurant
                "hotel" -> Hotel
                "karaoke" -> Karaoke
                "arcade" -> Arcade
                "playground" -> Playground
                "billiard" -> Billiard
                "bowling" -> Bowling
                "bar" -> Bar
                "warkop" -> Warkop
                else -> Cafe
            }
        }

        fun getAllCategories(): List<PlaceCategory> {
            return listOf(Cafe, Restaurant, Hotel, Karaoke, Arcade, Playground, Billiard, Bowling, Bar, Warkop)
        }

        fun getCategoryNames(): Array<String> {
            return getAllCategories().map { it.displayName }.toTypedArray()
        }
    }
}

data class HealingPlace(
    val placeId: Int,
    val placeName: String,
    val placeCategory: PlaceCategory,
    val imageUrl: String,
    val briefInfo: String,
    val detailedInfo: String,
    var isBookmarked: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        PlaceCategory.fromString(parcel.readString() ?: "cafe"),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(placeId)
        parcel.writeString(placeName)
        parcel.writeString(placeCategory.displayName) // simpan nama kategori sebagai string
        parcel.writeString(imageUrl)
        parcel.writeString(briefInfo)
        parcel.writeString(detailedInfo)
        parcel.writeByte(if (isBookmarked) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<HealingPlace> {
        override fun createFromParcel(parcel: Parcel): HealingPlace {
            return HealingPlace(parcel)
        }

        override fun newArray(size: Int): Array<HealingPlace?> {
            return arrayOfNulls(size)
        }
    }
}

class PlaceBuilder {
    companion object {
        fun createPlace(
            id: Int,
            name: String,
            category: String,
            imageUrl: String,
            brief: String,
            detailed: String,
            bookmarked: Boolean = false
        ): HealingPlace {
            return HealingPlace(
                placeId = id,
                placeName = name,
                placeCategory = PlaceCategory.fromString(category),
                imageUrl = imageUrl,
                briefInfo = brief,
                detailedInfo = detailed,
                isBookmarked = bookmarked
            )
        }
    }
}
