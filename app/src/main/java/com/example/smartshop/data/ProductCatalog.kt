package com.example.smartshop.data

import com.example.smartshop.model.Cart
import com.example.smartshop.model.CartItem
import com.example.smartshop.model.Product
import com.example.smartshop.model.Review

object CatalogData {

    fun get100Products(): List<Product> {
        val products = mutableListOf<Product>()
        
        // 1. Mobiles
        products.add(Product("1", "iPhone 15 Pro", "Apple", "Mobile", 124900.0, 134900.0, 4.8, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=500", "Titanium design, A17 Pro chip.", 
            listOf(Review("TechGuru", 5.0, "The titanium finish is elite. Best iPhone yet."), Review("Sarah", 4.5, "Fast, but gets warm during gaming."))))
        
        products.add(Product("2", "Samsung S24 Ultra", "Samsung", "Mobile", 129999.0, 139999.0, 4.7, "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=500", "AI-powered camera and S-Pen.", 
            listOf(Review("AndroidFan", 4.9, "The zoom is incredible. AI features are actually useful."), Review("Mike", 4.0, "A bit bulky for one-handed use."))))

        // 2. Laptops
        products.add(Product("3", "MacBook Pro M3", "Apple", "Laptop", 169900.0, 189900.0, 4.9, "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500", "Ultimate pro laptop with M3 chip.", 
            listOf(Review("DevFlow", 5.0, "Silent performance. Battery life is unbelievable."), Review("DesignPro", 4.8, "The display is the best in the market."))))
        
        products.add(Product("4", "Dell XPS 15", "Dell", "Laptop", 145000.0, 160000.0, 4.6, "https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=500", "Stunning display and performance.", 
            listOf(Review("User123", 4.5, "Great screen, but gets a bit loud under load."), Review("Emily", 4.7, "Solid build, very premium feel."))))

        // 3. Audio
        products.add(Product("5", "Sony WH-1000XM5", "Sony", "Audio", 29990.0, 34990.0, 4.8, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500", "Industry leading noise cancellation.", 
            listOf(Review("MusicLover", 5.0, "ANC is magic. Sound is balanced."), Review("Jake", 4.5, "Wish they folded like the XM4s."))))

        // 4. Accessories
        products.add(Product("7", "Logitech MX Master 3S", "Logitech", "Accessories", 10995.0, 12995.0, 4.8, "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500", "Precision mouse for productivity.", 
            listOf(Review("OfficeGeek", 5.0, "My wrist pain is gone. The scroll wheel is addictive."), Review("DevX", 4.7, "Best investment for my MacBook setup."))))
        
        products.add(Product("8", "Keychron K2 Wireless", "Keychron", "Accessories", 8499.0, 9999.0, 4.5, "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?w=500", "Compact mechanical keyboard.", 
            listOf(Review("Typist", 4.8, "Satisfying clicks. Works great with Mac."), Review("Leo", 4.0, "Keycaps are a bit high, need a wrist rest."))))
        
        products.add(Product("11", "Armor Tempered Glass", "Armor", "Accessories", 999.0, 1499.0, 4.9, "https://images.unsplash.com/photo-1603899122634-f086ca5f5ddd?w=500", "9H Hardness, Ultra-clear protection.", 
            listOf(Review("SafeUser", 5.0, "Saved my screen twice already. Crystal clear."), Review("Amy", 4.8, "Easy to install, no bubbles."))))
        
        products.add(Product("12", "SanDisk Dual Drive Luxe", "SanDisk", "Accessories", 2500.0, 3500.0, 4.7, "https://images.unsplash.com/photo-1565261021703-e298319f7e5b?w=500", "USB-C and USB-A 2-in-1 Flash Drive.", 
            listOf(Review("DataMover", 4.9, "Finally, I can move files from my iPhone to my PC without cables!"), Review("Techie", 4.5, "Fast and all-metal build."))))
        
        products.add(Product("13", "100W GaN Charger", "Anker", "Accessories", 5500.0, 6999.0, 4.8, "https://images.unsplash.com/photo-1625039141076-25805569f6f6?w=500", "Charge your Laptop and Mobile simultaneously.", 
            listOf(Review("Traveler", 5.0, "One brick for everything. Amazing."), Review("Chris", 4.6, "A bit pricey but worth it."))))

        products.add(Product("14", "Essential Wireless Mouse", "SmartShop", "Accessories", 1499.0, 1999.0, 4.2, "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500", "Affordable and reliable wireless mouse.", 
            listOf(Review("BudgetBuyer", 4.5, "Does the job well for the price."), Review("User", 4.0, "Basic but functional."))))
        
        products.add(Product("15", "Premium Laptop Bag", "Urban", "Accessories", 2999.0, 3999.0, 4.6, "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=500", "Water-resistant bag for up to 16 inch laptops.", 
            listOf(Review("Traveler", 4.8, "Lots of pockets, very comfortable."), Review("Office", 4.4, "Fits my MacBook Pro perfectly."))))

        products.add(Product("16", "Slim Keyboard K380", "Logitech", "Accessories", 2499.0, 3499.0, 4.4, "https://images.unsplash.com/photo-1587829741301-dc798b83dadc?w=500", "Multi-device Bluetooth keyboard.", 
            listOf(Review("Nomad", 4.5, "Perfect for travel."), Review("Sarah", 4.2, "Quiet and compact."))))
        
        products.add(Product("17", "Protective Laptop Sleeve", "SmartShop", "Accessories", 999.0, 1499.0, 4.5, "https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=500", "Soft interior laptop protection.", 
            listOf(Review("SafetyFirst", 4.7, "Great quality for the price."), Review("Leo", 4.3, "Fits my laptop perfectly."))))

        return products
    }

    fun getSampleCarts(): List<Cart> {
        val developerItems = listOf(
            CartItem(id = "3", name = "MacBook Pro M3", quantity = 1, price = 169900.0),
            CartItem(id = "7", name = "Logitech MX Master 3S", quantity = 1, price = 10995.0)
        )

        return listOf(
            Cart(
                id = "cart_dev",
                user_id = "user_dev_01",
                items = developerItems,
                total_amount = developerItems.sumOf { it.price * it.quantity }
            )
        )
    }
}