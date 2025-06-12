// Menentukan bahwa file ini berada dalam package com.example.sellr
package com.example.sellr

// Mendefinisikan data class Order yang merepresentasikan satu transaksi/pesanan
data class Order(
    val orderId: String = "",                // ID unik pesanan (dihasilkan otomatis oleh Firebase)
    val items: List<CartItem> = listOf(),    // Daftar item yang dibeli, berisi objek CartItem
    val totalPrice: Long = 0,                // Total harga seluruh item dalam pesanan (dalam satuan rupiah)
    val timestamp: Long = 0,                 // Waktu pemesanan (format: UNIX timestamp dalam milidetik)
    val userId: String = ""                  // ID pengguna yang membuat pesanan (UID dari Firebase Authentication)
) {
    // Firebase Realtime Database membutuhkan constructor kosong tanpa parameter
    constructor() : this("", listOf(), 0, 0, "")
    // Constructor ini digunakan saat Firebase membaca data dan memetakan ke objek Order secara otomatis
}
