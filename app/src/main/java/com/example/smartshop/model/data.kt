package com.example.smartshop.model

data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val currency: String = "INR",
    val tags: List<String> = emptyList(),
    val description: String = "",
    val brand: String = "",
    val subCategory: String = "",
    val rating: Double = 4.5,
    val inStock: Boolean = true
)

data class CartItem(
    val id: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0
)

data class Cart(
    val cart_id: String = "",
    val user_id: String = "",
    val items: List<CartItem> = emptyList(),
    val total_amount: Double = 0.0
)