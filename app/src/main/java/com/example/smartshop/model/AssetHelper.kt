package com.example.smartshop.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AssetHelper(private val context: Context) {
    private val gson = Gson()

    fun loadCatalog(): List<Product> {
        val jsonString = context.assets.open("catalog.json").bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<Product>>() {}.type
        return gson.fromJson(jsonString, listType)
    }

    fun loadCarts(): List<Cart> {
        val jsonString = context.assets.open("carts.json").bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<Cart>>() {}.type
        return gson.fromJson(jsonString, listType)
    }
}