// Mendeklarasikan package tempat file ini berada.
package com.example.sellr

// Mengimpor kelas-kelas yang dibutuhkan untuk fungsionalitas Activity.
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sellr.databinding.ActivityLoginBinding // ViewBinding untuk layout activity_login.xml
import com.google.firebase.auth.FirebaseAuth // Kelas utama dari Firebase Authentication.

/**
 * LoginActivity adalah layar untuk proses otentikasi pengguna.
 * Ia menangani input email/password dan memverifikasinya dengan Firebase.
 */
class LoginActivity : AppCompatActivity() {

    // Properti untuk View Binding, memungkinkan akses aman ke komponen UI.
    private lateinit var binding: ActivityLoginBinding
    // Inisialisasi instance dari Firebase Authentication.
    // Ini adalah titik masuk utama untuk semua operasi otentikasi.
    private val auth = FirebaseAuth.getInstance()

    /**
     * Metode ini dipanggil saat Activity pertama kali dibuat.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menginisialisasi objek binding dan menetapkan layout untuk Activity.
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Pengecekan Sesi Pengguna ---
        // Pengecekan penting: jika 'currentUser' tidak null, berarti pengguna sudah login sebelumnya.
        if (auth.currentUser != null) {
            // Jika sudah login, langsung arahkan ke MainActivity.
            startActivity(Intent(this, MainActivity::class.java))
            // Panggil finish() untuk menghapus LoginActivity dari tumpukan (back stack),
            // sehingga pengguna tidak bisa kembali ke halaman login dengan menekan tombol 'back'.
            finish()
            // Hentikan eksekusi kode lebih lanjut di onCreate agar tidak memasang listener yang tidak perlu.
            return
        }

        // Atur listener untuk tombol login. Saat diklik, panggil fungsi loginUser().
        binding.btnLogin.setOnClickListener { loginUser() }
        // Atur listener untuk teks "Belum punya akun?". Saat diklik, buka SignUpActivity.
        binding.tvGoToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    /**
     * Fungsi yang berisi logika untuk memproses login pengguna.
     */
    private fun loginUser() {
        // Ambil teks dari EditText, dan gunakan .trim() untuk menghapus spasi di awal/akhir.
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validasi input dasar: pastikan email dan password tidak kosong.
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            // Hentikan eksekusi fungsi jika validasi gagal.
            return
        }

        // --- Memberikan Umpan Balik Visual kepada Pengguna ---
        // Tampilkan ProgressBar untuk menandakan bahwa proses sedang berjalan.
        binding.progressBar.visibility = View.VISIBLE
        // Nonaktifkan tombol login untuk mencegah klik berulang kali.
        binding.btnLogin.isEnabled = false

        // Memulai proses login dengan Firebase. Ini adalah operasi asynchronous (tidak langsung selesai).
        auth.signInWithEmailAndPassword(email, password)
            // Tambahkan listener yang akan dieksekusi saat proses login selesai (baik berhasil maupun gagal).
            .addOnCompleteListener(this) { task ->
                // Sembunyikan kembali ProgressBar setelah proses selesai.
                binding.progressBar.visibility = View.GONE
                // Aktifkan kembali tombol login.
                binding.btnLogin.isEnabled = true

                // Periksa apakah tugas (task) login berhasil.
                if (task.isSuccessful) {
                    // Jika berhasil, tampilkan pesan sukses.
                    Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show()
                    // Buat Intent untuk pindah ke MainActivity.
                    val intent = Intent(this, MainActivity::class.java)
                    // Atur Flags: menghapus semua activity sebelumnya dari tumpukan (back stack).
                    // Ini penting agar pengguna tidak bisa kembali ke halaman login setelah berhasil masuk.
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    // Tutup LoginActivity.
                    finish()
                } else {
                    // Jika gagal, tampilkan pesan error yang didapat dari Firebase.
                    // 'task.exception?.message' berisi detail penyebab kegagalan (misal: password salah, user tidak ditemukan).
                    Toast.makeText(this, "Login Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}