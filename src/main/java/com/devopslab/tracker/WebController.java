package com.devopslab.tracker;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String showDashboard() {
        return "dashboard"; 
    }

    // NEW: The secret route for the kitchen staff
    @GetMapping("/admin")
    public String showAdmin() {
        return "admin"; 
    }
}