package com.example.smartshop.repository

import android.content.Context
import android.util.Log
import com.example.smartshop.model.Product
import com.example.smartshop.model.UiState
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseProductRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance().apply {
        // DIAGNOSTICS: Disable persistence to bypass any "Database not found" cache errors
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
        firestoreSettings = settings
    }
    
    // SUSPECTED FIX: The console URL shows "products%20" (a space). 
    private var collectionName = "products" 
    private val productsCollection = db.collection(collectionName)

    fun forceReseed(onComplete: (Boolean) -> Unit) {
        Log.d("FIREBASE_TEST", "🔄 Force re-seeding requested...")
        prefs.edit().putBoolean("is_data_seeded", false).apply()
        seedCatalogIfNecessary(onComplete)
    }

    init {
        val appId = FirebaseApp.getInstance().options.projectId
        Log.d("FIREBASE_DIAG", "Connected to Project ID: $appId")
    }
    private val prefs = context.getSharedPreferences("smartshop_prefs", Context.MODE_PRIVATE)

    // EDGE CASE: Seed only ONCE to avoid overwriting or creating duplicates every app launch
    fun seedCatalogIfNecessary(onComplete: (Boolean) -> Unit) {
        val isSeeded = prefs.getBoolean("is_data_seeded", false)

        if (isSeeded) {
            Log.d("FIREBASE_TEST", "⏩ Catalog already seeded. Skipping batch write.")
            onComplete(true)
            return
        }

        Log.d("FIREBASE_TEST", "🚀 First run detected. Seeding full catalog...")
        val batch = db.batch()

        val initialProducts = listOf(
            Product(id = "p1", name = "Wireless Mouse", category = "Electronics", price = 799.0),
            Product(id = "p2", name = "Mechanical Keyboard", category = "Electronics", price = 2499.0),
            Product(id = "p3", name = "Gaming Monitor", category = "Electronics", price = 12999.0),
            Product(id = "p4", name = "Desk Mat", category = "Accessories", price = 499.0)
        )

        initialProducts.forEach { product ->
            val docRef = productsCollection.document(product.id)
            batch.set(docRef, product)
        }

        batch.commit()
            .addOnSuccessListener {
                Log.d("FIREBASE_TEST", "✅ SEED SUCCESSFUL!")
                prefs.edit().putBoolean("is_data_seeded", true).apply()
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE_TEST", "❌ SEED FAILED: ${e.localizedMessage}", e)
                onComplete(false)
            }
    }

    // EDGE CASE: Real-time Snapshot Listener wrapped inside Kotlin Flow with Exception Handling
    fun getProductsStream(): Flow<UiState<List<Product>>> = callbackFlow {
        trySend(UiState.Loading)

        val listener = productsCollection
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FIREBASE_TEST", "Firestore error: ${error.message}")
                    trySend(UiState.Error(error.localizedMessage ?: "Failed to fetch data"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val products = snapshot.toObjects(Product::class.java)
                    Log.d("FIREBASE_TEST", "Found ${products.size} products in '$collectionName'")
                    snapshot.documents.forEach { doc ->
                        Log.d("FIREBASE_TEST", "Doc ID: ${doc.id}, data: ${doc.data}")
                    }
                    trySend(UiState.Success(products))
                } else {
                    trySend(UiState.Success(emptyList()))
                }
            }

        // Clean up the listener when the Flow collector is cancelled
        awaitClose { listener.remove() }
    }
}