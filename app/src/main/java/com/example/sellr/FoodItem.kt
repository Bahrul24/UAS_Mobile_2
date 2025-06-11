package com.example.sellr

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Model data untuk item makanan
// Mengimplementasikan Parcelable untuk bisa dikirim antar Activity
@Parcelize
data class FoodItem(
    val id: String = "", // ID unik
    val name: String = "", // Nama makanan
    val price: Long = 0, // Harga
    val imageUrl: String = "" // URL gambar
) : Parcelable