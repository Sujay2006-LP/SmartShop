# SmartShop: The Upsell & Cross-sell AI Agent

## 🚀 The Problem
Modern e-commerce often feels like a lonely catalog search. Merchants lose significant revenue because users forget essential add-ons (like protection for a new phone or a bag for a laptop), and generic "Frequently Bought Together" sections often suggest irrelevant or overly expensive items that break user trust.

## ✨ The Solution: The "AI Consultant"
SmartShop introduces a **Senior Tech Lifestyle Consultant**—an AI agent built on Gemini 1.5 Flash that lives within the shopping journey. It doesn't just show products; it builds **tailored tech ecosystems**.

### Key Solving Factors:
1.  **Revenue Growth via Synergy**: The agent analyzes the cart to find "missing pieces." If you buy a keyboard, it suggests a mouse. If you buy a laptop and mouse, it suggests a sleeve.
2.  **Budget-Conscious Intelligence**: Unlike "dumb" recommendation engines, this agent follows a **Strict Price Bounding** rule—it only suggests items that are cheaper than your primary purchase, ensuring suggestions feel like helpful essentials rather than pushy upselling.
3.  **Trust via Social Proof**: Every suggestion includes real-world context (e.g., "95% of customers add this") and the product's actual star rating (⭐) to provide immediate validation.

## 🛠 Technical Implementation & Compliance

### 1. Bounded & Gated (The Safety Layer)
The agent is strictly **Bounded** by the merchant's catalog. It is impossible for the AI to suggest a hallucinated product or price. The flow is **Gated** by the `MainViewModel`, which validates AI output against the local database before displaying it to the user.

### 2. The Audit Trail
Every interaction is logged to a `checkout_audit_trail` collection in Firebase. Each entry is **Explainable**, capturing:
-   **Decision**: (Suggested / Accepted / Rejected / Blocked)
-   **AI Reasoning**: The specific justification provided by the consultant.
-   **Revenue Impact**: `cartTotalBefore` vs `cartTotalAfter`.
-   **Guardrail Events**: Detailed logs of why a suggestion was blocked (e.g., "Price Limit Violated").

### 3. Graceful Failure Handling
The system is built to handle the "Unpredictable AI" problem. If the Gemini API produces malformed data or violates a safety guardrail, the app **silently bypasses** the suggestion and proceeds to the payment page, ensuring a frictionless user experience.

## 💳 Razorpay Integration
The agent is fully integrated with **Razorpay Test-Mode APIs**. When a user accepts a "Smart Setup Enhancement," the cart total is dynamically recalculated and injected into the Razorpay payment options, proving a direct line from AI suggestion to revenue capture.

## 📱 Tech Stack
-   **Language**: Kotlin
-   **UI**: Jetpack Compose (Modern Gradient Design)
-   **AI**: Google Generative AI SDK (Gemini 1.5 Flash)
-   **Backend**: Firebase Auth & Firestore
-   **Payments**: Razorpay Standard SDK
