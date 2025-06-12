// Mendefinisikan package tempat file ini berada
package com.example.sellr

// Mengimpor antarmuka Parcelable untuk mengizinkan objek dikirim antar komponen Android
import android.os.Parcelable
// Mengimpor anotasi @Parcelize untuk membuat Parcelable secara otomatis
import kotlinx.parcelize.Parcelize

// Menandai kelas ini dengan anotasi @Parcelize agar bisa otomatis mengimplementasikan Parcelable
@Parcelize
// Data class yang merepresentasikan satu item dalam keranjang belanja
data class CartItem(
    val foodItem: FoodItem = FoodItem(), // Objek FoodItem yang dibeli, default-nya objek kosong
    var quantity: Int = 0                // Jumlah item yang ditambahkan ke keranjang, default-nya 0
) : Parcelable {                         // Mengimplementasikan Parcelable agar bisa dikirim antar Activity/Fragment

    // Konstruktor kosong eksplisit, diperlukan agar bisa di-deserialize oleh Firebase Realtime Database
    constructor() : this(FoodItem(), 0)
}
