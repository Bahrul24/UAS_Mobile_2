import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Data class untuk merepresentasikan makanan di aplikasi
// Digunakan baik untuk menampilkan makanan maupun menyimpannya ke Firebase
@Parcelize
data class FoodItem(
    val id: String = "", // ID unik makanan (digunakan sebagai key di database)
    val name: String = "", // Nama makanan
    val price: Long = 0, // Harga dalam satuan rupiah
    val imageUrl: String = "" // URL gambar dari makanan
) : Parcelable
