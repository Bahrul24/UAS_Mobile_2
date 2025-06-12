// Package tempat file ini berada
package com.example.sellr

// Import untuk logging, digunakan untuk mencatat informasi/debug di Logcat
import android.util.Log
// Import untuk membuat tampilan layout XML menjadi View dalam kode
import android.view.LayoutInflater
import android.view.ViewGroup
// Import RecyclerView
import androidx.recyclerview.widget.RecyclerView
// Import Glide untuk memuat gambar dari URL
import com.bumptech.glide.Glide
// Import view binding khusus untuk layout item makanan (item_food.xml)
import com.example.sellr.databinding.ItemFoodBinding
// Import formatter untuk menampilkan harga dalam format rupiah
import java.text.NumberFormat
import java.util.Locale

// Kelas adapter untuk menampilkan daftar makanan di RecyclerView
class FoodAdapter(
    private val foodList: List<FoodItem>, // List data makanan yang akan ditampilkan
    private val onAddToCartClicked: (FoodItem) -> Unit // Fungsi callback saat tombol tambah ditekan
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    // ViewHolder untuk menampung view dari setiap item makanan
    inner class FoodViewHolder(val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root) {

        // Fungsi untuk mengisi data (bind) ke elemen UI pada item
        fun bind(foodItem: FoodItem) {
            // Set teks nama makanan ke TextView
            binding.tvFoodName.text = foodItem.name

            // Format harga menjadi format rupiah (misal Rp15.000)
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            binding.tvFoodPrice.text = formatter.format(foodItem.price)

            // Debug log: tampilkan info nama dan URL gambar di Logcat
            Log.d("FoodAdapter", "Loading image for ${foodItem.name}: ${foodItem.imageUrl}")

            // Gunakan Glide untuk memuat gambar dari URL ke ImageView
            Glide.with(itemView.context)
                .load(foodItem.imageUrl) // URL gambar makanan
                .placeholder(R.drawable.ic_placeholder_image) // Gambar default saat loading
                .error(R.drawable.ic_error_image) // Gambar jika gagal dimuat
                .into(binding.ivFoodImage) // Target ImageView

            // Saat tombol "Tambah ke Keranjang" ditekan
            binding.btnAddToCart.setOnClickListener {
                // Debug log: cetak nama makanan yang diklik
                Log.d("FoodAdapter", "Tombol Tambah diklik untuk: ${foodItem.name}")
                // Jalankan fungsi callback dengan data makanan yang diklik
                onAddToCartClicked(foodItem)
            }
        }
    }

    // Fungsi yang dijalankan saat ViewHolder dibuat untuk pertama kali
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        // Inflate layout item_food.xml menjadi objek binding
        val binding = ItemFoodBinding.inflate(
            LayoutInflater.from(parent.context), // Ambil context dari parent
            parent, // Parent ViewGroup
            false // Jangan langsung attach ke parent
        )
        // Kembalikan ViewHolder baru dengan binding tersebut
        return FoodViewHolder(binding)
    }

    // Fungsi yang menghubungkan data dengan tampilan pada posisi tertentu
    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        // Ambil data dari list berdasarkan posisi, dan bind ke holder
        holder.bind(foodList[position])
    }

    // Fungsi untuk menentukan jumlah total item dalam RecyclerView
    override fun getItemCount(): Int = foodList.size // Ukuran list makanan
}
