package com.example.smartshop.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smartshop.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailSheet(
    product: Product,
    aiSummary: String?,
    isLoadingSummary: Boolean,
    onDismiss: () -> Unit,
    onAddToCart: (Product) -> Unit
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
            AsyncImage(
                model = product.imageUrl.ifEmpty { "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500" },
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = product.name, 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "₹${product.price}", 
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Fair Price: ₹${product.fairPrice}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = product.description, 
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            // AI Review Summary Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "✨ AI Review Insights",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isLoadingSummary) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                    } else {
                        Text(
                            text = aiSummary ?: "Loading expert review summary...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reviews List
            Text(text = "Customer Reviews", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(max = 150.dp)
            ) {
                items(product.reviews) { review ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(text = "${review.userName} - ⭐ ${review.rating}", fontWeight = FontWeight.SemiBold)
                        Text(text = review.comment, style = MaterialTheme.typography.bodySmall)
                        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onAddToCart(product)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add to Cart (₹${product.price})")
            }
        }
    }
}