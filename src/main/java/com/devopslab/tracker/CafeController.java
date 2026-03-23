package com.devopslab.tracker;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/cafe")
public class CafeController {

    // 1. Send the Menu to the Frontend
    @GetMapping("/menu")
    public Map<String, List<Map<String, Object>>> getMenu() {
        Map<String, List<Map<String, Object>>> menu = new LinkedHashMap<>();
        
        menu.put("Coffee & Drinks", Arrays.asList(
            createItem("Espresso", 120, "Rich and bold single shot of pure coffee"),
            createItem("Cappuccino", 180, "Espresso with steamed milk and a thick layer of foam"),
            createItem("Masala Chai", 80, "Traditional Indian spiced tea brewed to perfection"),
            createItem("Iced Frappe", 220, "Blended cold coffee with chocolate syrup and whipped cream")
        ));
        
        menu.put("Fast Food & Snacks", Arrays.asList(
            createItem("Classic Cheeseburger", 250, "Juicy grilled patty with fresh lettuce, tomato, and melted cheese"),
            createItem("Margherita Pizza", 350, "Wood-fired crust with fresh basil and mozzarella"),
            createItem("Loaded French Fries", 150, "Crispy golden fries topped with cheese and jalapeños")
        ));
        
        menu.put("Desserts", Arrays.asList(
            createItem("Chocolate Fudge Brownie", 180, "Warm fudgy brownie served with vanilla ice cream"),
            createItem("New York Cheesecake", 220, "Classic creamy cheesecake with a graham cracker crust")
        ));

        return menu;
    }

    // 2. Receive the Order from the Checkout Cart
    @PostMapping("/order")
    public Map<String, String> placeOrder(@RequestBody Map<String, Object> orderDetails) {
        // In a real production app, this would save to an AWS Database (RDS)
        // For now, we simulate a successful order!
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Order received successfully! Our chefs are preparing your food.");
        return response;
    }

    // Helper method to build menu items
    private Map<String, Object> createItem(String name, int price, String description) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("price", price);
        item.put("description", description);
        return item;
    }
}