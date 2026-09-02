package com.example.smartshop.repository

import android.content.Context
import android.util.Log
import com.example.smartshop.model.Product
import com.example.smartshop.model.Review
import com.example.smartshop.model.UiState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseProductRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")

    fun getProductsStream(): Flow<UiState<List<Product>>> = callbackFlow {
        trySend(UiState.Loading)

        val listener = productsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FIREBASE_TEST", "Firestore error: ${error.message}", error)
                trySend(UiState.Error(error.localizedMessage ?: "Failed to fetch data"))
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val products = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: ""
                    val category = doc.getString("category") ?: "General"
                    val brand = doc.getString("brand") ?: ""
                    val description = doc.getString("description") ?: ""
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val price = doc.getDouble("price")
                        ?: doc.getLong("price")?.toDouble()
                        ?: 0.0
                    val rating = doc.getDouble("rating")
                        ?: doc.getLong("rating")?.toDouble()
                        ?: 4.5

                    // Parse nested review objects if present
                    val rawReviews = doc.get("reviews") as? List<Map<String, Any>> ?: emptyList()
                    val reviews = rawReviews.map { map ->
                        Review(
                            userId = map["userId"] as? String ?: "",
                            userName = map["userName"] as? String ?: "Anonymous",
                            rating = (map["rating"] as? Number)?.toDouble() ?: 5.0,
                            comment = map["comment"] as? String ?: ""
                        )
                    }

                    Product(
                        id = doc.id,
                        name = name,
                        category = category,
                        brand = brand,
                        description = description,
                        price = price,
                        rating = rating,
                        imageUrl = imageUrl,
                        reviews = reviews
                    )
                }
                trySend(UiState.Success(products))
            } else {
                trySend(UiState.Success(emptyList()))
            }
        }

        awaitClose { listener.remove() }
    }

    fun seedCatalogIfNecessary(onComplete: () -> Unit) {
        onComplete()
    }

    fun forceReseed(onComplete: (Boolean) -> Unit) {
        onComplete(true)
    }
}