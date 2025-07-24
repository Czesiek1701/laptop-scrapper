package com.example.scraper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

import com.example.scraper.model.LaptopAukcja;

@RestController
@RequestMapping("/api")
public class LaptopController {

    private final LaptopScraperLaurem scraper;

    public LaptopController(LaptopScraperLaurem scraper) {
        this.scraper = scraper;
    }

    @GetMapping("/laptops")
    public List<LaptopAukcja> getLaptops() {
        try {
//            System.out.println("🔐 Zmienna SPRING_DATASOURCE_PASSWORD: " + System.getenv("SPRING_DATASOURCE_PASSWORD"));
            return scraper.getLaptops();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

    }
}
