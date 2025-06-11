package com.example.sellr

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sellr.databinding.ItemCartBinding
import java.text.NumberFormat
import java.util.Locale

// Adapter untuk menampilkan item keranjang di RecyclerView
class CartAdapter(
    private val cartList: List<CartItem>, // Data list
    private val onIncrement: (CartItem) -> Unit, // Callback untuk tombol +
    private val onDecrement: (CartItem) -> Unit // Callback untuk tombol -
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // ViewHolder pattern
    inner class CartViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {
        // Bind data ke view
        fun bind(cartItem: CartItem) {
            binding.tvFoodName.text = cartItem.foodItem.name
            binding.tvQuantity.text = cartItem.quantity.toString()

            // Format harga ke mata uang Indonesia
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            binding.tvFoodPrice.text = formatter.format(cartItem.foodItem.price)

            // Load gambar dengan Glide
            Glide.with(itemView.context)
                .load(cartItem.foodItem.imageUrl)
                .into(binding.ivFoodImage)

            // Set listener untuk tombol
            binding.btnIncrement.setOnClickListener { onIncrement(cartItem) }
            binding.btnDecrement.setOnClickListener { onDecrement(cartItem) }
        }
    }

    // Buat ViewHolder baru
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    // Bind data ke ViewHolder pada posisi tertentu
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartList[position])
    }

    // Jumlah total item
    override fun getItemCount(): Int = cartList.size
}