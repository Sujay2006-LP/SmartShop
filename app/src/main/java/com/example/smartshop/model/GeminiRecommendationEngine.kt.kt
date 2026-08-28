package com.example.smartshop.model

import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson

class GeminiRecommendationEngine(private val apiKey: String) {

    // Using the active Gemini model
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    suspend fun getRecommendation(cart: Cart, catalog: List<Product>): String {
        val gson = Gson()
        val cartJson = gson.toJson(cart)
        val catalogJson = gson.toJson(catalog)

        val prompt = """
            You are an AI e-commerce assistant. 
            Here is the product catalog:
            $catalogJson

            Here is the user's current shopping cart:
            $cartJson

            Based on the items in the cart, suggest ONE product from the catalog that best complements what the user is buying.
            Provide a 2-sentence reason explaining why this item complements their cart.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "No recommendation available."
        } catch (e: Exception) {
            "Error fetching recommendation: ${e.localizedMessage}"
        }
    }
}