// Mendefinisikan package tempat file ini berada
package com.example.sellr

// Import untuk mengatur tampilan layout XML ke dalam view di Kotlin
import android.view.LayoutInflater
// Import untuk mengatur grup view (seperti RecyclerView)
import android.view.ViewGroup
// Import untuk membuat adapter RecyclerView
import androidx.recyclerview.widget.RecyclerView
// Import library Glide untuk memuat dan menampilkan gambar dari URL
import com.bumptech.glide.Glide
// Import binding ke layout item_cart.xml
import com.example.sellr.databinding.ItemCartBinding
// Import untuk memformat angka sebagai mata uang
import java.text.NumberFormat
// Import untuk menentukan format lokal Indonesia
import java.util.Locale

// Kelas adapter untuk menampilkan daftar item keranjang di RecyclerView
class CartAdapter(
    private val cartList: List<CartItem>,             // Daftar item CartItem yang akan ditampilkan di RecyclerView
    private val onIncrement: (CartItem) -> Unit,       // Fungsi callback saat tombol "+" ditekan
    private val onDecrement: (CartItem) -> Unit        // Fungsi callback saat tombol "-" ditekan
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() { // Meng-extend RecyclerView.Adapter dan mendefinisikan ViewHolder-nya

    // Kelas ViewHolder untuk menyimpan dan mengelola tampilan satu item dalam RecyclerView
    inner class CartViewHolder(val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) { // binding.root adalah root view dari item_cart.xml

        // Fungsi untuk mengisi data dari CartItem ke dalam elemen UI
        fun bind(cartItem: CartItem) {
            // Menampilkan nama makanan ke TextView
            binding.tvFoodName.text = cartItem.foodItem.name
            // Menampilkan jumlah item ke TextView
            binding.tvQuantity.text = cartItem.quantity.toString()

            // Membuat formatter untuk menampilkan harga dalam format mata uang Indonesia
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            // Menampilkan harga makanan yang telah diformat ke TextView
            binding.tvFoodPrice.text = formatter.format(cartItem.foodItem.price)

            // Menggunakan Glide untuk memuat gambar makanan dari URL ke ImageView
            Glide.with(itemView.context)
                .load(cartItem.foodItem.imageUrl) // URL gambar makanan
                .into(binding.ivFoodImage)        // Tujuan ImageView untuk menampilkan gambar

            // Menambahkan aksi saat tombol "+" ditekan, panggil fungsi onIncrement
            binding.btnIncrement.setOnClickListener { onIncrement(cartItem) }
            // Menambahkan aksi saat tombol "-" ditekan, panggil fungsi onDecrement
            binding.btnDecrement.setOnClickListener { onDecrement(cartItem) }
        }
    }

    // Fungsi yang dipanggil saat RecyclerView butuh ViewHolder baru
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        // Inflate layout XML item_cart.xml dan ubah menjadi objek binding
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context), // Gunakan context dari parent view group
            parent,                              // Parent dari layout yang di-inflate
            false                                // Jangan langsung attach ke parent
        )
        // Kembalikan ViewHolder baru dengan binding tersebut
        return CartViewHolder(binding)
    }

    // Fungsi untuk menghubungkan data ke tampilan pada posisi tertentu
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        // Ambil item pada posisi dan tampilkan menggunakan fungsi bind
        holder.bind(cartList[position])
    }

    // Fungsi yang memberi tahu RecyclerView berapa banyak item yang ada di daftar
    override fun getItemCount(): Int = cartList.size // Jumlah total item dalam daftar
}
