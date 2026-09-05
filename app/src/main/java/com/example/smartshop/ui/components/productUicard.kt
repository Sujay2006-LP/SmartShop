package com.example.smartshop.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.Color
import com.example.smartshop.model.Product

@Composable
fun ProductCard(
    product: Product,
    onAddToCart: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = Color.Black
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image loaded via Coil
            AsyncImage(
                model = product.imageUrl.ifEmpty { "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500" },
                contentDescription = product.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Product Information
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${product.price}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "₹${product.fairPrice}",
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = TextDecoration.LineThrough,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "${product.brand} • ⭐ ${product.rating}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Add Button
            Button(
                onClick = { onAddToCart(product) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add")
            }
        }
    }
}