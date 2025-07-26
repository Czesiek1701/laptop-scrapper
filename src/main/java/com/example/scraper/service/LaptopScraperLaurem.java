package com.example.scraper.service;

import com.example.scraper.model.LaptopAukcja;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;


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
        System.out.println("🔍 Rozpoczynam scrapowanie laptopa ze strony:");
        System.out.println("👉 URL: " + url);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/114 Safari/537.36")
                    .referrer("https://www.google.com")
                    .timeout(30_000)
                    .maxBodySize(0)
                    .followRedirects(true)
                    .get();

            System.out.println("✅ Pobrano dokument HTML");

            // 🔤 Tytuł laptopa
            Element titleElement = doc.selectFirst("h1.page-title");
            String title = (titleElement != null) ? titleElement.text().trim() : "Brak tytułu";
            if (titleElement == null) {
                System.out.println("⚠️ Nie znaleziono elementu: h1.page-title");
            }

            // 🧪 Parsowanie specyfikacji
            Elements rows = doc.select("tbody tr");
            System.out.println("🔎 Znaleziono wierszy specyfikacji: " + rows.size());

            // 🔧 Domyślne wartości
            String id = extractProductIdFromUrl(url);
            String manufacturer = "Microsoft"; // można ustawić na sztywno
            String model = "N/A";
            String condition = "N/A";
            String ramAmount = "N/A";
            String diskSize = "N/A";
            String diskType = "N/A";
            String cpuModel = "N/A";
            String cpuFrequencyGHz = "N/A";
            String cpuCores = "N/A";
            String screenType = "N/A";
            String foldingScreen = "N/A";
            String touchScreen = "N/A";
            String screenSizeInches = "N/A";
            String resolution = "N/A";
            String graphics = "N/A";
            String operatingSystem = "Windows 11 Home"; // również można ustawić ręcznie

            for (Element row : rows) {
                String label = row.select("td:nth-child(1)").text().replace(":", "").trim();

                Elements valueElements = row.select("td:nth-child(2) .n54117_item_b_sub");

                if (label.isEmpty() || valueElements.isEmpty()) continue;

                String value = valueElements.stream()
                        .map(Element::text)
                        .collect(Collectors.joining(", ")).trim();

                switch (label) {
                    case "Model":
                        model = value;
                        break;
                    case "Procesor":
                        cpuModel = value;
                        break;
                    case "Liczba rdzeni":
                        cpuCores = value;
                        break;
                    case "Taktowanie procesora":
                        cpuFrequencyGHz = value;
                        break;
                    case "Przekątna ekranu":
                        screenSizeInches = value;
                        break;
                    case "Rozdzielczość ekranu":
                        resolution = value;
                        break;
                    case "Typ matrycy":
                        screenType = value;
                        break;
                    case "Ekran dotykowy":
                        touchScreen = value;
                        break;
                    case "Karta Graficzna":
                        graphics = value;
                        break;
                    // Dodaj inne pola, jeśli chcesz je uzupełniać z HTML-a
                    default:
                        System.out.println("ℹ️ Pominięto nieobsługiwany wiersz: " + label);
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
                    screenSizeInches,
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
    /**
     * Pomocnicza metoda — wyciąga pierwszą sekwencję cyfr z URL-a jako ID.
     */
    private String extractProductIdFromUrl(String url) {
        for (String part : url.split("-")) {
            if (part.matches("\\d+")) {
                return part;
            }
        }
        return "0";
    }
}
