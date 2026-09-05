package com.example.smartshop.repository

import android.content.Context
import android.util.Log
import com.example.smartshop.data.CatalogData
import com.example.smartshop.model.Product
import com.example.smartshop.model.Review
import com.example.smartshop.model.UiState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseProductRepository(context: Context) {

    private val db = FirebaseFirestore.getInstance().apply {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        firestoreSettings = settings
    }
    private val productsCollection = db.collection("products")
    private val prefs = context.getSharedPreferences("smartshop_prefs", Context.MODE_PRIVATE)

    fun getProductsStream(): Flow<UiState<List<Product>>> = callbackFlow {
        trySend(UiState.Loading)

        // Seed data automatically on first stream request if local prefs say so
        if (!prefs.getBoolean("is_data_seeded", false)) {
            seedCatalogIfNecessary { /* continue */ }
        }

        val listener = productsCollection
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
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
                        val fairPrice = doc.getDouble("fairPrice")
                            ?: doc.getLong("fairPrice")?.toDouble()
                            ?: price
                        val rating = doc.getDouble("rating")
                            ?: doc.getLong("rating")?.toDouble()
                            ?: 4.5

                        val rawReviews = doc.get("reviews") as? List<Map<String, Any>> ?: emptyList()
                        val reviews = rawReviews.map { map ->
                            Review(
                                userName = map["userName"] as? String ?: "Anonymous",
                                rating = (map["rating"] as? Number)?.toDouble() ?: 5.0,
                                comment = map["comment"] as? String ?: ""
                            )
                        }

                        Product(
                            id = doc.id,
                            name = name,
                            brand = brand,
                            category = category,
                            price = price,
                            fairPrice = fairPrice,
                            rating = rating,
                            imageUrl = imageUrl,
                            description = description,
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
        val isSeeded = prefs.getBoolean("is_data_seeded", false)
        if (isSeeded) {
            onComplete()
            return
        }

        val batch = db.batch()
        // Only seed a small essential list (first 10 items) instead of 100+
        val initialProducts = CatalogData.get100Products().take(50)

        initialProducts.forEach { product ->
            val docRef = productsCollection.document(product.id)
            batch.set(docRef, product)
        }

        batch.commit()
            .addOnSuccessListener {
                prefs.edit().putBoolean("is_data_seeded", true).apply()
                onComplete()
            }
            .addOnFailureListener {
                onComplete()
            }
    }

    fun forceReseed(onComplete: (Boolean) -> Unit) {
        prefs.edit().putBoolean("is_data_seeded", false).apply()
        seedCatalogIfNecessary { onComplete(true) }
    }
}
