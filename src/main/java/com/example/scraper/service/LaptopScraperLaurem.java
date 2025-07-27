package com.example.scraper.service;

import com.example.scraper.model.LaptopAukcja;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class LaptopScraperLaurem {

    private static final String LISTING_URL = "https://laurem.pl/pol_m_Laptopy-100.html";

    /**
     * 1) Scrape listing page → zbierz wszystkie linki
     * 2) Dla każdego linku wywołaj scrapeLaptopDetails(...)
     */
    public List<LaptopAukcja> getLaptops() {
        List<LaptopAukcja> out = new ArrayList<>();
        try {
            Document listing = Jsoup.connect(LISTING_URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(30_000)
                    .maxBodySize(0)
                    .followRedirects(true)
                    .get();

            // wszystkie kafelki z linkiem do produktu
            Elements cards = listing.select("a.product-name");

            for (Element card : cards) {
                String url = card.absUrl("href");
                LaptopAukcja lap = scrapeLaptopDetails(url);
                if (lap != null) out.add(lap);
            }

        } catch (IOException e) {
            System.err.println(">>> Błąd przy pobieraniu listy: " + e.getMessage());
        }
        return out;
    }


    public LaptopAukcja scrapeLaptopDetails(String url) {
        System.out.println("🔍 Rozpoczynam scrapowanie laptopa ze strony: " + url);
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .referrer("https://www.bing.com")
                    .timeout(30_000)
                    .maxBodySize(0)
                    .followRedirects(true)
                    .get();
            System.out.println("✅ Pobrano dokument HTML");

            // —————————————————————————————————————————————————————————————
            // 1) ID, producent i tytuł aukcji
            // —————————————————————————————————————————————————————————————
            String id = extractProductIdFromUrl(url);
            String title = Optional.ofNullable(doc.selectFirst("h1.page-title"))
                    .map(Element::text).orElse("Brak tytułu")
                    .trim();

            String manufacturer = Optional.ofNullable(
                            doc.selectFirst(".basic_info .producer a.brand"))
                    .map(Element::text).orElse("N/A");

            // —————————————————————————————————————————————————————————————
            // 2) Cechy z <div class="traits_info">
            // —————————————————————————————————————————————————————————————
            String cpuModel       = "N/A";
            String ramAmount      = "N/A";
            String diskSize       = "N/A";
            String diskType       = "N/A";
            String screenSize     = "N/A";
            String resolution     = "N/A";
            String graphics       = "N/A";

            for (Element trait : doc.select(".traits_info .param_trait")) {
                String label = trait.selectFirst("span").text()
                        .replace(":", "").trim();
                String value = trait.selectFirst("strong.lt_description")
                        .text().trim();

                switch (label) {
                    case "Procesor"       -> cpuModel = value;
                    case "Pamięć RAM"     -> ramAmount = value;
                    case "Dysk Twardy"    -> {
                        // "512GB SSD" → diskSize="512GB", diskType="SSD"
                        String[] parts = value.split("\\s+", 2);
                        diskSize = parts[0];
                        diskType = (parts.length>1 ? parts[1] : "N/A");
                    }
                    case "Przekątna ekranu" -> screenSize = value;
                    case "Rozdzielczość ekranu" -> resolution = value;
                    case "Karta Graficzna"-> graphics = value;
                }
            }

            // —————————————————————————————————————————————————————————————
            // 3) Dodatkowe szczegóły z tabeli <tbody><tr>
            // —————————————————————————————————————————————————————————————
            String model            = "N/A";
            String cpuCores         = "N/A";
            String cpuFrequencyGHz  = "N/A";
            String screenType       = "N/A";
            String touchScreen      = "N/A";
            String foldingScreen    = "N/A";
            String condition        = "N/A";
            String operatingSystem  = "Windows 11 Home";  // domyślnie

            for (Element row : doc.select("tbody tr")) {
                String label = row.selectFirst("td:nth-child(1) span")
                        .text().replace(":", "").trim();
                String value = row.selectFirst("td:nth-child(2) .n54117_item_b_sub")
                        .text().trim();

                switch (label) {
                    case "Model"               -> model = value;
                    case "Liczba rdzeni"       -> cpuCores = value;
                    case "Taktowanie procesora"-> cpuFrequencyGHz = value;
                    case "Typ matrycy"         -> screenType = value;
                    case "Ekran dotykowy"      -> touchScreen = value;
                    // jeżeli będzie kiedykolwiek w HTML:
                    case "Zawiasy matrycy"     -> foldingScreen = value;
                    case "Stan produktu"       -> condition = value;
                    case "System operacyjny"   -> operatingSystem = value;
                }
            }

            LaptopAukcja laptop = new LaptopAukcja(
                    id,
                    url,
                    manufacturer,
                    model,
                    title,
                    condition,
                    ramAmount,
                    diskType,
                    diskSize,
                    cpuModel,
                    cpuFrequencyGHz,
                    cpuCores,
                    screenType,
                    foldingScreen,
                    touchScreen,
                    screenSize,
                    resolution,
                    graphics,
                    operatingSystem
            );

            System.out.println("✅ Scrapowanie zakończone: " + title);
            return laptop;

        } catch (IOException e) {
            System.err.println("❌ Błąd przy scrapowaniu szczegółów: " + e.getMessage());
            return null;
        }
    }

    // pomocniczka do wyciągnięcia ID z URL, przykładowa:
    private String extractProductIdFromUrl(String url) {
        // np. ...-22888.html → 22888
        Matcher m = Pattern.compile("-(\\d+)\\.html$").matcher(url);
        return m.find() ? m.group(1) : "0";
    }


}
