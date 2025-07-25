package com.example.scraper.controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostConstruct;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String sayHello() {
        return "Działa!";
    }

    @PostConstruct
    public void init() {
        System.out.println("✅ HelloController załadowany!");
    }

}

