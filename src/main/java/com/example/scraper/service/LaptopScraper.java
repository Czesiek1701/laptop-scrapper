package com.example.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class LaptopScraper {

    private static final String URL = "https://laurem.pl/pol_m_Laptopy-100.html";

    public List<String> getLaptops() {
        try {
            // 1. Pobierz i sparsuj HTML
            Document doc = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .get();

            // 2. Wyciągnij nazwy i linki
            Elements products = doc.select("a.product-name");
            List<String> laptops = new ArrayList<>();

            products.forEach(el -> {
                String name = el.text();
                String link = el.absUrl("href");
                laptops.add(name + " -> " + link);
            });

            return laptops;

        } catch (IOException e) {
            System.err.println("❌ Błąd podczas pobierania strony: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
