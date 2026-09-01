package com.example.smartshop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartshop.BuildConfig
import com.example.smartshop.util.AssetHelper
import com.example.smartshop.model.Cart
import com.example.smartshop.model.CartItem
import com.example.smartshop.ai.GeminiRecommendationEngine
import com.example.smartshop.model.Product
import com.example.smartshop.model.UiState
import com.example.smartshop.repository.FirebaseProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val assetHelper = AssetHelper(application)
    private val recommendationEngine = GeminiRecommendationEngine(BuildConfig.GEMINI_API_KEY)

    // 1. Initialize Firestore Repository
    private val repository = FirebaseProductRepository(application)

    // 2. StateFlow for Firestore Products State (Loading, Success, Error)
    private val _productState = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val productState: StateFlow<UiState<List<Product>>> = _productState.asStateFlow()

    // 3. Keep local reference to fetched catalog for Gemini recommendations
    private var currentCatalog: List<Product> = emptyList()

    // 4. Cart & Recommendation States
    private val _cart = MutableStateFlow(
        assetHelper.loadCarts().firstOrNull() ?: Cart("1", "user_1", emptyList(), 0.0)
    )
    val cart: StateFlow<Cart> = _cart.asStateFlow()

    private val _recommendation = MutableStateFlow<String?>(null)
    val recommendation: StateFlow<String?> = _recommendation.asStateFlow()

    private val _isLoadingRec = MutableStateFlow(false)
    val isLoadingRec: StateFlow<Boolean> = _isLoadingRec.asStateFlow()

    init {
        // Direct call guarantees Firestore snapshot listener connects immediately
        observeProducts()

        // Run catalog seeding separately without blocking observation
        repository.seedCatalogIfNecessary {
            // Optional callback after seeding
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
}