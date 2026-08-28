package com.example.smartshop.model

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Int,
    val currency: String,
    val tags: List<String>,
    val description: String
)

data class CartItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Int
)

data class Cart(
    val cart_id: String,
    val user_id: String,
    val items: List<CartItem>,
    val total_amount: Int
)