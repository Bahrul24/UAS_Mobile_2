// Mendeklarasikan package tempat file ini berada.
package com.example.sellr

// Mengimpor kelas-kelas yang dibutuhkan untuk fungsionalitas adapter.
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Library untuk memuat gambar dari URL.
import com.example.sellr.databinding.ItemFoodBinding // Kelas ViewBinding untuk layout item_food.xml.
import java.text.NumberFormat
import java.util.Locale

/**
 * FoodAdapter adalah kelas yang menghubungkan data daftar makanan (foodList) dengan RecyclerView.
 * Tujuannya adalah untuk menampilkan setiap item makanan yang tersedia untuk dibeli.
 *
 * @param foodList Daftar item makanan yang akan ditampilkan.
 * @param onAddToCartClicked Sebuah fungsi callback yang akan dipanggil ketika tombol "Tambah ke Keranjang" diklik.
 */
class FoodAdapter(
    private val foodList: List<FoodItem>,
    private val onAddToCartClicked: (FoodItem) -> Unit // Callback untuk menangani aksi klik.
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() { // Mewarisi dari kelas dasar RecyclerView.Adapter.

    /**
     * ViewHolder untuk item makanan. Kelas ini "memegang" referensi ke komponen UI
     * dalam satu baris layout (item_food.xml), membuat akses dan update menjadi efisien.
     */
    inner class FoodViewHolder(val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root) {
        /**
         * Fungsi 'bind' ini mengambil data dari satu objek FoodItem dan menampilkannya
         * ke dalam komponen-komponen UI yang sesuai.
         * @param foodItem Objek data untuk item pada posisi saat ini.
         */
        fun bind(foodItem: FoodItem) {
            // Mengatur teks pada TextView nama makanan.
            binding.tvFoodName.text = foodItem.name
            // Membuat formatter untuk mengubah angka menjadi format mata uang Rupiah.
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            // Mengatur teks harga dengan nilai yang sudah diformat.
            binding.tvFoodPrice.text = formatter.format(foodItem.price)

            // Log untuk debugging, memastikan URL gambar yang benar sedang dimuat.
            Log.d("FoodAdapter", "Loading image for ${foodItem.name}: ${foodItem.imageUrl}")

            // Menggunakan library Glide untuk memuat gambar dari URL.
            Glide.with(itemView.context) // Memulai Glide dengan konteks dari item view.
                .load(foodItem.imageUrl)       // Menentukan sumber gambar dari URL.
                .placeholder(R.drawable.ic_placeholder_image) // Menampilkan gambar ini saat gambar asli sedang diunduh.
                .error(R.drawable.ic_error_image)       // Menampilkan gambar ini jika terjadi error saat mengunduh (misal: URL salah, tidak ada internet).
                .into(binding.ivFoodImage)     // Menentukan ImageView target tempat gambar akan ditampilkan.

            // Menambahkan listener pada tombol "Tambah ke Keranjang".
            binding.btnAddToCart.setOnClickListener {
                Log.d("FoodAdapter", "Tombol Tambah diklik untuk: ${foodItem.name}")
                // Saat tombol diklik, panggil fungsi callback 'onAddToCartClicked' yang telah
                // diteruskan dari Activity, sambil mengirimkan objek 'foodItem' yang diklik.
                onAddToCartClicked(foodItem)
            }
        }
    }

    /**
     * Metode ini dipanggil oleh RecyclerView untuk membuat ViewHolder baru saat dibutuhkan.
     * Ini hanya membuat "kerangka" atau "cetakan" kosong untuk satu baris.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        // "Menggembungkan" (inflate) layout XML item_food.xml menjadi sebuah objek ViewBinding.
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Membuat dan mengembalikan ViewHolder baru dengan binding yang baru saja dibuat.
        return FoodViewHolder(binding)
    }

    /**
     * Metode ini dipanggil oleh RecyclerView untuk menampilkan data pada posisi tertentu.
     * Ia mengambil ViewHolder yang sudah ada dan mengisinya dengan data dari 'foodList'.
     */
    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        // Panggil fungsi 'bind' dari ViewHolder untuk mengisi UI dengan data dari item
        // pada posisi 'position' di dalam 'foodList'.
        holder.bind(foodList[position])
    }

    /**
     * Metode ini mengembalikan jumlah total item dalam dataset (daftar makanan).
     */
    override fun getItemCount(): Int = foodList.size
}