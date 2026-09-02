package com.example.smartshop.ai

import android.util.Log
import com.example.smartshop.model.Cart
import com.example.smartshop.model.Product
import com.example.smartshop.model.Review
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson

class GeminiRecommendationEngine(private val apiKey: String) {

    // Using the active Gemini model
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
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
            Log.e("GEMINI_ENGINE", "Error fetching recommendation", e)
            "Error fetching recommendation: ${e.localizedMessage}"
        }
    }

    suspend fun summarizeReviews(reviews: List<Review>): String {
        if (reviews.isEmpty()) return "No customer reviews available yet for this product."

        val formattedReviews = reviews.joinToString("\n") {
            "- ${it.userName} (${it.rating}/5⭐): ${it.comment}"
        }

        val prompt = """
            You are an AI e-commerce assistant. Summarize these customer reviews concisely into 2 bullet points for Key Pros and 1 bullet point for Key Cons:
            
            $formattedReviews
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "Unable to generate review summary."
        } catch (e: Exception) {
            Log.e("GEMINI_ENGINE", "Error summarizing reviews", e)
            "Review summary unavailable: ${e.localizedMessage}"
        }
    }
}