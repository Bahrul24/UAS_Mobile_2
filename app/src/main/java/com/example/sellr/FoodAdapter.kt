package com.example.sellr

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sellr.databinding.ItemFoodBinding
import java.text.NumberFormat
import java.util.Locale

// Adapter untuk daftar makanan
class FoodAdapter(
    private val foodList: List<FoodItem>, // Data list
    private val onAddToCartClicked: (FoodItem) -> Unit // Callback klik tombol tambah
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    // ViewHolder pattern
    inner class FoodViewHolder(val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root) {
        // Bind data ke view
        fun bind(foodItem: FoodItem) {
            binding.tvFoodName.text = foodItem.name

            // Format harga ke mata uang Indonesia
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            binding.tvFoodPrice.text = formatter.format(foodItem.price)

            // Log untuk debugging gambar
            Log.d("FoodAdapter", "Loading image for ${foodItem.name}: ${foodItem.imageUrl}")

            // Load gambar dengan Glide
            Glide.with(itemView.context)
                .load(foodItem.imageUrl)
                .placeholder(R.drawable.ic_placeholder_image) // Gambar sementara
                .error(R.drawable.ic_error_image) // Gambar error
                .into(binding.ivFoodImage)

            // Set listener untuk tombol tambah
            binding.btnAddToCart.setOnClickListener {
                Log.d("FoodAdapter", "Tombol Tambah diklik untuk: ${foodItem.name}")
                onAddToCartClicked(foodItem)
            }
        }
    }

    // Buat ViewHolder baru
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FoodViewHolder(binding)
    }

    // Bind data ke ViewHolder pada posisi tertentu
    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(foodList[position])
    }

    // Jumlah total item
    override fun getItemCount(): Int = foodList.size
}