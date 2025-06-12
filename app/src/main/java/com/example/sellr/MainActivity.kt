// Mendeklarasikan package tempat file ini berada.
package com.example.sellr

// Mengimpor kelas-kelas yang dibutuhkan untuk fungsionalitas Activity.
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sellr.databinding.ActivityMainBinding // ViewBinding untuk layout activity_main.xml
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * MainActivity adalah layar utama aplikasi setelah pengguna login.
 * Menampilkan daftar produk dan menyediakan navigasi ke keranjang, riwayat, dan fungsi logout.
 */
class MainActivity : AppCompatActivity() {

    // Properti untuk View Binding.
    private lateinit var binding: ActivityMainBinding
    // Adapter untuk menampilkan daftar makanan.
    private lateinit var foodAdapter: FoodAdapter
    // Properti untuk menampung daftar makanan. Di sini, diinisialisasi dengan list kosong.
    private var foodList: List<FoodItem> = listOf()

    // Instance dari Firebase Authentication untuk mengelola sesi pengguna (logout).
    private val auth = FirebaseAuth.getInstance()
    // Instance dari Firebase Realtime Database untuk operasi keranjang.
    private val database = FirebaseDatabase.getInstance("https://sellr-9c516-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    // Konstanta untuk nama node 'carts' di Firebase.
    private val cartNode = "carts"
    // Tag untuk logging.
    private val TAG = "MainActivity_DB_FIX"

    /**
     * Metode ini dipanggil saat Activity pertama kali dibuat.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.i(TAG, "onCreate: Firebase Database URL: ${database.toString()}")

        // Mengisi foodList dengan data produk yang statis (hardcoded).
        // Dalam aplikasi nyata, ini bisa diganti dengan pengambilan data dari Firebase atau API.
        foodList = getHardcodedProductListWithNewImages()
        // Panggil fungsi untuk menyiapkan RecyclerView.
        setupRecyclerView()
        // Panggil fungsi untuk menyiapkan aksi pada menu toolbar.
        setupToolbar()

        // Atur listener untuk Floating Action Button (FAB) keranjang.
        binding.fabCart.setOnClickListener {
            Log.d(TAG, "Tombol Keranjang (FAB) diklik.")
            // Saat diklik, buka CartActivity.
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    /**
     * Menyediakan daftar produk statis (hardcoded).
     * Berguna untuk pengembangan dan pengujian awal sebelum data dinamis diimplementasikan.
     * @return Sebuah List dari objek FoodItem.
     */
    private fun getHardcodedProductListWithNewImages(): List<FoodItem> {
        // Daftar produk yang didefinisikan langsung di dalam kode.
        return listOf(
            FoodItem(id = "HRD001", name = "Nasi Goreng Ayam Spesial", price = 28000, imageUrl = "https://i.pinimg.com/736x/94/82/ab/9482ab2e248d249e7daa7fd6924c8d3b.jpg"),
            FoodItem(id = "HRD002", name = "Ayam Bakar Kecap", price = 32000, imageUrl = "https://i.pinimg.com/736x/08/77/a7/0877a7d7d769099216823f067373a0fa.jpg"),
            FoodItem(id = "HRD003", name = "Soto Ayam", price = 20000, imageUrl = "https://i.pinimg.com/736x/c6/28/e5/c628e596829de0d478045472c8d2b260.jpg"),
            FoodItem(id = "HRD004", name = "Gado-Gado", price = 22000, imageUrl = "https://i.pinimg.com/736x/3a/ec/9e/3aec9effb6ac339895b8ef4e281b2acf.jpg"),
            FoodItem(id = "HRD005", name = "Es Jeruk Manis", price = 8000, imageUrl = "https://i.pinimg.com/736x/9a/10/98/9a10985db487e939ce8a4fc8dd6eb7d3.jpg"),
            FoodItem(id = "HRD006", name = "Mie Goreng Jawa", price = 26000, imageUrl = "https://i.pinimg.com/736x/0f/76/e8/0f76e8e797bf5d4e40f004475ffdbe16.jpg")
        )
    }

    /**
     * Mengatur listener untuk item menu pada toolbar.
     */
    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            // Menggunakan 'when' untuk memeriksa ID dari item menu yang diklik.
            when (menuItem.itemId) {
                // Jika menu riwayat yang diklik.
                R.id.menu_history -> {
                    Log.d(TAG, "Menu Riwayat Pesanan diklik.")
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true // Mengembalikan true menandakan bahwa klik telah ditangani.
                }
                // Jika menu logout yang diklik.
                R.id.menu_logout -> {
                    Log.d(TAG, "Menu Logout diklik.")
                    auth.signOut() // Melakukan proses logout dari Firebase.
                    Toast.makeText(this, "Anda telah logout.", Toast.LENGTH_SHORT).show()
                    // Arahkan kembali ke LoginActivity dan bersihkan back stack.
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish() // Tutup MainActivity.
                    true
                }
                // Jika item menu lain yang diklik.
                else -> false // Mengembalikan false menandakan bahwa klik tidak ditangani.
            }
        }
    }

    /**
     * Menginisialisasi FoodAdapter dan mengkonfigurasi RecyclerView.
     */
    private fun setupRecyclerView() {
        // Inisialisasi adapter dengan daftar makanan dan sebuah fungsi callback (lambda).
        // Blok kode '{ selectedFoodItem -> ... }' akan dieksekusi oleh adapter saat tombol "Tambah" diklik.
        foodAdapter = FoodAdapter(foodList) { selectedFoodItem ->
            Log.i(TAG, "Tombol 'Tambah' untuk item '${selectedFoodItem.name}' diklik di adapter.")
            // Panggil fungsi addToCart dengan item yang dipilih.
            addToCart(selectedFoodItem)
        }
        binding.rvFood.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = foodAdapter
        }
    }

    /**
     * Menangani logika penambahan item ke keranjang belanja di Firebase.
     * @param foodItem Objek makanan yang akan ditambahkan ke keranjang.
     */
    private fun addToCart(foodItem: FoodItem) {
        Log.i(TAG, "Memulai proses addToCart untuk: ${foodItem.name} (ID: ${foodItem.id})")
        val currentUser = auth.currentUser
        // Pemeriksaan keamanan: pastikan pengguna sudah login.
        if (currentUser == null) {
            Log.w(TAG, "addToCart gagal: Pengguna belum login.")
            Toast.makeText(this, "Anda belum login. Silakan login terlebih dahulu.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }

        Log.d(TAG, "Pengguna saat ini: ${currentUser.uid}")
        // Membuat referensi yang menunjuk ke lokasi spesifik item di dalam keranjang pengguna.
        val cartItemRef = database.child(cartNode).child(currentUser.uid).child(foodItem.id)
        Log.d(TAG, "Referensi Firebase untuk item keranjang: ${cartItemRef.toString()}")

        // Menggunakan addListenerForSingleValueEvent untuk membaca data SATU KALI saja.
        // Ini lebih efisien daripada addValueEventListener jika kita hanya perlu memeriksa data sekali.
        cartItemRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange dipanggil untuk item ${foodItem.id}. Snapshot ada: ${snapshot.exists()}, Value: ${snapshot.value}")
                // Periksa apakah item tersebut sudah ada di keranjang.
                if (snapshot.exists()) {
                    // JIKA SUDAH ADA: update kuantitasnya.
                    val currentCartItem = snapshot.getValue(CartItem::class.java)
                    val currentQuantity = currentCartItem?.quantity ?: 0
                    val newQuantity = currentQuantity + 1
                    Log.d(TAG, "Item ${foodItem.id} sudah ada. Kuantitas lama: $currentQuantity, Kuantitas baru: $newQuantity")

                    // Update hanya field "quantity" saja.
                    snapshot.ref.child("quantity").setValue(newQuantity)
                        .addOnSuccessListener {
                            Log.i(TAG, "Kuantitas untuk ${foodItem.name} berhasil diperbarui menjadi $newQuantity.")
                            Toast.makeText(this@MainActivity, "${foodItem.name} kuantitas +1", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Gagal memperbarui kuantitas untuk ${foodItem.name}: ${e.message}", e)
                        }
                } else {
                    // JIKA BELUM ADA: buat entri baru di keranjang.
                    Log.d(TAG, "Item ${foodItem.id} belum ada di keranjang. Membuat item baru.")
                    val newCartItem = CartItem(foodItem = foodItem, quantity = 1)
                    // Simpan objek CartItem yang baru ke Firebase.
                    cartItemRef.setValue(newCartItem)
                        .addOnSuccessListener {
                            Log.i(TAG, "${foodItem.name} berhasil ditambahkan ke keranjang.")
                            Toast.makeText(this@MainActivity, "${foodItem.name} ditambahkan", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Gagal menambahkan ${foodItem.name} ke keranjang: ${e.message}", e)
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Penanganan jika terjadi error pada saat pembacaan database.
                Log.w(TAG, "Operasi database addToCart dibatalkan untuk ${foodItem.id}: ${error.message}", error.toException())
            }
        })
    }
}