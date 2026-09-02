package com.example.smartshop.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartshop.BuildConfig
import com.example.smartshop.ai.GeminiRecommendationEngine
import com.example.smartshop.model.Cart
import com.example.smartshop.model.CartItem
import com.example.smartshop.model.Product
import com.example.smartshop.model.UiState
import com.example.smartshop.repository.FirebaseProductRepository
import com.example.smartshop.util.AssetHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val assetHelper = AssetHelper(application)
    private val recommendationEngine = GeminiRecommendationEngine(BuildConfig.GEMINI_API_KEY)

    // 1. Initialize Firebase Auth
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // 2. Initialize Firestore Repository
    private val repository = FirebaseProductRepository(application)

    // 3. StateFlow for Firestore Products State (Loading, Success, Error)
    private val _productState = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val productState: StateFlow<UiState<List<Product>>> = _productState.asStateFlow()

    // 4. Keep local reference to fetched catalog for Gemini recommendations
    private var currentCatalog: List<Product> = emptyList()

    // 5. Cart & Recommendation States
    private val _cart = MutableStateFlow(
        assetHelper.loadCarts().firstOrNull() ?: Cart("1", "user_1", emptyList(), 0.0)
    )
    val cart: StateFlow<Cart> = _cart.asStateFlow()

    private val _recommendation = MutableStateFlow<String?>(null)
    val recommendation: StateFlow<String?> = _recommendation.asStateFlow()

    private val _isLoadingRec = MutableStateFlow(false)
    val isLoadingRec: StateFlow<Boolean> = _isLoadingRec.asStateFlow()

    // 6. Product Detail Bottom Sheet & Review Summarizer States
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _reviewSummary = MutableStateFlow<String?>(null)
    val reviewSummary: StateFlow<String?> = _reviewSummary.asStateFlow()

    private val _isLoadingSummary = MutableStateFlow(false)
    val isLoadingSummary: StateFlow<Boolean> = _isLoadingSummary.asStateFlow()

    init {
        // Authenticate user anonymously on app startup
        signInAnonymously()

        // Direct call guarantees Firestore snapshot listener connects immediately
        observeProducts()

        // Run catalog seeding separately without blocking observation
        repository.seedCatalogIfNecessary {
            // Optional callback after seeding
        }
    }

    private fun signInAnonymously() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    Log.d("AUTH", "Signed in anonymously with UID: $uid")
                    // Update Cart user_id with the authenticated Firebase UID
                    _cart.value = _cart.value.copy(user_id = uid)
                } else {
                    Log.e("AUTH", "Anonymous authentication failed", task.exception)
                }
            }
        } else {
            val uid = auth.currentUser?.uid ?: ""
            Log.d("AUTH", "Already authenticated with UID: $uid")
            _cart.value = _cart.value.copy(user_id = uid)
        }
    }

    private fun observeProducts() {
        viewModelScope.launch {
            repository.getProductsStream().collect { state ->
                _productState.value = state
                if (state is UiState.Success) {
                    currentCatalog = state.data
                    fetchRecommendation()
                }
            }
        }
    }

    fun fetchProducts() {
        observeProducts()
    }

    fun forceReseed() {
        repository.forceReseed { success ->
            if (success) {
                observeProducts()
            }
        }
    }

    fun addToCart(product: Product) {
        val currentItems = _cart.value.items.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.id == product.id }

        if (existingIndex != -1) {
            val existing = currentItems[existingIndex]
            currentItems[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentItems.add(CartItem(product.id, product.name, 1, product.price))
        }

        val newTotal = currentItems.sumOf { it.price * it.quantity }
        _cart.value = _cart.value.copy(items = currentItems, total_amount = newTotal)

        fetchRecommendation()
    }

    private fun fetchRecommendation() {
        if (_cart.value.items.isEmpty() || currentCatalog.isEmpty()) return

        viewModelScope.launch {
            try {
                _isLoadingRec.value = true
                val result = recommendationEngine.getRecommendation(_cart.value, currentCatalog)
                _recommendation.value = result
            } catch (e: Exception) {
                _recommendation.value = "Add items to your cart for recommendations!"
            } finally {
                _isLoadingRec.value = false
            }
        }
    }

    // Detail Bottom Sheet Selection & Review Summarizer triggers
    fun selectProduct(product: Product) {
        _selectedProduct.value = product
        _reviewSummary.value = null
        fetchReviewSummary(product)
    }

    fun clearSelectedProduct() {
        _selectedProduct.value = null
        _reviewSummary.value = null
    }

    private fun fetchReviewSummary(product: Product) {
        viewModelScope.launch {
            _isLoadingSummary.value = true
            try {
                _reviewSummary.value = recommendationEngine.summarizeReviews(product.reviews)
            } catch (e: Exception) {
                _reviewSummary.value = "Review summary unavailable."
            } finally {
                _isLoadingSummary.value = false
            }
        }
    }
}