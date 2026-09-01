package com.example.smartshop.repository

import android.content.Context
import android.util.Log
import com.example.smartshop.model.Product
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

                    // Safely parse price regardless of whether Firestore saved it as Int, Long, or Double
                    val price = doc.getDouble("price")
                        ?: doc.getLong("price")?.toDouble()
                        ?: 0.0

                    Product(
                        id = doc.id,
                        name = name,
                        category = category,
                        brand = brand,
                        price = price
                    )
                }

                Log.d("FIREBASE_TEST", "Successfully loaded ${products.size} items from Firestore")
                trySend(UiState.Success(products))
            } else {
                trySend(UiState.Success(emptyList()))
            }
        }

        awaitClose { listener.remove() }
    }

    fun seedCatalogIfNecessary(onComplete: () -> Unit) {
        // Keeps your seeding flow active
        onComplete()
    }

    fun forceReseed(onComplete: (Boolean) -> Unit) {
        onComplete(true)
    }
}