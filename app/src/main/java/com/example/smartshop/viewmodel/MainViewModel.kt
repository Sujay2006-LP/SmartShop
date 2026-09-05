package com.example.smartshop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartshop.BuildConfig
import com.example.smartshop.ai.GeminiRecommendationEngine
import com.example.smartshop.model.*
import com.example.smartshop.repository.FirebaseProductRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val repository = FirebaseProductRepository(application)
    private val recommendationEngine = GeminiRecommendationEngine(BuildConfig.GEMINI_API_KEY)

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    fun signIn(email: String, pword: String) {
        if (email.isEmpty() || pword.isEmpty()) return
        auth.signInWithEmailAndPassword(email, pword)
            .addOnSuccessListener { _currentUser.value = auth.currentUser }
    }

    fun signUp(email: String, pword: String) {
        if (email.isEmpty() || pword.isEmpty()) return
        auth.createUserWithEmailAndPassword(email, pword)
            .addOnSuccessListener { _currentUser.value = auth.currentUser }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
    }

    // ... (rest of states)
    
    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    fun initiateCheckout() {
        if (_cart.value.items.isEmpty()) return
        
        viewModelScope.launch {
            // If we already have a banner suggestion, don't show the dialog again
            if (_recommendation.value != null) {
                _checkoutState.value = CheckoutState.FinalizingPayment
                return@launch
            }

            _checkoutState.value = CheckoutState.Analyzing
            
            // The engine now handles fallback internally to ensure requirements are always met
            val result = recommendationEngine.getCheckoutAddons(_cart.value, _allProducts.value)
            
            when (result) {
                is CheckoutAgentResult.Suggestion -> {
                    logAuditTrail("SUGGESTED", result.suggestion)
                    _checkoutState.value = CheckoutState.Suggesting(result.suggestion)
                }
                is CheckoutAgentResult.Blocked -> {
                    logAuditTrail("BLOCKED_BY_GUARDRAIL", null, result.attemptedProductId, result.reason)
                    // If AI is blocked by safety guardrails, proceed straight to payment to avoid showing bad data
                    _checkoutState.value = CheckoutState.FinalizingPayment
                }
                is CheckoutAgentResult.NoSuggestion -> {
                    _checkoutState.value = CheckoutState.FinalizingPayment
                }
            }
        }
    }

    fun acceptSuggestion(suggestion: CheckoutSuggestion) {
        val product = _allProducts.value.find { it.id == suggestion.productId }
        if (product != null) {
            addToCart(product)
            logAuditTrail("ACCEPTED", suggestion)
        }
        _checkoutState.value = CheckoutState.FinalizingPayment
    }

    fun rejectSuggestion(suggestion: CheckoutSuggestion) {
        logAuditTrail("REJECTED", suggestion)
        _checkoutState.value = CheckoutState.FinalizingPayment
    }

    fun dismissCheckout() {
        _checkoutState.value = CheckoutState.Idle
    }

    private fun logAuditTrail(
        decision: String, 
        suggestion: CheckoutSuggestion? = null,
        blockedId: String? = null,
        blockedReason: String? = null
    ) {
        val aiMode = if (BuildConfig.GEMINI_API_KEY.startsWith("AIza")) "GEMINI_AI" else "LOCAL_FALLBACK"
        val entry = mapOf(
            "cartId" to _cart.value.id,
            "timestamp" to System.currentTimeMillis(),
            "decision" to decision,
            "aiMode" to aiMode,
            "suggestedProductId" to (suggestion?.productId ?: blockedId),
            "aiReasoning" to (suggestion?.reasoning ?: blockedReason),
            "cartTotalBefore" to _cart.value.total_amount,
            "cartTotalAfter" to if (decision == "ACCEPTED" && suggestion != null) 
                _cart.value.total_amount + suggestion.price else _cart.value.total_amount
        )
        
        db.collection("checkout_audit_trail").add(entry)
            .addOnSuccessListener { android.util.Log.d("AUDIT", "Decision logged ($aiMode): $decision") }
            .addOnFailureListener { e -> android.util.Log.e("AUDIT", "Failed to log decision", e) }
    }

    // Catalog & Search States
    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")

    val filteredProducts: StateFlow<List<Product>> = combine(_allProducts, searchQuery, selectedCategory) { products, query, category ->
        products.filter { product ->
            val matchesSearch = product.name.contains(query, ignoreCase = true) ||
                    product.brand.contains(query, ignoreCase = true)
            val matchesCategory = if (category == "All") true else product.category.equals(category, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Cart & Selection States
    private val _cart = MutableStateFlow(Cart("1", "user_1", emptyList(), 0.0))
    val cart: StateFlow<Cart> = _cart.asStateFlow()

    private val _recommendation = MutableStateFlow<CheckoutSuggestion?>(null)
    val recommendation: StateFlow<CheckoutSuggestion?> = _recommendation.asStateFlow()

    private val _isLoadingRec = MutableStateFlow(false)
    val isLoadingRec: StateFlow<Boolean> = _isLoadingRec.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _reviewSummary = MutableStateFlow<String?>(null)
    val reviewSummary: StateFlow<String?> = _reviewSummary.asStateFlow()

    private val _isLoadingSummary = MutableStateFlow(false)
    val isLoadingSummary: StateFlow<Boolean> = _isLoadingSummary.asStateFlow()

    init {
        android.util.Log.d("DIAGNOSTICS", "Gemini Key Prefix: ${BuildConfig.GEMINI_API_KEY.take(5)}")
        observeProducts()
    }

    private fun observeProducts() {
        viewModelScope.launch {
            repository.getProductsStream().collect { state ->
                if (state is UiState.Success) {
                    _allProducts.value = state.data
                    if (_cart.value.items.isNotEmpty()) fetchRecommendation()
                }
            }
        }
    }

    fun fetchProducts() {
        observeProducts()
    }

    fun selectProductById(productId: String) {
        val product = _allProducts.value.find { it.id == productId }
        if (product != null) selectProduct(product)
    }

    fun selectProduct(product: Product) {
        _selectedProduct.value = product
        _reviewSummary.value = null
        viewModelScope.launch {
            _isLoadingSummary.value = true
            val summary = recommendationEngine.summarizeReviews(product)
            android.util.Log.d("VIEWMODEL_AI", "Summary for ${product.name}: $summary")
            _reviewSummary.value = summary
            _isLoadingSummary.value = false
        }
    }

    fun clearSelectedProduct() {
        _selectedProduct.value = null
        _reviewSummary.value = null
    }

    fun addToCartById(productId: String) {
        val product = _allProducts.value.find { it.id == productId }
        if (product != null) addToCart(product)
    }

    fun addToCart(product: Product) {
        val currentItems = _cart.value.items.toMutableList()
        val index = currentItems.indexOfFirst { it.id == product.id }

        if (index != -1) {
            val item = currentItems[index]
            currentItems[index] = item.copy(quantity = item.quantity + 1)
        } else {
            currentItems.add(CartItem(product.id, product.name, 1, product.price))
        }

        val total = currentItems.sumOf { it.price * it.quantity }
        _cart.value = _cart.value.copy(items = currentItems, total_amount = total)

        viewModelScope.launch {
            _snackbarEvent.emit("Added ${product.name} to cart")
        }

        if (currentItems.isNotEmpty()) fetchRecommendation() else _recommendation.value = null
    }

    fun decreaseQuantity(productId: String) {
        val currentItems = _cart.value.items.toMutableList()
        val index = currentItems.indexOfFirst { it.id == productId }

        if (index != -1) {
            val item = currentItems[index]
            if (item.quantity > 1) {
                currentItems[index] = item.copy(quantity = item.quantity - 1)
            } else {
                currentItems.removeAt(index)
            }
            val total = currentItems.sumOf { it.price * it.quantity }
            _cart.value = _cart.value.copy(items = currentItems, total_amount = total)
            
            if (currentItems.isNotEmpty()) fetchRecommendation() else _recommendation.value = null
        }
    }

    fun removeFromCart(productId: String) {
        val currentItems = _cart.value.items.toMutableList()
        val index = currentItems.indexOfFirst { it.id == productId }

        if (index != -1) {
            currentItems.removeAt(index)
            val total = currentItems.sumOf { it.price * it.quantity }
            _cart.value = _cart.value.copy(items = currentItems, total_amount = total)
            
            if (currentItems.isNotEmpty()) fetchRecommendation() else _recommendation.value = null
        }
    }

    fun clearCart() {
        _cart.value = Cart("1", "user_1", emptyList(), 0.0)
        _recommendation.value = null
    }

    private fun fetchRecommendation() {
        if (_cart.value.items.isEmpty()) {
            _recommendation.value = null
            return
        }
        viewModelScope.launch {
            _isLoadingRec.value = true
            _recommendation.value = recommendationEngine.getRecommendation(_cart.value, _allProducts.value)
            _isLoadingRec.value = false
        }
    }
}
