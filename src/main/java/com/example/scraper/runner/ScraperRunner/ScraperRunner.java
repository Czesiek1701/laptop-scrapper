package com.example.scraper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ScraperRunner implements CommandLineRunner {

    private final LaptopScraper laptopScraper;

    public ScraperRunner(LaptopScraper laptopScraper) {
        this.laptopScraper = laptopScraper;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Scrapowanie laptopów...");

        var laptops = laptopScraper.getLaptops();

        laptops.forEach(l -> System.out.println("Laptop: " + l));

        System.out.println("Scraping zakończony.");
    }
}
