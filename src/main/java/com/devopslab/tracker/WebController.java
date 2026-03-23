package com.devopslab.tracker;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String showDashboard() {
        // This tells Spring Boot to look for a file named "dashboard.html"
        return "dashboard"; 
    }
}