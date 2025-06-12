// Menentukan bahwa file ini berada dalam package com.example.sellr
package com.example.sellr

// Import LayoutInflater untuk membuat view dari file XML layout
import android.view.LayoutInflater
// Import ViewGroup sebagai parent dari item RecyclerView
import android.view.ViewGroup
// Import RecyclerView untuk membuat adapter dan view holder
import androidx.recyclerview.widget.RecyclerView
// Import binding untuk item_order.xml
import com.example.sellr.databinding.ItemOrderBinding
// Import untuk format angka ke dalam mata uang
import java.text.NumberFormat
// Import untuk format tanggal dan waktu
import java.text.SimpleDateFormat
// Import untuk menentukan lokasi (digunakan dalam format)
import java.util.*

// Kelas OrderAdapter digunakan untuk menghubungkan data Order dengan RecyclerView
// Adapter ini menerima daftar Order (orderList) dan menampilkannya dalam RecyclerView
class OrderAdapter(private val orderList: List<Order>) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    // ViewHolder sebagai pembungkus tampilan satu item pesanan dalam RecyclerView
    inner class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        // Fungsi untuk mengisi tampilan (bind) dengan data dari satu objek Order
        fun bind(order: Order) {
            // Menampilkan 6 karakter terakhir dari orderId sebagai identitas pesanan
            binding.tvOrderId.text = "#${order.orderId.takeLast(6)}"

            // Membuat formatter tanggal dengan format Indonesia
            val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
            // Format dan tampilkan waktu pemesanan
            binding.tvOrderDate.text = sdf.format(Date(order.timestamp))

            // Format harga total menjadi format mata uang Indonesia
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            // Tampilkan total harga pesanan
            binding.tvOrderTotal.text = formatter.format(order.totalPrice)
        }
    }

    // Fungsi yang dipanggil saat ViewHolder baru perlu dibuat
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        // Inflate layout item_order.xml menggunakan ViewBinding
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Kembalikan instance ViewHolder dengan binding yang telah dibuat
        return OrderViewHolder(binding)
    }

    // Fungsi untuk menghubungkan data (Order) pada posisi tertentu dengan ViewHolder
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        // Ambil item Order dari posisi dan ikatkan ke holder
        holder.bind(orderList[position])
    }

    // Fungsi untuk menghitung jumlah item yang akan ditampilkan di RecyclerView
    override fun getItemCount(): Int = orderList.size
    // Mengembalikan ukuran dari daftar pesanan
}
