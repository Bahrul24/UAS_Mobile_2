// Mendeklarasikan package tempat file ini berada.
package com.example.sellr

// Mengimpor kelas-kelas yang dibutuhkan untuk fungsionalitas Activity.
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sellr.databinding.ActivityHistoryBinding // ViewBinding untuk layout activity_history.xml
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * HistoryActivity adalah layar yang menampilkan riwayat pesanan pengguna.
 * Ia mengambil data dari node 'orders' di Firebase dan menampilkannya dalam sebuah daftar.
 */
class HistoryActivity : AppCompatActivity() {

    // Properti untuk View Binding, memungkinkan akses aman ke komponen UI di layout.
    private lateinit var binding: ActivityHistoryBinding
    // Adapter untuk RecyclerView, yang akan mengelola bagaimana data pesanan ditampilkan.
    private lateinit var orderAdapter: OrderAdapter
    // List yang bisa diubah untuk menampung objek-objek Order yang diambil dari Firebase.
    private val orderList = mutableListOf<Order>()
    // Instance dari Firebase Authentication untuk mendapatkan informasi pengguna yang sedang login.
    private val auth = FirebaseAuth.getInstance()
    // Instance dari Firebase Realtime Database.
    private val database = FirebaseDatabase.getInstance("https://sellr-9c516-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    // Konstanta untuk nama node 'orders' di Firebase.
    private val ordersNode = "orders"
    // Tag untuk logging, mempermudah proses debugging.
    private val TAG = "HistoryActivity_DB_FIX"

    /**
     * Metode ini dipanggil saat Activity pertama kali dibuat.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menginisialisasi objek binding.
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        // Menetapkan layout utama untuk Activity ini.
        setContentView(binding.root)

        Log.i(TAG, "onCreate: Firebase Database URL: ${database.toString()}")

        // Mendapatkan ID unik (UID) dari pengguna yang sedang login.
        val userId = auth.currentUser?.uid
        // Pemeriksaan keamanan: jika tidak ada pengguna yang login, tutup activity.
        if (userId == null) {
            Log.e(TAG, "onCreate gagal: userId null. Pengguna tidak login.")
            Toast.makeText(this, "Sesi berakhir, silakan login kembali.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        Log.d(TAG, "Pengguna saat ini: $userId")

        // Atur listener untuk tombol kembali di toolbar.
        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Tombol kembali (toolbar) diklik.")
            finish()
        }

        // Panggil fungsi untuk menyiapkan RecyclerView.
        setupRecyclerView()
        // Panggil fungsi untuk mengambil data riwayat pesanan dari Firebase.
        fetchOrderHistory(userId)
    }

    /**
     * Menginisialisasi OrderAdapter dan mengkonfigurasi RecyclerView.
     */
    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView")
        // Inisialisasi adapter dengan list data pesanan.
        orderAdapter = OrderAdapter(orderList)
        binding.rvHistory.apply {
            // Atur item agar ditampilkan dalam daftar linear vertikal.
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            // Hubungkan adapter ke RecyclerView.
            adapter = orderAdapter
        }
    }

    /**
     * Mengambil data riwayat pesanan dari Firebase dan mengurutkannya.
     * @param userId UID pengguna untuk menemukan riwayat pesanan yang benar.
     */
    private fun fetchOrderHistory(userId: String) {
        Log.d(TAG, "fetchOrderHistory untuk userId: $userId")
        // Membuat referensi yang menunjuk langsung ke node pesanan milik pengguna.
        val userOrdersRef = database.child(ordersNode).child(userId)
        Log.d(TAG, "Referensi riwayat pesanan pengguna: ${userOrdersRef.toString()}")

        // Mengambil data dan mengurutkannya berdasarkan 'timestamp' secara menaik (ascending).
        // Ini adalah cara Firebase untuk melakukan sorting data sebelum dikirim ke client.
        userOrdersRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            /**
             * Metode ini dipanggil saat data berhasil diambil atau saat ada perubahan.
             * @param snapshot Berisi data dari Firebase.
             */
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i(TAG, "onDataChange untuk riwayat pesanan dipanggil. Snapshot ada: ${snapshot.exists()}, Value: ${snapshot.value}")
                // Kosongkan list lokal terlebih dahulu untuk menghindari duplikasi.
                orderList.clear()
                // Periksa apakah snapshot (data) benar-benar ada.
                if(snapshot.exists()){
                    // Iterasi melalui setiap anak dari snapshot (setiap pesanan).
                    for (orderSnapshot in snapshot.children) {
                        // Firebase mengubah data JSON menjadi objek Kotlin 'Order'.
                        val order = orderSnapshot.getValue(Order::class.java)
                        if (order != null) {
                            Log.d(TAG, "Menambahkan pesanan ke orderList: ID ${order.orderId}")
                            // Tambahkan pesanan yang sudah di-parse ke dalam list lokal.
                            orderList.add(order)
                        } else {
                            Log.w(TAG, "Gagal parse Order dari snapshot: ${orderSnapshot.key}")
                        }
                    }
                } else {
                    Log.d(TAG, "Snapshot riwayat pesanan tidak ada atau kosong.")
                }

                // Membalik urutan list. Karena Firebase mengurutkan dari terlama ke terbaru (ascending),
                // kita membaliknya di sini agar pesanan terbaru (newest) tampil di paling atas.
                orderList.reverse()

                // Beri tahu adapter bahwa dataset telah berubah, agar UI di-refresh.
                orderAdapter.notifyDataSetChanged()

                // Tampilkan atau sembunyikan pesan "riwayat kosong".
                toggleEmptyView()
                Log.d(TAG, "Total pesanan di orderList setelah onDataChange: ${orderList.size}")
            }

            /**
             * Metode ini dipanggil jika terjadi error saat membaca data.
             */
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "fetchOrderHistory dibatalkan: ${error.message}", error.toException())
                Toast.makeText(this@HistoryActivity, "Gagal memuat riwayat: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    /**
     * Mengatur visibilitas elemen UI berdasarkan apakah riwayat pesanan kosong atau tidak.
     */
    private fun toggleEmptyView() {
        if (orderList.isEmpty()) {
            Log.d(TAG, "toggleEmptyView: Riwayat kosong.")
            // Jika kosong, tampilkan teks "Tidak Ada Riwayat".
            binding.tvNoHistory.visibility = View.VISIBLE
            // Sembunyikan RecyclerView.
            binding.rvHistory.visibility = View.GONE
        } else {
            Log.d(TAG, "toggleEmptyView: Riwayat berisi pesanan.")
            // Jika ada isinya, sembunyikan teks.
            binding.tvNoHistory.visibility = View.GONE
            // Tampilkan RecyclerView.
            binding.rvHistory.visibility = View.VISIBLE
        }
    }
}