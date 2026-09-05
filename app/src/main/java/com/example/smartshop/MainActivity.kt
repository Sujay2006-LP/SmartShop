package com.example.smartshop

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartshop.model.CheckoutState
import com.example.smartshop.model.Product
import com.example.smartshop.model.UiState
import com.example.smartshop.ui.components.CartSheet
import com.example.smartshop.ui.components.LogoView
import com.example.smartshop.ui.components.ProductCard
import com.example.smartshop.ui.components.ProductDetailSheet
import com.example.smartshop.ui.screens.LoginScreen
import com.example.smartshop.ui.theme.SmartShopTheme
import com.example.smartshop.ui.theme.IndigoPrimary
import com.example.smartshop.ui.theme.PureWhite
import com.example.smartshop.ui.theme.DeepBlack
import com.example.smartshop.ui.theme.SuccessGreen
import com.example.smartshop.viewmodel.MainViewModel
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject

class MainActivity : ComponentActivity(), PaymentResultListener {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Checkout.preload(applicationContext)

        setContent {
            SmartShopTheme {
                val user by viewModel.currentUser.collectAsState()
                val checkoutState by viewModel.checkoutState.collectAsState()
                val cart by viewModel.cart.collectAsState()

                // Trigger Razorpay when AI Agent flow is finished
                LaunchedEffect(checkoutState) {
                    if (checkoutState is CheckoutState.FinalizingPayment) {
                        startRazorpayPayment(cart.total_amount)
                        viewModel.dismissCheckout()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (user == null) {
                        LoginScreen(
                            onLoginSuccess = { email, pword -> viewModel.signIn(email, pword) },
                            onSignUpSuccess = { email, pword -> viewModel.signUp(email, pword) }
                        )
                    } else {
                        SmartShopScreen(
                            viewModel = viewModel,
                            onProceedToCheckout = { viewModel.initiateCheckout() }
                        )

                        // AI Agent Suggestion Dialog removed to avoid redundancy with banner
                        // FinalizingPayment handles startRazorpayPayment via LaunchedEffect
                    }
                }
            }
        }
    }

    private fun startRazorpayPayment(amount: Double) {
        if (amount <= 0) return
        val checkout = Checkout()
        checkout.setKeyID(BuildConfig.RAZORPAY_KEY_ID)
        try {
            val user = viewModel.currentUser.value
            val options = JSONObject().apply {
                put("name", "SmartShop Setup Store")
                put("description", "Premium Tech Accessories")
                put("currency", "INR")
                put("amount", (amount * 100).toLong()) // Amount in paise
                put("prefill", JSONObject().apply {
                    put("email", user?.email ?: "customer@example.com")
                    put("contact", "9999999999")
                })
                put("theme", JSONObject().apply {
                    put("color", "#6366F1")
                })
            }
            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        Toast.makeText(this, "Payment Successful: $razorpayPaymentId", Toast.LENGTH_LONG).show()
        viewModel.clearCart()
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed ($code): $response", Toast.LENGTH_LONG).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartShopScreen(
    viewModel: MainViewModel,
    onProceedToCheckout: () -> Unit
) {
    val products by viewModel.filteredProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val recommendation by viewModel.recommendation.collectAsState()
    val isLoadingRec by viewModel.isLoadingRec.collectAsState()

    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val reviewSummary by viewModel.reviewSummary.collectAsState()
    val isLoadingSummary by viewModel.isLoadingSummary.collectAsState()
    
    var showCartSheet by remember { mutableStateOf(false) }

    val categories = listOf("All", "Mobile", "Laptop", "Audio", "Accessories")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(modifier = Modifier.background(
                Brush.linearGradient(
                    colors = listOf(IndigoPrimary, Color(0xFF4F46E5))
                )
            )) {
                TopAppBar(
                    title = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LogoView(size = 32.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SmartShop", fontWeight = FontWeight.Bold)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showCartSheet = true }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = PureWhite)
                        }
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = PureWhite)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = PureWhite,
                        actionIconContentColor = PureWhite
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 1. Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Category Chips Filter
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { viewModel.selectedCategory.value = category },
                        label = { Text(category) }
                    )
                }
            }

            // 3. AI Cart Recommendation Banner
            if (cart.items.isNotEmpty()) {
                recommendation?.let { suggestion ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("✨ AI Consultant Suggestion", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            if (isLoadingRec) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp).padding(top = 4.dp))
                            } else {
                                Text(suggestion.reasoning, style = MaterialTheme.typography.bodyMedium)
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Tap to Explore",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { 
                                            viewModel.selectProductById(suggestion.productId)
                                        }
                                    )

                                    Button(
                                        onClick = { viewModel.addToCartById(suggestion.productId) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Add to Cart", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Product Catalog
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        onAddToCart = { viewModel.addToCart(it) },
                        modifier = Modifier.clickable { viewModel.selectProduct(product) }
                    )
                }
            }

            // 5. Checkout Footer
            if (cart.items.isNotEmpty()) {
                Surface(
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Items in Cart", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        cart.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.decreaseQuantity(item.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = IndigoPrimary)
                                    }
                                    Text(
                                        "${item.quantity}", 
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { 
                                            val product = products.find { it.id == item.id }
                                            if (product != null) viewModel.addToCart(product)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = IndigoPrimary)
                                    }
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // Available Offers Section
                        recommendation?.let { suggestion ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFB923C)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🎁", modifier = Modifier.padding(end = 8.dp))
                                    Column {
                                        Text("Available Offers", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall, color = Color(0xFFC2410C))
                                        Text(suggestion.bankOffer, style = MaterialTheme.typography.bodySmall, color = Color.Black)
                                        Text("✨ Lucky Customer Extra Applied", style = MaterialTheme.typography.labelSmall, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cart Total:", fontWeight = FontWeight.Bold)
                            Text("₹${cart.total_amount}", fontWeight = FontWeight.ExtraBold, color = IndigoPrimary)
                        }

                        Button(
                            onClick = { onProceedToCheckout() },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            enabled = cart.items.isNotEmpty(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Proceed to Pay with Razorpay")
                        }
                    }
                }
            }
        }

        // Product Detail & Review Summary Sheet
        selectedProduct?.let { product ->
            ProductDetailSheet(
                product = product,
                aiSummary = reviewSummary,
                isLoadingSummary = isLoadingSummary,
                onDismiss = { viewModel.clearSelectedProduct() },
                onAddToCart = { viewModel.addToCart(it) }
            )
        }

        // Cart Management Sheet
        if (showCartSheet) {
            val allProducts by viewModel.filteredProducts.collectAsState()
            CartSheet(
                cart = cart,
                allProducts = allProducts,
                onIncrease = { viewModel.addToCart(it) },
                onDecrease = { viewModel.decreaseQuantity(it) },
                onRemove = { viewModel.removeFromCart(it) },
                onDismiss = { showCartSheet = false }
            )
        }
    }
}