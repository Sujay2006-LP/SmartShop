# SmartShop: The Upsell & Cross-sell AI Agent 🛒✨

SmartShop is a modern e-commerce application integrated with an advanced **AI Consultant** designed to grow merchant revenue through intelligent, synergistic product recommendations. Built for the **Razorpay Hackathon**, this app demonstrates how an AI agent can provide explainable, bounded, and gated financial suggestions while maintaining a premium user experience.

---

## 🚀 The Problem
Modern e-commerce often feels like a lonely catalog search. Merchants lose significant revenue because users forget essential add-ons (like protection for a new phone or a bag for a laptop). Generic "Frequently Bought Together" sections often suggest irrelevant or overly expensive items that break user trust.

## ✨ The Solution: The "AI Consultant"
SmartShop introduces a **Senior Tech Lifestyle Consultant**—an AI agent built on Gemini 1.5 Flash that lives within the shopping journey. It doesn't just show products; it builds **tailored tech ecosystems**.

### Key Solving Factors:
1.  **Revenue Growth via Synergy**: The agent analyzes the cart to find "missing pieces." If you buy a keyboard, it suggests a mouse. If you buy a laptop and mouse, it suggests a sleeve.
2.  **Budget-Conscious Intelligence**: Follows a **Strict Price Bounding** rule—it only suggests items cheaper than your primary purchase.
3.  **Trust via Social Proof**: Suggestions include real-world context (e.g., "95% of customers add this") and star ratings (⭐).

---

## 🏗 Project Structure

The project follows a clean MVVM architecture organized by feature and responsibility:

```text
app/src/main/java/com/example/smartshop/
├── ai/                 # AI Agent logic & Gemini integration
│   └── GeminiRecommendationEngine.kt
├── data/               # Local data sources & static catalog
├── model/              # Data models (Product, Cart, CheckoutState)
├── repository/         # Firebase Firestore data syncing
├── ui/                 # UI Layer (Jetpack Compose)
│   ├── components/     # Reusable UI (CartSheet, ProductCard, etc.)
│   ├── screens/        # Full screen layouts (Login, Main)
│   └── theme/          # Design system & Gradient styling
├── viewmodel/          # Business logic & state management
│   └── MainViewModel.kt
└── MainActivity.kt     # App entry point & Razorpay integration
```

---

## 🛠 Hackathon Compliance (10-Point Checklist)

This app was built strictly against the Track 01 problem statement:

1.  **Revenue Growth**: Real-time synergistic upsells in the checkout flow.
2.  **Reasoning**: Every suggestion comes with an expert-level explanation.
3.  **Bounded**: Suggestions are strictly validated against the merchant's fixed catalog.
4.  **Gated**: The `MainViewModel` acts as a gatekeeper, validating AI output before display.
5.  **Explainable**: Every decision is logged with a human-readable reason.
6.  **Audit Trail**: Full decision history (Suggested/Accepted/Blocked) is stored in Firebase.
7.  **Failure Handling**: Guardrail violations (like price up-selling) are caught and bypassed gracefully.
8.  **Razorpay Integration**: The payment gateway total updates dynamically with AI upsells.
9.  **Measurable Outcome**: Audit logs track `cartTotalBefore` vs `cartTotalAfter`.
10. **Clean Clone**: Standard Gradle structure ensures the project runs out of the box.

---

## 📱 Features

- **Interactive Cart Management**: Live-updating footer with quantity controls.
- **Dynamic Footer Visibility**: Checkout bar only appears when items are present.
- **"Tap to Explore" Navigation**: AI suggestions link directly to product details.
- **Premium Gradient UX**: Modern, edge-to-edge design with high-contrast styling.
- **Snackbar Feedback**: Instant confirmation for every "Add to Cart" action.

---

## 🛠 Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose
- **AI**: Gemini 1.5 Flash (Google Generative AI SDK)
- **Backend**: Firebase Auth & Firestore
- **Payments**: Razorpay Standard SDK (Test Mode)
- **Image Loading**: Coil

---

## 🚀 Setup & Installation
1. Clone the repository.
2. **Security Note**: `local.properties` and `google-services.json` are excluded from version control for security.
3. Add your `GEMINI_API_KEY` and `RAZORPAY_KEY_ID` to `local.properties`.
4. Place your Firebase `google-services.json` in the `app/src/` directory.
5. Open in Android Studio (Ladybug or newer).
6. Sync Gradle and Run on an Emulator or Physical Device.
