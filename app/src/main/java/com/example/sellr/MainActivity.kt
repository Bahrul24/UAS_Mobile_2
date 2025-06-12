// Package tempat kelas ini berada
package com.example.sellr

// Import komponen Android yang diperlukan
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sellr.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

// Kelas utama yang menangani tampilan daftar makanan
class MainActivity : AppCompatActivity() {

    // View binding untuk mengakses elemen layout activity_main.xml
    private lateinit var binding: ActivityMainBinding

    // Adapter untuk RecyclerView makanan
    private lateinit var foodAdapter: FoodAdapter

    // List makanan yang ditampilkan
    private var foodList: List<FoodItem> = listOf()

    // Firebase Authentication untuk login pengguna
    private val auth = FirebaseAuth.getInstance()

    // Referensi ke Firebase Realtime Database (gunakan URL sesuai lokasi region Anda)
    private val database = FirebaseDatabase.getInstance(
        "https://sellr-9c516-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).reference

    // Nama node keranjang di Firebase
    private val cartNode = "carts"

    // Tag untuk log
    private val TAG = "MainActivity_DB_FIX"

    // Fungsi yang dipanggil saat Activity pertama kali dibuat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cetak URL database ke log (debug)
        Log.i(TAG, "onCreate: Firebase Database URL: ${database.toString()}")

        // Ambil data makanan dari fungsi hardcoded
        foodList = getHardcodedProductListWithNewImages()

        // Siapkan RecyclerView dengan adapter
        setupRecyclerView()

        // Siapkan toolbar (menu)
        setupToolbar()

        // Ketika tombol keranjang (FAB) ditekan
        binding.fabCart.setOnClickListener {
            Log.d(TAG, "Tombol Keranjang (FAB) diklik.")
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    // Fungsi untuk membuat list makanan secara manual (sementara tidak ambil dari Firebase)
    private fun getHardcodedProductListWithNewImages(): List<FoodItem> {
        return listOf(
            FoodItem(
                id = "HRD001",
                name = "Nasi Goreng Ayam Spesial",
                price = 28000,
                imageUrl = "https://i.pinimg.com/736x/94/82/ab/9482ab2e248d249e7daa7fd6924c8d3b.jpg"
            ),
            FoodItem(
                id = "HRD002",
                name = "Ayam Bakar Kecap",
                price = 32000,
                imageUrl = "https://i.pinimg.com/736x/08/77/a7/0877a7d7d769099216823f067373a0fa.jpg"
            ),
            FoodItem(
                id = "HRD003",
                name = "Soto Ayam",
                price = 20000,
                imageUrl = "https://i.pinimg.com/736x/c6/28/e5/c628e596829de0d478045472c8d2b260.jpg"
            ),
            FoodItem(
                id = "HRD004",
                name = "Gado-Gado",
                price = 22000,
                imageUrl = "https://i.pinimg.com/736x/3a/ec/9e/3aec9effb6ac339895b8ef4e281b2acf.jpg"
            ),
            FoodItem(
                id = "HRD005",
                name = "Es Jeruk Manis",
                price = 8000,
                imageUrl = "https://i.pinimg.com/736x/9a/10/98/9a10985db487e939ce8a4fc8dd6eb7d3.jpg"
            ),
            FoodItem(
                id = "HRD006",
                name = "Mie Goreng Jawa",
                price = 26000,
                imageUrl = "https://i.pinimg.com/736x/0f/76/e8/0f76e8e797bf5d4e40f004475ffdbe16.jpg"
            )
        )
    }

    // Fungsi untuk mengatur toolbar menu
    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_history -> {
                    Log.d(TAG, "Menu Riwayat Pesanan diklik.")
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }

                R.id.menu_logout -> {
                    Log.d(TAG, "Menu Logout diklik.")
                    auth.signOut() // Logout pengguna
                    Toast.makeText(this, "Anda telah logout.", Toast.LENGTH_SHORT).show()

                    // Arahkan ke LoginActivity
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    true
                }

                else -> false
            }
        }
    }

    // Fungsi untuk mengatur RecyclerView makanan
    private fun setupRecyclerView() {
        // Buat instance adapter, dengan aksi saat tombol tambah ditekan
        foodAdapter = FoodAdapter(foodList) { selectedFoodItem ->
            Log.i(TAG, "Tombol 'Tambah' untuk item '${selectedFoodItem.name}' diklik di adapter.")
            addToCart(selectedFoodItem) // Tambahkan ke keranjang
        }

        // Konfigurasi RecyclerView
        binding.rvFood.apply {
            layoutManager = LinearLayoutManager(this@MainActivity) // Tampilkan secara vertikal
            adapter = foodAdapter
        }
    }

    // Fungsi untuk menambahkan makanan ke keranjang di Firebase
    private fun addToCart(foodItem: FoodItem) {
        Log.i(TAG, "Memulai proses addToCart untuk: ${foodItem.name} (ID: ${foodItem.id})")

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "addToCart gagal: Pengguna belum login.")
            Toast.makeText(this, "Anda belum login. Silakan login terlebih dahulu.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }

        Log.d(TAG, "Pengguna saat ini: ${currentUser.uid}")

        // Referensi ke lokasi item di keranjang pengguna di Firebase
        val cartItemRef = database.child(cartNode).child(currentUser.uid).child(foodItem.id)
        Log.d(TAG, "Referensi Firebase untuk item keranjang: ${cartItemRef.toString()}")

        // Ambil data lama untuk cek apakah sudah ada
        cartItemRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange dipanggil untuk item ${foodItem.id}. Snapshot ada: ${snapshot.exists()}, Value: ${snapshot.value}")

                if (snapshot.exists()) {
                    // Jika item sudah ada, tambahkan quantity
                    val currentCartItem = snapshot.getValue(CartItem::class.java)
                    val currentQuantity = currentCartItem?.quantity ?: 0
                    val newQuantity = currentQuantity + 1

                    Log.d(TAG, "Item ${foodItem.id} sudah ada. Kuantitas lama: $currentQuantity, Kuantitas baru: $newQuantity")

                    // Update hanya bagian kuantitas
                    snapshot.ref.child("quantity").setValue(newQuantity)
                        .addOnSuccessListener {
                            Log.i(TAG, "Kuantitas untuk ${foodItem.name} berhasil diperbarui menjadi $newQuantity.")
                            Toast.makeText(this@MainActivity, "${foodItem.name} kuantitas +1", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Gagal memperbarui kuantitas untuk ${foodItem.name}: ${e.message}", e)
                            Toast.makeText(this@MainActivity, "Gagal update kuantitas: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    // Jika belum ada, buat item baru
                    Log.d(TAG, "Item ${foodItem.id} belum ada di keranjang. Membuat item baru.")
                    val newCartItem = CartItem(foodItem = foodItem, quantity = 1)

                    cartItemRef.setValue(newCartItem)
                        .addOnSuccessListener {
                            Log.i(TAG, "${foodItem.name} berhasil ditambahkan ke keranjang.")
                            Toast.makeText(this@MainActivity, "${foodItem.name} ditambahkan", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Gagal menambahkan ${foodItem.name} ke keranjang: ${e.message}", e)
                            Toast.makeText(this@MainActivity, "Gagal menambahkan: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Jika operasi database dibatalkan
                Log.w(TAG, "Operasi database addToCart dibatalkan untuk ${foodItem.id}: ${error.message}", error.toException())
                Toast.makeText(this@MainActivity, "Database error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
