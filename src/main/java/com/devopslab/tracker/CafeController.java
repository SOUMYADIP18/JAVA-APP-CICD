package com.devopslab.tracker;

import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

@RestController
@RequestMapping("/api/cafe")
public class CafeController {

    // This is the megaphone that broadcasts to the WebSockets!
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/menu")
    public Map<String, List<Map<String, Object>>> getMenu() {
        // Keep your exact same menu code here!
        Map<String, List<Map<String, Object>>> menu = new LinkedHashMap<>();
        menu.put("Coffee & Drinks", Arrays.asList(
            createItem("Espresso", 120, "Rich and bold single shot of pure coffee"),
            createItem("Cappuccino", 180, "Espresso with steamed milk and a thick layer of foam")
        ));
        menu.put("Fast Food & Snacks", Arrays.asList(
            createItem("Classic Cheeseburger", 250, "Juicy grilled patty with fresh lettuce"),
            createItem("Loaded French Fries", 150, "Crispy golden fries topped with cheese")
        ));
        return menu;
    }

    // UPDATED: Now it broadcasts to the kitchen!
    @PostMapping("/order")
    public Map<String, Object> placeOrder(@RequestBody Map<String, Object> orderDetails) {
        
        // 1. Generate a random Order ID like a real cafe
        String orderId = "ORD-" + (int)(Math.random() * 10000);
        orderDetails.put("orderId", orderId);
        orderDetails.put("status", "NEW");
        orderDetails.put("time", new Date().toString());

        // 2. THE WEBSOCKET MAGIC: Broadcast this exact order to the "/topic/orders" channel
        messagingTemplate.convertAndSend("/topic/orders", orderDetails);

        // 3. Send the standard HTTP response back to the customer's phone
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Order " + orderId + " sent to the kitchen!");
        return response;
    }

    private Map<String, Object> createItem(String name, int price, String description) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("price", price);
        item.put("description", description);
        return item;
    }
}