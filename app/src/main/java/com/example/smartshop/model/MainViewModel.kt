package com.example.smartshop.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.smartshop.BuildConfig

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val assetHelper = AssetHelper(application)

    private val recommendationEngine = GeminiRecommendationEngine(BuildConfig.GEMINI_API_KEY)


    val catalog: List<Product> = assetHelper.loadCatalog()

    private val _cart = MutableStateFlow(assetHelper.loadCarts().firstOrNull() ?: Cart("1", "user_1", emptyList(), 0.0))
    val cart: StateFlow<Cart> = _cart.asStateFlow()

    private val _recommendation = MutableStateFlow<String?>(null)
    val recommendation: StateFlow<String?> = _recommendation.asStateFlow()

    private val _isLoadingRec = MutableStateFlow(false)
    val isLoadingRec: StateFlow<Boolean> = _isLoadingRec.asStateFlow()

    init {
        fetchRecommendation()
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
        if (_cart.value.items.isEmpty()) return

        viewModelScope.launch {
            _isLoadingRec.value = true
            val result = recommendationEngine.getRecommendation(_cart.value, catalog)
            _recommendation.value = result
            _isLoadingRec.value = false
        }
    }
}