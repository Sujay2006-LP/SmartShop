package com.example.smartshop.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartshop.model.Cart
import com.example.smartshop.model.Product
import com.example.smartshop.ui.theme.IndigoPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartSheet(
    cart: Cart,
    allProducts: List<Product>,
    onIncrease: (Product) -> Unit,
    onDecrease: (String) -> Unit,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Your Cart",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            if (cart.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Your cart is empty", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f, fill = false).heightIn(max = 400.dp)) {
                    items(cart.items) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("₹${item.price} each", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onDecrease(item.id) }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = IndigoPrimary)
                                }
                                Text("${item.quantity}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(onClick = { 
                                    val product = allProducts.find { it.id == item.id }
                                    if (product != null) onIncrease(product)
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = IndigoPrimary)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { onRemove(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Amount", fontWeight = FontWeight.Bold)
                Text("₹${cart.total_amount}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = IndigoPrimary)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
