package com.example.smartshop.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseProductRepository {

    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")

    /**
     * Stream all products from Firestore, optionally filtered by category ("Tech" or "Apparel").
     * Real-time updates push automatically whenever items change in the console.
     */
    fun getProductsByCategory(category: String? = null): Flow<List<Product>> = callbackFlow {
        // Query all products, or filter by specific category if provided
        val query = if (category.isNullOrEmpty()) {
            productsCollection
        } else {
            productsCollection.whereEqualTo("category", category)
        }

        // Attach real-time snapshot listener
        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                // Automatically parse Firestore documents into Product data classes
                val productList = snapshot.toObjects(Product::class.java)
                trySend(productList)
            }
        }

        // Clean up memory listener when the Coroutine scope closes
        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Add a single product to Firestore programmatically
     */
    fun addProduct(product: Product, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val docId = product.id.ifEmpty { productsCollection.document().id }
        val updatedProduct = product.copy(id = docId)

        productsCollection.document(docId)
            .set(updatedProduct)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    /**
     * AUTOMATED BULK SEEDER
     * Call this once to upscale your Firestore database with a full catalog!
     */
    fun seedFullCatalog(onComplete: (Boolean) -> Unit) {
        val batch = db.batch()

        // 🛍️ Tech & Apparel Expanded Catalog Data
        val fullCatalog = listOf(
            // --- TECH CATEGORY ---
            Product("tech_101", "MacBook Air 15-inch M3", "Tech", 134900.0, "INR", listOf("Laptop", "Apple", "M3"), "Ultra-thin laptop with M3 chip.", "Apple", "Laptops", 4.8, true),
            Product("tech_102", "Sony WH-1000XM5 Headphones", "Tech", 29990.0, "INR", listOf("Audio", "Wireless", "ANC"), "Industry leading noise canceling headphones.", "Sony", "Audio", 4.7, true),
            Product("tech_103", "Samsung Galaxy S24 Ultra", "Tech", 129999.0, "INR", listOf("Mobile", "Android", "AI"), "Flagship smartphone with Galaxy AI.", "Samsung", "Mobiles", 4.9, true),
            Product("tech_104", "iPad Air M2 (11-inch)", "Tech", 59900.0, "INR", listOf("Tablet", "Apple"), "Supercharged by M2 processor.", "Apple", "Tablets", 4.6, true),
            Product("tech_105", "Logitech MX Master 3S Mouse", "Tech", 8995.0, "INR", listOf("Accessory", "Wireless"), "Performance ergonomic wireless mouse.", "Logitech", "Accessories", 4.8, true),
            Product("tech_106", "Dell XPS 13 Laptop", "Tech", 114990.0, "INR", listOf("Laptop", "Windows"), "InfinityEdge display premium ultrabook.", "Dell", "Laptops", 4.5, true),
            Product("tech_107", "Apple Watch Series 9", "Tech", 41900.0, "INR", listOf("Wearable", "Smartwatch"), "Smarter, brighter, and more powerful.", "Apple", "Wearables", 4.7, true),

            // --- APPAREL CATEGORY ---
            Product("apparel_101", "Nike Tech Fleece Joggers", "Apparel", 6495.0, "INR", listOf("Streetwear", "Pants"), "Lightweight warmth with tailored feel.", "Nike", "Bottomwear", 4.7, true),
            Product("apparel_102", "Adidas Originals Hoodie", "Apparel", 4999.0, "INR", listOf("Sweatshirt", "Casual"), "Classic trefoil pullover hoodie.", "Adidas", "Topwear", 4.5, true),
            Product("apparel_103", "Levi's 511 Slim Fit Jeans", "Apparel", 3599.0, "INR", listOf("Denim", "Jeans"), "Modern slim-cut classic denim.", "Levi's", "Bottomwear", 4.6, true),
            Product("apparel_104", "Puma Motorsport Jacket", "Apparel", 7999.0, "INR", listOf("Outerwear", "Jacket"), "Sleek racing-inspired bomber jacket.", "Puma", "Outerwear", 4.4, true),
            Product("apparel_105", "Uniqlo Oversized Airism Tee", "Apparel", 1990.0, "INR", listOf("T-Shirt", "Basics"), "Smooth cotton-blend oversized crew neck.", "Uniqlo", "Topwear", 4.8, true),
            Product("apparel_106", "Jordan Retro High Sneakers", "Apparel", 16995.0, "INR", listOf("Footwear", "Sneakers"), "Iconic high-top basketball sneakers.", "Nike", "Footwear", 4.9, true)
        )

        // Queue all items into a single atomic batch upload
        fullCatalog.forEach { product ->
            val docRef = productsCollection.document(product.id)
            batch.set(docRef, product)
        }

        // Commit batch to Firestore
        batch.commit()
            .addOnSuccessListener {
                Log.d("FirestoreSeeder", "Successfully upscaled database with ${fullCatalog.size} items!")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreSeeder", "Error seeding database: ${e.message}")
                onComplete(false)
            }
    }
}