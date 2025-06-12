// Mendeklarasikan package tempat file ini berada. Ini adalah cara Kotlin/Java mengorganisir kode.
package com.example.sellr

// Mengimpor kelas-kelas yang dibutuhkan dari library Android dan Firebase.
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sellr.databinding.ActivityCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.Locale

/**
 * CartActivity adalah layar yang menampilkan item-item yang telah ditambahkan pengguna ke keranjang belanja.
 * Aktivitas ini bertanggung jawab untuk:
 * - Mengambil dan menampilkan data keranjang dari Firebase Realtime Database.
 * - Memperbarui kuantitas atau menghapus item dari keranjang.
 * - Menghitung dan menampilkan total harga.
 * - Memproses checkout, yang akan memindahkan item keranjang ke daftar pesanan (orders) dan mengosongkan keranjang.
 */
class CartActivity : AppCompatActivity() {

    // Properti untuk View Binding, memungkinkan akses aman dan mudah ke komponen UI di layout activity_cart.xml.
    private lateinit var binding: ActivityCartBinding
    // Adapter untuk RecyclerView, yang akan mengelola bagaimana data item keranjang ditampilkan.
    private lateinit var cartAdapter: CartAdapter
    // List yang bisa diubah (mutable) untuk menampung objek-objek CartItem yang diambil dari Firebase.
    private val cartList = mutableListOf<CartItem>()
    // Instance dari Firebase Authentication untuk mendapatkan informasi pengguna yang sedang login.
    private val auth = FirebaseAuth.getInstance()
    // Instance dari Firebase Realtime Database. URL spesifik ditambahkan untuk memastikan koneksi ke database yang benar.
    // .reference menunjuk ke root (akar) dari database.
    private val database = FirebaseDatabase.getInstance("https://sellr-9c516-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    // Konstanta untuk nama node (seperti folder) di Firebase agar kode lebih rapi dan mudah diubah.
    private val cartNode = "carts"
    private val ordersNode = "orders"
    // Tag untuk logging, mempermudah proses debugging dengan menyaring log di Logcat.
    private val TAG = "CartActivity_DB_FIX"

    /**
     * Metode ini dipanggil saat Activity pertama kali dibuat.
     * Tempat untuk inisialisasi UI, data, dan listener.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menginisialisasi objek binding dengan "menggembungkan" (inflate) layout XML.
        binding = ActivityCartBinding.inflate(layoutInflater)
        // Menetapkan layout utama untuk Activity ini. binding.root adalah view terluar dari layout.
        setContentView(binding.root)

        // Mencetak URL database ke Logcat untuk verifikasi saat debugging.
        Log.i(TAG, "onCreate: Firebase Database URL: ${database.toString()}")

        // Mendapatkan ID unik (UID) dari pengguna yang saat ini login.
        val userId = auth.currentUser?.uid

        // Pemeriksaan krusial: Jika tidak ada pengguna yang login (userId null), hentikan proses.
        if (userId == null) {
            Log.e(TAG, "onCreate gagal: userId null. Pengguna tidak login.")
            Toast.makeText(this, "Sesi berakhir, silakan login kembali.", Toast.LENGTH_LONG).show()
            finish() // Tutup activity ini.
            return   // Hentikan eksekusi kode lebih lanjut di dalam onCreate.
        }
        // Jika berhasil, catat UID pengguna untuk debugging.
        Log.d(TAG, "Pengguna saat ini: $userId")

        // Panggil fungsi untuk menyiapkan RecyclerView.
        setupRecyclerView(userId)
        // Panggil fungsi untuk mengambil data keranjang dari Firebase.
        fetchCartData(userId)

        // Atur listener untuk tombol navigasi (panah kembali) di toolbar.
        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Tombol kembali (toolbar) diklik.")
            finish() // Menutup activity saat ini dan kembali ke layar sebelumnya.
        }

        // Atur listener untuk tombol checkout.
        binding.btnCheckout.setOnClickListener {
            Log.i(TAG, "Tombol Checkout diklik.")
            // Hanya proses jika keranjang tidak kosong.
            if (cartList.isNotEmpty()) {
                Log.d(TAG, "Keranjang tidak kosong (${cartList.size} item). Memulai konfirmasi checkout.")
                // Tampilkan dialog untuk memastikan pengguna benar-benar ingin checkout.
                showCheckoutConfirmationDialog(userId)
            } else {
                Log.w(TAG, "Tombol Checkout diklik, tetapi keranjang kosong.")
                Toast.makeText(this, "Keranjang Anda kosong!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Menginisialisasi CartAdapter dan mengkonfigurasi RecyclerView.
     * @param userId UID pengguna saat ini, diperlukan untuk meneruskan ke fungsi update/delete.
     */
    private fun setupRecyclerView(userId: String) {
        Log.d(TAG, "setupRecyclerView untuk userId: $userId")
        // Inisialisasi adapter dengan list data dan dua fungsi lambda sebagai callback.
        cartAdapter = CartAdapter(cartList,
            // Callback ini akan dipanggil dari adapter saat tombol '+' diklik.
            onIncrement = { cartItem -> updateQuantity(userId, cartItem, cartItem.quantity + 1) },
            // Callback ini akan dipanggil dari adapter saat tombol '-' diklik.
            onDecrement = { cartItem ->
                // Jika kuantitas lebih dari 1, kurangi saja.
                if (cartItem.quantity > 1) {
                    updateQuantity(userId, cartItem, cartItem.quantity - 1)
                } else {
                    // Jika kuantitas adalah 1, hapus item dari keranjang.
                    Log.d(TAG, "Kuantitas item ${cartItem.foodItem.name} akan menjadi 0, menghapus dari keranjang.")
                    removeItemFromCart(userId, cartItem)
                }
            }
        )
        // Konfigurasi RecyclerView menggunakan blok 'apply' agar lebih ringkas.
        binding.rvCart.apply {
            // Atur item agar ditampilkan dalam daftar linear vertikal.
            layoutManager = LinearLayoutManager(this@CartActivity)
            // Hubungkan adapter yang telah dibuat ke RecyclerView.
            adapter = cartAdapter
        }
    }

    /**
     * Mengambil data keranjang dari Firebase Realtime Database dan mengatur pendengar (listener)
     * untuk perubahan data secara real-time.
     * @param userId UID pengguna untuk menemukan keranjang yang benar di database.
     */
    private fun fetchCartData(userId: String) {
        Log.d(TAG, "fetchCartData untuk userId: $userId")
        // Membuat referensi yang menunjuk langsung ke node keranjang milik pengguna. (e.g., /carts/USER_ID_ABC)
        val userCartRef = database.child(cartNode).child(userId)
        Log.d(TAG, "Referensi keranjang pengguna: ${userCartRef.toString()}")

        // Menambahkan listener yang akan aktif setiap kali ada perubahan data di 'userCartRef'.
        userCartRef.addValueEventListener(object : ValueEventListener {
            /**
             * Metode ini dipanggil sekali saat listener dipasang, dan setiap kali data berubah.
             * @param snapshot Berisi data dari lokasi Firebase.
             */
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i(TAG, "onDataChange untuk keranjang dipanggil. Snapshot ada: ${snapshot.exists()}, Value: ${snapshot.value}")
                // Kosongkan list lokal terlebih dahulu untuk menghindari duplikasi data.
                cartList.clear()
                // Periksa apakah snapshot (data) benar-benar ada.
                if (snapshot.exists()) {
                    // Iterasi melalui setiap anak dari snapshot (setiap item di keranjang).
                    for (cartSnapshot in snapshot.children) {
                        // Firebase secara otomatis mengubah data JSON menjadi objek Kotlin 'CartItem'.
                        val cartItem = cartSnapshot.getValue(CartItem::class.java)
                        // Jika konversi berhasil, tambahkan item ke list lokal.
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
                // Beri tahu adapter bahwa dataset telah berubah, agar UI RecyclerView di-refresh.
                cartAdapter.notifyDataSetChanged()
                // Perbarui tampilan total harga.
                updateTotalPrice()
                // Tampilkan atau sembunyikan pesan "keranjang kosong".
                toggleEmptyView()
                Log.d(TAG, "Total item di cartList setelah onDataChange: ${cartList.size}")
            }

            /**
             * Metode ini dipanggil jika terjadi error saat membaca data dari Firebase.
             */
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "fetchCartData dibatalkan: ${error.message}", error.toException())
                Toast.makeText(this@CartActivity, "Gagal memuat keranjang: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    /**
     * Memperbarui field 'quantity' dari sebuah item di Firebase.
     * @param userId UID pengguna.
     * @param cartItem Item yang kuantitasnya akan diubah.
     * @param newQuantity Nilai kuantitas yang baru.
     */
    private fun updateQuantity(userId: String, cartItem: CartItem, newQuantity: Int) {
        Log.d(TAG, "updateQuantity untuk ${cartItem.foodItem.name} menjadi $newQuantity")
        // Navigasi ke field 'quantity' dari item spesifik dan atur nilainya.
        database.child(cartNode).child(userId).child(cartItem.foodItem.id).child("quantity").setValue(newQuantity)
            .addOnSuccessListener { Log.i(TAG, "Kuantitas untuk ${cartItem.foodItem.name} berhasil diupdate.") }
            .addOnFailureListener { e-> Log.e(TAG, "Gagal update kuantitas: ${e.message}") }
    }

    /**
     * Menghapus seluruh node item dari keranjang di Firebase.
     * @param userId UID pengguna.
     * @param cartItem Item yang akan dihapus.
     */
    private fun removeItemFromCart(userId: String, cartItem: CartItem) {
        Log.d(TAG, "removeItemFromCart untuk ${cartItem.foodItem.name}")
        // Navigasi ke node item spesifik dan hapus seluruhnya.
        database.child(cartNode).child(userId).child(cartItem.foodItem.id).removeValue()
            .addOnSuccessListener { Log.i(TAG, "Item ${cartItem.foodItem.name} berhasil dihapus.") }
            .addOnFailureListener { e-> Log.e(TAG, "Gagal hapus item: ${e.message}") }
    }

    /**
     * Menghitung total harga dari semua item di 'cartList' dan memformatnya
     * ke dalam format mata uang Rupiah (IDR), lalu menampilkannya di UI.
     */
    private fun updateTotalPrice() {
        // Gunakan fungsi sumOf untuk menjumlahkan hasil dari (harga * kuantitas) untuk setiap item.
        val totalPrice = cartList.sumOf { (it.foodItem.price * it.quantity) }
        Log.d(TAG, "updateTotalPrice: Total harga baru adalah Rp $totalPrice")
        // Buat formatter untuk mata uang Rupiah Indonesia.
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        // Atur teks pada TextView total harga dengan nilai yang sudah diformat.
        binding.tvTotalPrice.text = formatter.format(totalPrice)
    }

    /**
     * Mengatur visibilitas elemen UI berdasarkan apakah keranjang kosong atau tidak.
     */
    private fun toggleEmptyView() {
        if (cartList.isEmpty()) {
            Log.d(TAG, "toggleEmptyView: Keranjang kosong.")
            // Jika kosong, tampilkan pesan "keranjang kosong".
            binding.tvEmptyCart.visibility = View.VISIBLE
            // Sembunyikan RecyclerView.
            binding.rvCart.visibility = View.GONE
            // Nonaktifkan tombol checkout.
            binding.btnCheckout.isEnabled = false
        } else {
            Log.d(TAG, "toggleEmptyView: Keranjang berisi item.")
            // Jika ada isinya, sembunyikan pesan "keranjang kosong".
            binding.tvEmptyCart.visibility = View.GONE
            // Tampilkan RecyclerView.
            binding.rvCart.visibility = View.VISIBLE
            // Aktifkan tombol checkout.
            binding.btnCheckout.isEnabled = true
        }
    }

    /**
     * Menampilkan dialog konfirmasi kepada pengguna sebelum memproses pesanan.
     * @param userId UID pengguna untuk diteruskan ke fungsi processCheckout.
     */
    private fun showCheckoutConfirmationDialog(userId: String) {
        Log.d(TAG, "Menampilkan dialog konfirmasi checkout.")
        // Menggunakan AlertDialog.Builder untuk membuat dialog.
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pesanan")
            .setMessage("Anda yakin ingin menyelesaikan pesanan ini?")
            // Tombol positif: jika diklik, jalankan proses checkout.
            .setPositiveButton("Ya, Proses Pesanan") { dialog, _ ->
                Log.i(TAG, "Pengguna mengkonfirmasi pesanan. Memulai processCheckout.")
                processCheckout(userId)
                dialog.dismiss() // Tutup dialog.
            }
            // Tombol negatif: jika diklik, batalkan aksi dan tutup dialog.
            .setNegativeButton("Batal") { dialog, _ ->
                Log.d(TAG, "Pengguna membatalkan checkout.")
                dialog.dismiss() // Tutup dialog.
            }
            .show() // Tampilkan dialog ke layar.
    }

    /**
     * Memproses checkout: membuat entri pesanan baru di Firebase, menghapus keranjang,
     * dan mengarahkan pengguna ke halaman riwayat pesanan.
     * @param userId UID pengguna.
     */
    private fun processCheckout(userId: String) {
        Log.i(TAG, "Memulai processCheckout untuk userId: $userId")
        // Pengecekan ulang jika keranjang ternyata kosong saat fungsi ini dipanggil.
        if (cartList.isEmpty()) {
            Log.w(TAG, "processCheckout dihentikan: Keranjang kosong.")
            Toast.makeText(this, "Tidak ada item untuk di-checkout.", Toast.LENGTH_SHORT).show()
            return
        }

        // Referensi ke node 'orders' milik pengguna.
        val ordersUserRef = database.child(ordersNode).child(userId)
        // Buat ID unik baru untuk pesanan ini menggunakan push().key.
        val orderId = ordersUserRef.push().key

        // Jika karena suatu alasan orderId gagal dibuat (null), hentikan proses.
        if(orderId == null) {
            Log.e(TAG, "processCheckout gagal: Gagal membuat orderId (null).")
            Toast.makeText(this, "Gagal membuat ID pesanan. Coba lagi.", Toast.LENGTH_LONG).show()
            return
        }
        Log.d(TAG, "orderId berhasil dibuat: $orderId")

        // Hitung total harga pesanan.
        val totalOrderPrice = cartList.sumOf { (it.foodItem.price * it.quantity) }
        // Dapatkan waktu saat ini dalam milidetik.
        val currentTimestamp = System.currentTimeMillis()
        // Salin item keranjang ke list baru untuk disimpan di dalam objek Order.
        val itemsForOrder = ArrayList(cartList)

        // Buat objek Order baru dengan semua data yang relevan.
        val newOrder = Order(
            orderId = orderId,
            items = itemsForOrder,
            totalPrice = totalOrderPrice,
            timestamp = currentTimestamp,
            userId = userId
        )
        Log.d(TAG, "Objek Order dibuat: $newOrder")

        // Simpan objek 'newOrder' ke Firebase di bawah ID pesanan yang baru dibuat.
        ordersUserRef.child(orderId).setValue(newOrder)
            .addOnSuccessListener {
                // Blok ini dieksekusi HANYA JIKA pesanan berhasil disimpan.
                Log.i(TAG, "Pesanan $orderId berhasil disimpan ke Firebase.")
                Toast.makeText(this, "Pesanan berhasil dibuat!", Toast.LENGTH_SHORT).show()

                // Setelah pesanan berhasil, hapus keranjang pengguna.
                val userCartRef = database.child(cartNode).child(userId)
                userCartRef.removeValue()
                    .addOnSuccessListener { Log.i(TAG, "Keranjang untuk pengguna $userId berhasil dihapus.") }
                    .addOnFailureListener { e -> Log.e(TAG, "Gagal menghapus keranjang $userId: ${e.message}", e) }

                // Arahkan pengguna ke halaman riwayat pesanan (HistoryActivity).
                Log.d(TAG, "Mengarahkan ke HistoryActivity.")
                val intent = Intent(this, HistoryActivity::class.java)
                // Flags ini membersihkan tumpukan aktivitas di atasnya, sehingga pengguna tidak bisa kembali
                // ke CartActivity dengan menekan tombol 'back'.
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish() // Tutup CartActivity.
            }
            .addOnFailureListener { e ->
                // Blok ini dieksekusi jika gagal menyimpan pesanan.
                Log.e(TAG, "Gagal menyimpan pesanan $orderId ke Firebase: ${e.message}", e)
                Toast.makeText(this, "Gagal memproses pesanan: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}