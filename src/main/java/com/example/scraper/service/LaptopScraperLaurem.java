package com.example.scraper.service;

import com.example.scraper.model.LaptopAukcja;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class LaptopScraperLaurem {

    private static final String URL = "https://laurem.pl/pol_m_Laptopy-100.html";

//    public List<String> getLaptops() {
//        try {
//            // 1. Pobierz i sparsuj HTML
//            Document doc = Jsoup.connect(URL)
//                    .userAgent("Mozilla/5.0")
//                    .timeout(30_000)
//                    .maxBodySize(0)
//                    .followRedirects(true)
//                    .get();
//
//            // 2. Wyciągnij nazwy i linki
//            Elements products = doc.select("a.product-name");
//            List<String> laptops = new ArrayList<>();
//
//            products.forEach(el -> {
//                String name = el.text();
//                String link = el.absUrl("href");
//                laptops.add(name + " -> " + link);
//            });
//
//            return laptops;
//
//        } catch (IOException e) {
//            System.err.println("Błąd podczas pobierania strony: " + e.getMessage());
//            return Collections.emptyList();
//        }
//    }

    public List<LaptopAukcja> getLaptops() {
        try {
            Document doc = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(30_000)
                    .maxBodySize(0)
                    .followRedirects(true)
                    .get();

            Elements products = doc.select("a.product-name");
            List<LaptopAukcja> laptops = new ArrayList<>();

            products.forEach(el -> {
                String name = el.text();
                String link = el.absUrl("href");
                String id = "00000";

                LaptopAukcja laptop = new LaptopAukcja(
                        id,
                        link,
                        "N/A",  // manufacturer
                        "N/A",  // model
                        name,
                        "N/A",  // condition
                        "N/A",  // ramAmount
                        "N/A",  // diskType
                        "N/A",  // diskSize
                        "N/A",  // cpuModel
                        "N/A",  // cpuFrequencyGHz
                        "N/A",  // cpuCores
                        "N/A",  // screenType
                        "N/A", // foldingScreen
                        "N/A", // touchScreen
                        "N/A",  // screenSizeInches
                        "N/A",  // resolution
                        "N/A",  // graphics
                        "N/A"   // operatingSystem
                );

                laptops.add(laptop);
            });

            return laptops;

        } catch (IOException e) {
            System.err.println("Błąd podczas pobierania strony: " + e.getMessage());
            return Collections.emptyList();
        }
    }


}
