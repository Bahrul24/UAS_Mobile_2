// Paket tempat file ini berada
package com.example.sellr

// Import library untuk navigasi antar activity
import android.content.Intent
// Import untuk Bundle, digunakan saat activity dibuat
import android.os.Bundle
// Import untuk log debugging
import android.util.Log
// Import untuk mengatur visibilitas komponen
import android.view.View
// Import untuk menampilkan pesan singkat ke pengguna
import android.widget.Toast
// Import untuk membuat dialog konfirmasi
import androidx.appcompat.app.AlertDialog
// Import untuk activity berbasis AppCompat (kompatibel ke belakang)
import androidx.appcompat.app.AppCompatActivity
// Import untuk menampilkan daftar item secara vertikal
import androidx.recyclerview.widget.LinearLayoutManager
// Import view binding untuk ActivityCart
import com.example.sellr.databinding.ActivityCartBinding
// Import untuk autentikasi Firebase
import com.google.firebase.auth.FirebaseAuth
// Import untuk mengakses database realtime Firebase
import com.google.firebase.database.*
// Import untuk memformat angka ke dalam format mata uang
import java.text.NumberFormat
// Import untuk menentukan locale (lokasi) Indonesia
import java.util.Locale

// Kelas utama untuk menampilkan halaman keranjang
class CartActivity : AppCompatActivity() {

    // Inisialisasi objek binding untuk mengakses view XML dengan aman
    private lateinit var binding: ActivityCartBinding

    // Adapter untuk mengatur tampilan data keranjang di RecyclerView
    private lateinit var cartAdapter: CartAdapter

    // List yang menyimpan semua item dalam keranjang
    private val cartList = mutableListOf<CartItem>()

    // Objek autentikasi Firebase
    private val auth = FirebaseAuth.getInstance()

    // Referensi utama ke Firebase Realtime Database (menggunakan URL manual)
    private val database = FirebaseDatabase.getInstance("https://sellr-9c516-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    // Nama node untuk data keranjang
    private val cartNode = "carts"
    // Nama node untuk data pesanan
    private val ordersNode = "orders"

    // Tag log untuk identifikasi log di Logcat
    private val TAG = "CartActivity_DB_FIX"

    // Fungsi yang dipanggil saat activity pertama kali dibuat
    override fun onCreate(savedInstanceState: Bundle?) {
        // Panggil metode superclass untuk setup awal
        super.onCreate(savedInstanceState)
        // Inisialisasi binding dengan layout
        binding = ActivityCartBinding.inflate(layoutInflater)
        // Set tampilan utama ke root dari binding
        setContentView(binding.root)

        // Tampilkan URL database di Logcat untuk debugging
        Log.i(TAG, "onCreate: Firebase Database URL: ${database.toString()}")

        // Ambil user ID dari pengguna yang login
        val userId = auth.currentUser?.uid
        // Jika user belum login, tampilkan pesan dan keluar dari activity
        if (userId == null) {
            Log.e(TAG, "onCreate gagal: userId null. Pengguna tidak login.")
            Toast.makeText(this, "Sesi berakhir, silakan login kembali.", Toast.LENGTH_LONG).show()
            finish() // Tutup activity
            return
        }
        // Tampilkan user ID di log
        Log.d(TAG, "Pengguna saat ini: $userId")

        // Setup RecyclerView dan ambil data keranjang dari Firebase
        setupRecyclerView(userId)
        fetchCartData(userId)

        // Saat tombol kembali di toolbar diklik, keluar dari activity
        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Tombol kembali (toolbar) diklik.")
            finish()
        }

        // Saat tombol checkout diklik
        binding.btnCheckout.setOnClickListener {
            Log.i(TAG, "Tombol Checkout diklik.")
            // Jika keranjang tidak kosong
            if (cartList.isNotEmpty()) {
                Log.d(TAG, "Keranjang tidak kosong (${cartList.size} item). Memulai konfirmasi checkout.")
                showCheckoutConfirmationDialog(userId)
            } else {
                // Jika kosong, beri tahu pengguna
                Log.w(TAG, "Tombol Checkout diklik, tetapi keranjang kosong.")
                Toast.makeText(this, "Keranjang Anda kosong!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi untuk menyiapkan RecyclerView
    private fun setupRecyclerView(userId: String) {
        // Tampilkan log bahwa RecyclerView sedang disiapkan
        Log.d(TAG, "setupRecyclerView untuk userId: $userId")
        // Buat adapter untuk daftar item keranjang
        cartAdapter = CartAdapter(
            cartList, // Data list item keranjang
            onIncrement = { cartItem ->
                // Fungsi saat tombol tambah ditekan
                updateQuantity(userId, cartItem, cartItem.quantity + 1)
            },
            onDecrement = { cartItem ->
                // Fungsi saat tombol kurang ditekan
                if (cartItem.quantity > 1) {
                    // Kurangi jumlah item
                    updateQuantity(userId, cartItem, cartItem.quantity - 1)
                } else {
                    // Jika jumlah 1 dan dikurangi, hapus item
                    Log.d(TAG, "Kuantitas item ${cartItem.foodItem.name} akan menjadi 0, menghapus dari keranjang.")
                    removeItemFromCart(userId, cartItem)
                }
            }
        )
        // Pasang layout dan adapter ke RecyclerView
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(this@CartActivity) // Tampilkan vertikal
            adapter = cartAdapter
        }
    }

    // Fungsi untuk mengambil data keranjang dari Firebase
    private fun fetchCartData(userId: String) {
        Log.d(TAG, "fetchCartData untuk userId: $userId")
        // Referensi ke node keranjang user
        val userCartRef = database.child(cartNode).child(userId)
        Log.d(TAG, "Referensi keranjang pengguna: ${userCartRef.toString()}")

        // Dengarkan perubahan data
        userCartRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i(TAG, "onDataChange untuk keranjang dipanggil. Snapshot ada: ${snapshot.exists()}, Value: ${snapshot.value}")
                cartList.clear() // Kosongkan list lama
                if (snapshot.exists()) {
                    // Loop semua item dalam snapshot
                    for (cartSnapshot in snapshot.children) {
                        val cartItem = cartSnapshot.getValue(CartItem::class.java)
                        if (cartItem != null) {
                            Log.d(TAG, "Menambahkan item ke cartList: ${cartItem.foodItem.name} (Qty: ${cartItem.quantity})")
                            cartList.add(cartItem)
                        } else {
                            Log.w(TAG, "Gagal parse CartItem dari snapshot: ${cartSnapshot.key}")
                        }
                    }
                } else {
                    Log.d(TAG, "Snapshot keranjang tidak ada atau kosong.")
                }
                cartAdapter.notifyDataSetChanged() // Perbarui tampilan RecyclerView
                updateTotalPrice() // Perbarui total harga
                toggleEmptyView() // Periksa apakah keranjang kosong
                Log.d(TAG, "Total item di cartList setelah onDataChange: ${cartList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "fetchCartData dibatalkan: ${error.message}", error.toException())
                Toast.makeText(this@CartActivity, "Gagal memuat keranjang: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // Fungsi untuk memperbarui kuantitas item
    private fun updateQuantity(userId: String, cartItem: CartItem, newQuantity: Int) {
        Log.d(TAG, "updateQuantity untuk ${cartItem.foodItem.name} menjadi $newQuantity")
        database.child(cartNode).child(userId).child(cartItem.foodItem.id).child("quantity").setValue(newQuantity)
            .addOnSuccessListener {
                Log.i(TAG, "Kuantitas untuk ${cartItem.foodItem.name} berhasil diupdate.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Gagal update kuantitas: ${e.message}")
            }
    }

    // Fungsi untuk menghapus item dari keranjang
    private fun removeItemFromCart(userId: String, cartItem: CartItem) {
        Log.d(TAG, "removeItemFromCart untuk ${cartItem.foodItem.name}")
        database.child(cartNode).child(userId).child(cartItem.foodItem.id).removeValue()
            .addOnSuccessListener {
                Log.i(TAG, "Item ${cartItem.foodItem.name} berhasil dihapus.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Gagal hapus item: ${e.message}")
            }
    }

    // Fungsi untuk menghitung total harga
    private fun updateTotalPrice() {
        val totalPrice = cartList.sumOf { it.foodItem.price * it.quantity }
        Log.d(TAG, "updateTotalPrice: Total harga baru adalah Rp $totalPrice")
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        binding.tvTotalPrice.text = formatter.format(totalPrice)
    }

    // Fungsi untuk menampilkan teks jika keranjang kosong
    private fun toggleEmptyView() {
        if (cartList.isEmpty()) {
            Log.d(TAG, "toggleEmptyView: Keranjang kosong.")
            binding.tvEmptyCart.visibility = View.VISIBLE
            binding.rvCart.visibility = View.GONE
            binding.btnCheckout.isEnabled = false
        } else {
            Log.d(TAG, "toggleEmptyView: Keranjang berisi item.")
            binding.tvEmptyCart.visibility = View.GONE
            binding.rvCart.visibility = View.VISIBLE
            binding.btnCheckout.isEnabled = true
        }
    }

    // Tampilkan dialog konfirmasi saat checkout
    private fun showCheckoutConfirmationDialog(userId: String) {
        Log.d(TAG, "Menampilkan dialog konfirmasi checkout.")
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pesanan")
            .setMessage("Anda yakin ingin menyelesaikan pesanan ini?")
            .setPositiveButton("Ya, Proses Pesanan") { dialog, _ ->
                Log.i(TAG, "Pengguna mengkonfirmasi pesanan. Memulai processCheckout.")
                processCheckout(userId)
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                Log.d(TAG, "Pengguna membatalkan checkout.")
                dialog.dismiss()
            }
            .show()
    }

    // Proses checkout (simpan order ke Firebase dan hapus keranjang)
    private fun processCheckout(userId: String) {
        Log.i(TAG, "Memulai processCheckout untuk userId: $userId")
        if (cartList.isEmpty()) {
            Log.w(TAG, "processCheckout dihentikan: Keranjang kosong.")
            Toast.makeText(this, "Tidak ada item untuk di-checkout.", Toast.LENGTH_SHORT).show()
            return
        }

        val ordersUserRef = database.child(ordersNode).child(userId)
        val orderId = ordersUserRef.push().key // Buat ID pesanan baru

        if (orderId == null) {
            Log.e(TAG, "processCheckout gagal: Gagal membuat orderId (null).")
            Toast.makeText(this, "Gagal membuat ID pesanan. Coba lagi.", Toast.LENGTH_LONG).show()
            return
        }
        Log.d(TAG, "orderId berhasil dibuat: $orderId")

        val totalOrderPrice = cartList.sumOf { it.foodItem.price * it.quantity }
        val currentTimestamp = System.currentTimeMillis()
        val itemsForOrder = ArrayList(cartList)

        // Buat objek Order
        val newOrder = Order(
            orderId = orderId,
            items = itemsForOrder,
            totalPrice = totalOrderPrice,
            timestamp = currentTimestamp,
            userId = userId
        )
        Log.d(TAG, "Objek Order dibuat: $newOrder")

        // Simpan order ke Firebase
        ordersUserRef.child(orderId).setValue(newOrder)
            .addOnSuccessListener {
                Log.i(TAG, "Pesanan $orderId berhasil disimpan ke Firebase.")
                Toast.makeText(this, "Pesanan berhasil dibuat!", Toast.LENGTH_SHORT).show()

                // Hapus keranjang
                val userCartRef = database.child(cartNode).child(userId)
                userCartRef.removeValue()
                    .addOnSuccessListener {
                        Log.i(TAG, "Keranjang untuk pengguna $userId berhasil dihapus.")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Gagal menghapus keranjang $userId: ${e.message}", e)
                    }

                // Arahkan ke halaman riwayat pesanan
                Log.d(TAG, "Mengarahkan ke HistoryActivity.")
                val intent = Intent(this, HistoryActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Gagal menyimpan pesanan $orderId ke Firebase: ${e.message}", e)
                Toast.makeText(this, "Gagal memproses pesanan: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
