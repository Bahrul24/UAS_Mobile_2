// Mendefinisikan package tempat file ini berada
package com.example.sellr

// Mengimpor interface Parcelable agar objek bisa dikirim antar komponen Android (seperti antar Activity)
import android.os.Parcelable

// Mengimpor anotasi Parcelize untuk membuat Parcelable otomatis tanpa harus implementasi manual
import kotlinx.parcelize.Parcelize

// Menandai kelas ini sebagai Parcelable secara otomatis menggunakan anotasi @Parcelize
@Parcelize
// Data class untuk merepresentasikan informasi satu makanan di aplikasi
data class FoodItem(
    val id: String = "",        // ID unik dari makanan, digunakan sebagai key di Firebase
    val name: String = "",      // Nama makanan, misalnya "Nasi Goreng"
    val price: Long = 0,        // Harga makanan dalam satuan rupiah (misal: 15000)
    val imageUrl: String = ""   // URL dari gambar makanan (misalnya link Firebase Storage atau URL online)
) : Parcelable                  // Mengimplementasikan Parcelable agar FoodItem bisa dikirim lewat Intent atau Bundle
