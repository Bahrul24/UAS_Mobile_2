// Mendeklarasikan package tempat file ini berada.
package com.example.sellr

// Mengimpor interface Parcelable dari library Android.
import android.os.Parcelable
// Mengimpor anotasi Parcelize dari library Kotlin, untuk otomatisasi implementasi Parcelable.
import kotlinx.parcelize.Parcelize

/**
 * Anotasi ini memberitahu compiler Kotlin untuk secara otomatis menghasilkan
 * metode-metode yang dibutuhkan oleh interface Parcelable (seperti writeToParcel dan createFromParcel).
 * Ini sangat menyederhanakan proses membuat sebuah kelas menjadi Parcelable.
 */
@Parcelize
/**
 * Mendefinisikan sebuah 'data class' bernama CartItem.
 * 'data class' adalah kelas khusus di Kotlin yang ideal untuk menyimpan data.
 * Ia secara otomatis menghasilkan fungsi-fungsi berguna seperti .equals(), .hashCode(), .toString(), dan .copy().
 * Kelas ini juga mengimplementasikan interface Parcelable.
 */
data class CartItem(
    // Properti pertama, menyimpan seluruh objek FoodItem, bukan hanya ID-nya.
    // Ini adalah 'denormalisasi' data yang umum di Firebase untuk mempermudah pengambilan data.
    // Diberi nilai default agar bisa bekerja dengan constructor kosong.
    val foodItem: FoodItem = FoodItem(),

    // Properti kedua, menyimpan jumlah item ini yang dibeli.
    // Menggunakan 'var' karena kuantitas bisa berubah (ditambah atau dikurangi).
    // Diberi nilai default 0.
    var quantity: Int = 0
) : Parcelable { // Menandakan bahwa kelas ini mengimplementasikan Parcelable.

    // Ini adalah constructor sekunder tanpa argumen (constructor kosong).
    // Firebase membutuhkannya untuk proses deserialization (mengubah data dari database menjadi objek Kotlin).
    // Saat Firebase membuat objek CartItem, ia akan memanggil constructor kosong ini terlebih dahulu,
    // lalu mengisi properti 'foodItem' dan 'quantity' dengan data dari database.
    // ': this(...)' berarti constructor ini mendelegasikan pembuatannya ke constructor utama di atas,
    // dengan memberikan nilai default.
    constructor() : this(FoodItem(), 0)
}