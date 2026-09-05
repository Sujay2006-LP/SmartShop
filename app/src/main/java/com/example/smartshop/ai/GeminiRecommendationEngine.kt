package com.example.smartshop.ai

import android.util.Log
import com.example.smartshop.model.Cart
import com.example.smartshop.model.CheckoutAgentResult
import com.example.smartshop.model.CheckoutSuggestion
import com.example.smartshop.model.Product
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson

class GeminiRecommendationEngine(private val apiKey: String) {

    private val gson = Gson()
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    suspend fun getCheckoutAddons(cart: Cart, catalog: List<Product>): CheckoutAgentResult {
        val aiResult = try {
            if (apiKey.startsWith("AIza")) {
                callGeminiForCheckout(cart, catalog)
            } else null
        } catch (e: Exception) {
            Log.e("GEMINI_AGENT", "API Error: ${e.message}")
            null
        }

        if (aiResult is CheckoutAgentResult.Suggestion) return aiResult

        val fallback = getLocalFallbackSuggestion(cart, catalog)
        return fallback?.let { CheckoutAgentResult.Suggestion(it) } ?: CheckoutAgentResult.NoSuggestion
    }

    private suspend fun callGeminiForCheckout(cart: Cart, catalog: List<Product>): CheckoutAgentResult? {
        val catalogData = catalog.map { product ->
            mapOf(
                "id" to product.id,
                "name" to product.name,
                "category" to product.category,
                "price" to product.price,
                "rating" to product.rating,
                "reviews" to product.reviews.map { it.comment }
            )
        }
        val cartItems = cart.items.map { it.name }
        val maxCartPrice = cart.items.maxOfOrNull { it.price } ?: 0.0
        
        val prompt = """
            You are a Senior Tech Lifestyle Consultant. 
            Catalog Data: ${gson.toJson(catalogData)}
            User's Cart: ${gson.toJson(cartItems)}
            Current Cart Max Price: $maxCartPrice

            Goal: Suggest exactly ONE synergistic product.
            
            CRITICAL RULES:
            1. PRICE LIMIT: The suggested product MUST be cheaper than $maxCartPrice. We are offering helpful add-ons, not expensive upgrades.
            2. RATING: Include the suggested product's rating (⭐) in the reasoning.
            3. SYNERGY: 
               - If cart has Laptop AND Mouse: Suggest a "Laptop Bag" or "Protective Sleeve" to complete the PC setup.
               - If cart has Laptop: Suggest mouse/keyboard/sleeve.
               - If cart has Mouse (no PC yet): Suggest a "Keyboard" (avoid mobile items).
               - If cart has Keyboard: Suggest mouse/desk mat/laptop bag.
               - If cart has Mobile: PRIORITIZE "Armor Tempered Glass" (Screen Guard) for protection. Only suggest chargers if they already have protection.
               - If cart has Accessory (Glass): Suggest a cheaper item like a Cable or Flash Drive.
            
            Format ONLY JSON: {"productId": "ID", "reasoning": "Many users who bought [Item] also bought [Suggested] (⭐ Rating). [Explanation]...", "link": "...", "bankOffer": "..."}
        """.trimIndent()

        val response = generativeModel.generateContent(prompt)
        val jsonText = response.text?.replace("```json", "")?.replace("```", "")?.trim() ?: return null
        val raw = gson.fromJson(jsonText, Map::class.java)
        
        val id = raw["productId"] as? String ?: return null
        val reason = raw["reasoning"] as? String ?: return null
        val offer = raw["bankOffer"] as? String ?: "Flat 10% Off for Lucky Customers"
        val link = raw["link"] as? String ?: "https://smartshop.tech/exclusive-offer"

        val validated = catalog.find { it.id == id }
        if (validated == null) {
            return CheckoutAgentResult.Blocked(id, "Suggested product ID not found in catalog")
        }

        // Hard Guardrail: Price Bounding
        if (validated.price >= maxCartPrice && maxCartPrice > 0) {
            return CheckoutAgentResult.Blocked(id, "Suggested product price (₹${validated.price}) exceeds cart bounding limit (₹$maxCartPrice)")
        }

        return CheckoutAgentResult.Suggestion(
            CheckoutSuggestion(validated.id, validated.name, reason, validated.price, link, offer)
        )
    }

    private fun getLocalFallbackSuggestion(cart: Cart, catalog: List<Product>): CheckoutSuggestion? {
        val cartNames = cart.items.map { it.name.lowercase() }
        val maxPrice = cart.items.maxOfOrNull { it.price } ?: 0.0
        val hasLaptop = cartNames.any { it.contains("macbook") || it.contains("dell") || it.contains("laptop") }
        val hasMouse = cartNames.any { it.contains("mouse") }
        val hasMobile = cartNames.any { it.contains("iphone") || it.contains("samsung") || it.contains("mobile") }
        val hasGlass = cartNames.any { it.contains("glass") || it.contains("armor") }
        
        return when {
            hasLaptop && hasMouse -> {
                // If both, suggest bag or sleeve
                val product = catalog.find { (it.name.contains("bag", true) || it.name.contains("sleeve", true)) && it.price < maxPrice }
                    ?: catalog.find { it.id == "15" || it.id == "17" }
                
                product?.let { CheckoutSuggestion(it.id, it.name, "Many users who bought a laptop and mouse also bought the ${it.name} (⭐ ${it.rating}) to complete their PC setup.", it.price, "https://smartshop.tech/setup", "Lucky Customer: Extra 7% Off on Protection") }
            }
            hasMouse -> {
                // If only mouse, suggest keyboard
                val product = catalog.find { (it.name.contains("keyboard", true) || it.id == "16") && it.price < maxPrice }
                    ?: catalog.find { it.category == "Accessories" && it.price < maxPrice }
                
                product?.let { CheckoutSuggestion(it.id, it.name, "Many users who bought a mouse also bought the ${it.name} (⭐ ${it.rating}) for a better workspace experience.", it.price, "https://smartshop.tech/setup", "Bundle Discount: 5% Off on Keyboards") }
            }
            cartNames.any { it.contains("keyboard") } -> {
                // If keyboard, suggest mouse or bag (cheaper than keyboard)
                val product = catalog.find { (it.name.contains("mouse", true) || it.name.contains("bag", true)) && it.price < maxPrice }
                    ?: catalog.find { it.category == "Accessories" && it.price < maxPrice }
                
                product?.let { CheckoutSuggestion(it.id, it.name, "Many users who bought a keyboard also bought the ${it.name} (⭐ ${it.rating}) to complete their desk setup.", it.price, "https://smartshop.tech/setup", "Extra 5% Discount for Desk Upgrades") }
            }
            hasLaptop -> {
                // If laptop, suggest mouse, keyboard or sleeve
                val product = catalog.find { (it.name.contains("mouse", true) || it.name.contains("keyboard", true) || it.id == "8" || it.id == "7") && it.price < maxPrice }
                
                product?.let { CheckoutSuggestion(it.id, it.name, "Many people buying a laptop also bought the ${it.name} (⭐ ${it.rating}). It's a functional masterpiece for productivity.", it.price, "https://smartshop.tech/pro", "Flat 10% Off on HDFC Credit Cards") }
            }
            hasMobile && !hasGlass -> {
                // Prioritize Screen Guard (ID 11) for mobiles
                catalog.find { it.id == "11" && it.price < maxPrice }
                    ?.let { CheckoutSuggestion(it.id, it.name, "Protect your investment! Many users added the ${it.name} (⭐ ${it.rating}) for premium screen protection.", it.price, "https://smartshop.tech/protect", "Special: Get it for just ₹799 with any phone") }
            }
            hasMobile && hasGlass -> {
                // If they already have glass, then suggest charger
                catalog.find { it.id == "13" && it.price < maxPrice }
                    ?.let { CheckoutSuggestion(it.id, it.name, "Don't run out of juice! Many users also added the ${it.name} (⭐ ${it.rating}) to their new mobile setup.", it.price, "https://smartshop.tech/protect", "Flat 5% Off on Charging Essentials") }
            }
            else -> {
                // Generic cheaper accessory fallback
                catalog.filter { it.price < maxPrice }.maxByOrNull { it.rating }
                    ?.let { CheckoutSuggestion(it.id, it.name, "Other customers also loved this ${it.name} (⭐ ${it.rating}) for their tech collection.", it.price, "https://smartshop.tech/all", "Flat 5% Off for Lucky Customers") }
            }
        }
    }

    suspend fun getRecommendation(cart: Cart, catalog: List<Product>): CheckoutSuggestion? {
        val result = getCheckoutAddons(cart, catalog)
        return if (result is CheckoutAgentResult.Suggestion) result.suggestion else null
    }

    suspend fun summarizeReviews(product: Product): String {
        val prompt = "Summarize these customer reviews for ${product.name}: ${product.reviews.joinToString { it.comment }}. Mention the specific pros users love."
        return try {
            generativeModel.generateContent(prompt).text ?: "Premium quality verified by tech experts."
        } catch (e: Exception) {
            "Highly regarded for its robust build quality and exceptional performance."
        }
    }
}
