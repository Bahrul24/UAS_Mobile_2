import android.os.Parcelable
import com.example.sellr.FoodItem
import kotlinx.parcelize.Parcelize

// Data class yang merepresentasikan item dalam keranjang
// Parcelable memungkinkan objek ini dikirim antar komponen Android (Activity/Fragment)
@Parcelize
data class CartItem(
    val foodItem: FoodItem = FoodItem(), // Objek FoodItem yang dibeli
    var quantity: Int = 0 // Jumlah item dalam keranjang
) : Parcelable {
    // Konstruktor kosong untuk keperluan deserialisasi Firebase
    constructor() : this(FoodItem(), 0)
}
