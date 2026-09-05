package com.example.smartshop.model

data class Review(
    val userName: String = "",
    val rating: Double = 0.0,
    val comment: String = ""
)

data class Product(
    val id: String = "",
    val name: String = "",
    val brand: String = "",
    val category: String = "", // e.g., "Mobile", "Laptop", "Audio", "Accessories"
    val price: Double = 0.0,
    val fairPrice: Double = 0.0,
    val rating: Double = 0.0,
    val imageUrl: String = "",
    val description: String = "",
    val reviews: List<Review> = emptyList()
)

data class CartItem(
    val id: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0
)

data class Cart(
    val id: String = "",
    val user_id: String = "",
    val items: List<CartItem> = emptyList(),
    val total_amount: Double = 0.0
)
