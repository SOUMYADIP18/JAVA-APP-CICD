package com.devopslab.tracker;

import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

@RestController
@RequestMapping("/api/cafe")
public class CafeController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/menu")
    public Map<String, List<Map<String, Object>>> getMenu() {
        Map<String, List<Map<String, Object>>> menu = new LinkedHashMap<>();
        
        // Added high-quality Unsplash image URLs to every item
        menu.put("Signature Coffees", Arrays.asList(
            createItem("Classic Espresso", 120, "Rich and bold single shot of pure arabica coffee.", "https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?w=500&q=80"),
            createItem("Velvet Cappuccino", 180, "Espresso with steamed milk and a thick layer of foam.", "https://images.unsplash.com/photo-1534778101976-62847782c213?w=500&q=80"),
            createItem("Iced Hazelnut Frappe", 220, "Blended cold coffee with hazelnut syrup and whipped cream.", "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=500&q=80")
        ));
        
        menu.put("Gourmet Fast Food", Arrays.asList(
            createItem("Artisan Cheeseburger", 250, "Juicy grilled patty with fresh lettuce, tomato, and melted cheese.", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&q=80"),
            createItem("Wood-Fired Margherita", 350, "Authentic crust with fresh basil and mozzarella.", "https://grandecheese.com/wp-content/uploads/2025/02/Wood-Fired-Margherita-1.jpg"),
            createItem("Truffle Fries", 150, "Crispy golden fries tossed in truffle oil and parmesan.", "https://images.unsplash.com/photo-1534080564583-6be75777b70a?w=500&q=80")
        ));
        
        menu.put("Decadent Desserts", Arrays.asList(
            createItem("Fudge Brownie", 180, "Warm fudgy brownie served with vanilla bean ice cream.", "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=500&q=80"),
            createItem("New York Cheesecake", 220, "Classic creamy cheesecake with a buttery graham crust.", "https://images.unsplash.com/photo-1533134242443-d4fd215305ad?w=500&q=80")
        ));

        return menu;
    }

    @PostMapping("/order")
    public Map<String, Object> placeOrder(@RequestBody Map<String, Object> orderDetails) {
        String orderId = "ORD-" + (int)(Math.random() * 10000);
        orderDetails.put("orderId", orderId);
        orderDetails.put("status", "NEW");
        orderDetails.put("time", new Date().toString());

        messagingTemplate.convertAndSend("/topic/orders", orderDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Order " + orderId + " sent to the kitchen!");
        return response;
    }

    // UPDATED: Now accepts an imageUrl parameter
    private Map<String, Object> createItem(String name, int price, String description, String imageUrl) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("price", price);
        item.put("description", description);
        item.put("imageUrl", imageUrl); // Attaching the image to the JSON response
        return item;
    }
}