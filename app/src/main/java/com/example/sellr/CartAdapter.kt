// Mendeklarasikan package tempat file ini berada.
package com.example.sellr

// Mengimpor kelas-kelas yang dibutuhkan untuk fungsionalitas adapter.
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Library untuk memuat gambar dari URL dengan efisien.
import com.example.sellr.databinding.ItemCartBinding // Kelas ViewBinding untuk layout item_cart.xml.
import java.text.NumberFormat
import java.util.Locale

/**
 * CartAdapter adalah kelas yang menghubungkan data item keranjang (cartList) dengan RecyclerView.
 * Tugasnya adalah membuat dan mengisi tampilan (View) untuk setiap item dalam daftar,
 * serta menangani interaksi pengguna pada item tersebut, seperti klik tombol.
 *
 * @param cartList Daftar item yang akan ditampilkan.
 * @param onIncrement Fungsi callback yang akan dipanggil saat tombol '+' diklik.
 * @param onDecrement Fungsi callback yang akan dipanggil saat tombol '-' diklik.
 */
class CartAdapter(
    private val cartList: List<CartItem>, // Sumber data utama untuk adapter.
    private val onIncrement: (CartItem) -> Unit, // Fungsi yang diterima dari Activity untuk menangani logika penambahan kuantitas.
    private val onDecrement: (CartItem) -> Unit  // Fungsi yang diterima dari Activity untuk menangani logika pengurangan kuantitas.
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() { // Mewarisi dari kelas dasar RecyclerView.Adapter.

    /**
     * ViewHolder adalah kelas yang "memegang" referensi ke setiap View (Tombol, Teks, Gambar)
     * di dalam satu baris layout item. Ini menghindari pemanggilan findViewById() yang berulang
     * dan membuat scrolling menjadi lebih efisien.
     */
    inner class CartViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Fungsi 'bind' bertugas untuk mengambil data dari satu objek CartItem
         * dan menampilkannya ke dalam komponen UI yang sesuai di dalam ViewHolder ini.
         * @param cartItem Objek data untuk item pada posisi saat ini.
         */
        fun bind(cartItem: CartItem) {
            // Mengatur teks pada TextView nama makanan.
            binding.tvFoodName.text = cartItem.foodItem.name
            // Mengatur teks pada TextView kuantitas. Angka harus diubah ke String.
            binding.tvQuantity.text = cartItem.quantity.toString()

            // Membuat formatter untuk mengubah angka menjadi format mata uang Rupiah.
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            // Mengatur teks pada TextView harga dengan nilai yang sudah diformat.
            binding.tvFoodPrice.text = formatter.format(cartItem.foodItem.price)

            // Menggunakan library Glide untuk memuat gambar dari URL ke dalam ImageView.
            // 'itemView.context' adalah konteks dari item view ini.
            // 'load()' berisi URL gambar.
            // 'into()' adalah target ImageView tempat gambar akan ditampilkan.
            Glide.with(itemView.context).load(cartItem.foodItem.imageUrl).into(binding.ivFoodImage)

            // Menambahkan listener pada tombol increment ('+').
            // Saat diklik, panggil fungsi callback 'onIncrement' yang didapat dari CartActivity,
            // sambil mengirimkan data 'cartItem' saat ini.
            binding.btnIncrement.setOnClickListener { onIncrement(cartItem) }

            // Menambahkan listener pada tombol decrement ('-').
            // Saat diklik, panggil fungsi callback 'onDecrement'.
            binding.btnDecrement.setOnClickListener { onDecrement(cartItem) }
        }
    }

    /**
     * Metode ini dipanggil oleh RecyclerView ketika perlu membuat ViewHolder baru.
     * Ini hanya terjadi saat tidak ada ViewHolder yang bisa didaur ulang.
     * Tugasnya adalah membuat "kerangka" atau "cetakan" untuk satu baris.
     *
     * @param parent ViewGroup tempat ViewHolder baru akan ditambahkan (dalam hal ini, RecyclerView itu sendiri).
     * @param viewType Tipe view, berguna jika ada beberapa jenis layout dalam satu RecyclerView.
     * @return Sebuah instance CartViewHolder yang baru.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        // "Menggembungkan" (inflate) layout XML item_cart.xml menjadi sebuah objek ViewBinding.
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Membuat dan mengembalikan ViewHolder baru dengan binding yang baru saja dibuat.
        return CartViewHolder(binding)
    }

    /**
     * Metode ini dipanggil oleh RecyclerView untuk menampilkan data pada posisi tertentu.
     * Ia mengambil ViewHolder yang sudah ada dan mengisinya dengan data dari 'cartList'
     * pada 'position' yang sesuai.
     *
     * @param holder ViewHolder yang akan diisi datanya.
     * @param position Posisi item dalam daftar data.
     */
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        // Panggil fungsi 'bind' dari ViewHolder untuk mengisi UI dengan data dari item
        // pada posisi 'position' di dalam 'cartList'.
        holder.bind(cartList[position])
    }

    /**
     * Metode ini mengembalikan jumlah total item dalam dataset.
     * RecyclerView menggunakan ini untuk mengetahui seberapa banyak item yang harus ditampilkan.
     *
     * @return Jumlah item dalam cartList.
     */
    override fun getItemCount(): Int = cartList.size // Sintaks singkat untuk mengembalikan ukuran dari list.
}