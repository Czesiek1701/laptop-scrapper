package com.example.scraper.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class LauremScraperService {

    public String scrapeTitle() {
        try {
            Document doc = Jsoup.connect("https://laurem.pl").get();
            // Przykład: wyciągamy tytuł strony
            return doc.title();
        } catch (Exception e) {
            e.printStackTrace();
            return "Błąd podczas scrapowania";
        }
    }
}
