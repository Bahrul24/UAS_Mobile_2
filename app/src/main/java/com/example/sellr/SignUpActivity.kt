// Mendeklarasikan package tempat file ini berada.
package com.example.sellr

// Mengimpor kelas-kelas yang dibutuhkan untuk fungsionalitas Activity.
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sellr.databinding.ActivitySignUpBinding // ViewBinding untuk layout activity_sign_up.xml
import com.google.firebase.auth.FirebaseAuth // Kelas utama dari Firebase Authentication.

/**
 * SignUpActivity adalah layar untuk proses pendaftaran pengguna baru.
 * Ia menangani input email/password dan membuat akun baru menggunakan Firebase.
 */
class SignUpActivity : AppCompatActivity() {

    // Properti untuk View Binding, memungkinkan akses aman ke komponen UI.
    private lateinit var binding: ActivitySignUpBinding
    // Inisialisasi instance dari Firebase Authentication.
    private val auth = FirebaseAuth.getInstance()

    /**
     * Metode ini dipanggil saat Activity pertama kali dibuat.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menginisialisasi objek binding dan menetapkan layout untuk Activity.
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Atur listener untuk tombol daftar. Saat diklik, panggil fungsi signUpUser().
        binding.btnSignUp.setOnClickListener { signUpUser() }
        // Atur listener untuk teks "Sudah punya akun?". Saat diklik, tutup Activity ini
        // untuk kembali ke LoginActivity.
        binding.tvGoToLogin.setOnClickListener { finish() }
    }

    /**
     * Fungsi yang berisi logika untuk memproses pendaftaran pengguna baru.
     */
    private fun signUpUser() {
        // Ambil teks dari EditText, dan gunakan .trim() untuk menghapus spasi di awal/akhir.
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // --- Validasi Input Pengguna ---
        // Pastikan email dan password tidak kosong.
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return // Hentikan eksekusi fungsi jika validasi gagal.
        }
        // Firebase mensyaratkan password minimal 6 karakter.
        if (password.length < 6) {
            Toast.makeText(this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show()
            return // Hentikan eksekusi fungsi.
        }

        // --- Memberikan Umpan Balik Visual kepada Pengguna ---
        // Tampilkan ProgressBar untuk menandakan bahwa proses sedang berjalan.
        binding.progressBar.visibility = View.VISIBLE
        // Nonaktifkan tombol daftar untuk mencegah klik berulang kali.
        binding.btnSignUp.isEnabled = false

        // Memulai proses pembuatan akun dengan Firebase. Ini adalah operasi asynchronous.
        auth.createUserWithEmailAndPassword(email, password)
            // Tambahkan listener yang akan dieksekusi saat proses selesai (baik berhasil maupun gagal).
            .addOnCompleteListener(this) { task ->
                // Sembunyikan kembali ProgressBar setelah proses selesai.
                binding.progressBar.visibility = View.GONE
                // Aktifkan kembali tombol daftar.
                binding.btnSignUp.isEnabled = true

                // Periksa apakah tugas (task) pendaftaran berhasil.
                if (task.isSuccessful) {
                    // Jika berhasil, tampilkan pesan sukses yang informatif.
                    Toast.makeText(this, "Pendaftaran Berhasil, silakan login", Toast.LENGTH_LONG).show()
                    // Tutup SignUpActivity untuk kembali ke LoginActivity.
                    finish()
                } else {
                    // Jika gagal, tampilkan pesan error yang didapat dari Firebase.
                    // 'task.exception?.message' berisi detail penyebab kegagalan (misal: email sudah terdaftar, format email salah).
                    Toast.makeText(this, "Pendaftaran Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}