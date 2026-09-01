package com.example.smartshop.util

import android.content.Context
import android.util.Log
import com.example.smartshop.model.Cart
import com.example.smartshop.model.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AssetHelper(private val context: Context) {
    private val gson = Gson()

    fun loadCatalog(): List<Product> {
        return try {
            val jsonString = context.assets.open("catalog.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Product>>() {}.type
            gson.fromJson(jsonString, listType) ?: emptyList()
        } catch (e: Exception) {
            Log.e("AssetHelper", "catalog.json not found, skipping local seed: ${e.message}")
            emptyList()
        }
    }

    fun loadCarts(): List<Cart> {
        return try {
            val jsonString = context.assets.open("carts.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Cart>>() {}.type
            gson.fromJson(jsonString, listType) ?: emptyList()
        } catch (e: Exception) {
            Log.e("AssetHelper", "carts.json not found, using empty cart: ${e.message}")
            emptyList()
        }
    }
}