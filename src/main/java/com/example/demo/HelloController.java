package com.example.demo; // Update this if your folder name is different

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return "DevOps Java App is LIVE on AWS! V2 😎";
    }
}
