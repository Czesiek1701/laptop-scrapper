package com.example.scraper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LaptopController {

    private final LaptopScraper laptopScraper;

    public LaptopController(LaptopScraper laptopScraper) {
        this.laptopScraper = laptopScraper;
    }

    @GetMapping("/laptops")
    public List<String> getLaptops() {
        return laptopScraper.getLaptops();
    }
}
