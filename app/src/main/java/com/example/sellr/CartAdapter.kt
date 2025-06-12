package com.example.sellr

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sellr.databinding.ItemCartBinding
import java.text.NumberFormat
import java.util.Locale

// Adapter untuk menampilkan daftar item dalam keranjang belanja di RecyclerView
class CartAdapter(
    private val cartList: List<CartItem>, // Daftar item yang akan ditampilkan
    private val onIncrement: (CartItem) -> Unit, // Callback saat tombol "+" ditekan
    private val onDecrement: (CartItem) -> Unit  // Callback saat tombol "-" ditekan
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // ViewHolder berisi binding untuk setiap item keranjang
    inner class CartViewHolder(val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Menghubungkan data CartItem ke UI
        fun bind(cartItem: CartItem) {
            binding.tvFoodName.text = cartItem.foodItem.name
            binding.tvQuantity.text = cartItem.quantity.toString()

            // Format harga dalam mata uang Indonesia (Rupiah)
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            binding.tvFoodPrice.text = formatter.format(cartItem.foodItem.price)

            // Menampilkan gambar makanan menggunakan Glide
            Glide.with(itemView.context)
                .load(cartItem.foodItem.imageUrl)
                .into(binding.ivFoodImage)

            // Set aksi tombol tambah dan kurang
            binding.btnIncrement.setOnClickListener { onIncrement(cartItem) }
            binding.btnDecrement.setOnClickListener { onDecrement(cartItem) }
        }
    }

    // Membuat tampilan baru (inflate layout item_cart.xml)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    // Menghubungkan data ke ViewHolder berdasarkan posisi
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartList[position])
    }

    // Mengembalikan jumlah total item dalam RecyclerView
    override fun getItemCount(): Int = cartList.size
}
