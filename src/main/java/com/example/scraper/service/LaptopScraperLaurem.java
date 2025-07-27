package com.example.scraper.service;

import com.example.scraper.model.LaptopAukcja;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LaptopScraperLaurem {

    private static final String LISTING_URL = "https://laurem.pl/pol_m_Laptopy-100.html";

    public List<LaptopAukcja> getLaptops() {
        List<LaptopAukcja> out = new ArrayList<>();
        try {
            Document listing = Jsoup.connect(LISTING_URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(30_000)
                    .maxBodySize(0)
                    .followRedirects(true)
                    .get();

            Elements cards = listing.select("a.product-name");
            int i = 0;
            for (Element card : cards) {
                String url = card.absUrl("href");
                LaptopAukcja lap = scrapeLaptopDetails(url);
                if (lap != null) out.add(lap);

                // !!!!!!!!!!!!!!!!!!!!!
                i++;
                if(i>8) break;
            }

        } catch (IOException e) {
            System.err.println(">>> Błąd przy pobieraniu listy: " + e.getMessage());
        }
        return out;
    }

    private static String cleanUnits(String value) {
        return value
                .replaceAll("(\\d+),(\\d+)\\s*GHz", "$1.$2 GHz")     // zamiana przecinków na kropki w GHz
                .replaceAll("(\\d+)\\s*GB\\s*GB", "$1 GB")           // usunięcie duplikatów GB
                .replaceAll("(\\d+)\\\"\\s*[”\"]?", "$1\"")          // uporządkowanie znaków cali
                .replaceAll("\\s+", " ")                             // usunięcie zbędnych spacji
                .trim();
    }


    public LaptopAukcja scrapeLaptopDetails(String url) {
        System.out.println("🔍 Rozpoczynam scrapowanie: " + url);
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
//            String auctionTitle = Optional.ofNullable(doc.selectFirst("h1.page-title"))
//                    .map(Element::text).orElse("Brak tytułu").trim();
            Element h1 = doc.selectFirst("h1");
            String auctionTitle = h1 != null ? h1.text().trim() : "Brak tytułu";


            String manufacturer = Optional.ofNullable(
                            doc.selectFirst(".basic_info .producer a.brand"))
                    .map(Element::text).orElse("N/A");

            // —————————————————————————————————————————————————————————————
            // 1b) Seria (u nas traktujemy jako 'Stan')
            // —————————————————————————————————————————————————————————————
            String status = Optional.ofNullable(
                            doc.selectFirst(".basic_info .param_trait.zloty .proj_laur"))
                    .map(Element::text).orElse("N/A");

            // —————————————————————————————————————————————————————————————
            // 2) Cechy z <div class="traits_info">
            // —————————————————————————————————————————————————————————————
            String cpuModel    = "N/A";
            String ramAmount   = "N/A";
            String diskSize    = "N/A";
            String diskType    = "N/A";
            String screenSize  = "N/A";
            String resolution  = "N/A";
            String graphics    = "N/A";

            for (Element trait : doc.select(".traits_info .param_trait")) {
                Element labelEl = trait.selectFirst("span");
                Element valueEl = trait.selectFirst("strong.lt_description");

                String label = labelEl != null
                        ? labelEl.text().replace(":", "").trim()
                        : "N/A";
                String value = valueEl != null
                        ? valueEl.text().trim()
                        : "N/A";

                switch (label) {
                    case "Procesor"            -> cpuModel   = value;
                    case "Pamięć RAM"          -> ramAmount  = value;
                    case "Dysk Twardy"         -> {
                        String[] parts = value.split("\\s+", 2);
                        diskSize = parts[0];
                        diskType = (parts.length > 1 ? parts[1] : "N/A");
                    }
                    case "Przekątna ekranu"    -> screenSize = value;
                    case "Rozdzielczość ekranu"-> resolution = value;
                    case "Karta Graficzna"     -> graphics   = value;
                }
            }

            // —————————————————————————————————————————————————————————————
            // 3) Dodatkowe szczegóły z tabeli <tbody><tr>
            // —————————————————————————————————————————————————————————————
            String model           = "N/A";
            String cpuCores        = "N/A";
            String cpuFreqGHz      = "N/A";
            String screenType      = "N/A";
            String touchScreen     = "N/A";
            String condition       = status;          // domyślnie seria
            String operatingSystem = "Windows 11 Home";

            for (Element row : doc.select("tbody tr")) {
                Element labelEl = row.selectFirst("td:nth-child(1) span");
                Element valueEl = row.selectFirst("td:nth-child(2) .n54117_item_b_sub");

                String label = labelEl != null
                        ? labelEl.text().replace(":", "").trim()
                        : "N/A";
                String value = valueEl != null
                        ? valueEl.text().trim()
                        : "N/A";

                switch (label) {
                    case "Model"               -> model           = value;
                    case "Liczba rdzeni"       -> cpuCores        = value;
                    case "Taktowanie procesora"-> cpuFreqGHz      = value;
                    case "Typ matrycy"         -> screenType      = value;
                    case "Ekran dotykowy"      -> touchScreen     = value;
                    case "Stan produktu"       -> condition       = value;  // nadpisz, jeśli jest
                    case "System operacyjny"   -> operatingSystem = value;
                }
            }

            LaptopAukcja laptop = new LaptopAukcja(
                    id,
                    url,
                    auctionTitle,
                    manufacturer,
                    model,
                    condition,
                    ramAmount,
                    diskType,
                    diskSize,
                    cpuModel,
                    cpuFreqGHz,
                    cpuCores,
                    screenType,
                    touchScreen,
                    screenSize,
                    resolution,
                    graphics,
                    operatingSystem
            );

            System.out.println("✅ Scrapowanie zakończone: " + auctionTitle);
            return laptop;

        } catch (IOException e) {
            System.err.println("❌ Błąd przy scrapowaniu szczegółów: " + e.getMessage());
            return null;
        }
    }

    private String extractProductIdFromUrl(String url) {
        Matcher m = Pattern.compile("-(\\d+)\\.html$").matcher(url);
        return m.find() ? m.group(1) : "0";
    }
}
