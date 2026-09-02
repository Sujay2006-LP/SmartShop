package com.example.smartshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartshop.model.UiState
import com.example.smartshop.ui.components.ProductCard
import com.example.smartshop.ui.components.ProductDetailSheet
import com.example.smartshop.ui.theme.SmartShopTheme
import com.example.smartshop.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SmartShopTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmartShopScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartShopScreen(viewModel: MainViewModel) {
    val productState by viewModel.productState.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val recommendation by viewModel.recommendation.collectAsState()
    val isLoadingRec by viewModel.isLoadingRec.collectAsState()

    // States for Product Detail & Gemini Review Summarizer Sheet
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val reviewSummary by viewModel.reviewSummary.collectAsState()
    val isLoadingSummary by viewModel.isLoadingSummary.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SmartShop", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 1. AI Recommendation Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "✨ Recommended for Your Setup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isLoadingRec) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = recommendation ?: "Add items to your cart for AI suggestions!",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 2. Catalog Section Header
            Text(
                text = "Catalog Products",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // 3. Product Catalog Area with Coil ProductCard UI & Click Action
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (val currentState = productState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is UiState.Error -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "❌ Firestore Error",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.fetchProducts() }) {
                                Text("Retry Connection")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.forceReseed() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text("Force Reset & Seed Data")
                            }
                        }
                    }
                    is UiState.Success -> {
                        val products = currentState.data

                        if (products.isEmpty()) {
                            Text(
                                text = "No products found in catalog.",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(products) { product ->
                                    ProductCard(
                                        product = product,
                                        onAddToCart = { item ->
                                            viewModel.addToCart(item)
                                        },
                                        modifier = Modifier.clickable {
                                            viewModel.selectProduct(product)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Cart & Checkout Footer
            Surface(
                tonalElevation = 4.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cart Total (${cart.items.sumOf { it.quantity }} items): ₹${cart.total_amount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { /* Razorpay trigger coming in Day 5! */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Proceed to Pay with Razorpay")
                    }
                }
            }
        }

        // 5. Product Details & Gemini Review Summary Bottom Sheet
        selectedProduct?.let { product ->
            ProductDetailSheet(
                product = product,
                aiSummary = reviewSummary,
                isLoadingSummary = isLoadingSummary,
                onDismiss = { viewModel.clearSelectedProduct() },
                onAddToCart = { item -> viewModel.addToCart(item) }
            )
        }
    }
}