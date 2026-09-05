package com.example.smartshop.model

data class CheckoutSuggestion(
    val productId: String,
    val productName: String,
    val reasoning: String,
    val price: Double,
    val externalLink: String = "https://example.com/product",
    val bankOffer: String = "Get 5% Instant Discount with any Credit Card"
)

data class AuditLogEntry(
    val cartId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val decision: String, // "SUGGESTED", "ACCEPTED", "REJECTED", "BLOCKED_BY_GUARDRAIL"
    val suggestedProductId: String?,
    val aiReasoning: String?,
    val cartTotalBefore: Double,
    val cartTotalAfter: Double
)

sealed class CheckoutAgentResult {
    data class Suggestion(val suggestion: CheckoutSuggestion) : CheckoutAgentResult()
    data class Blocked(val attemptedProductId: String, val reason: String) : CheckoutAgentResult()
    object NoSuggestion : CheckoutAgentResult()
}

sealed class CheckoutState {
    object Idle : CheckoutState()
    object Analyzing : CheckoutState()
    data class Suggesting(val suggestion: CheckoutSuggestion) : CheckoutState()
    object FinalizingPayment : CheckoutState()
}
