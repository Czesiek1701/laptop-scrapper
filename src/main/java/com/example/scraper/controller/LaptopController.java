package com.example.scraper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
public class LaptopController {

    private final LaptopScraper scraper;

    public LaptopController(LaptopScraper scraper) {
        this.scraper = scraper;
    }

    @GetMapping("/laptops")
    public List<String> getLaptops() {
        try {
            return scraper.getLaptops();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
