package com.example.scraper.service;

import com.example.scraper.model.LaptopAukcja;
import com.example.scraper.model.LaptopAukcjaJPA;
import com.example.scraper.model.LaptopMapper;
import com.example.scraper.repository.LaptopAukcjaRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.stream.Collectors;

import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LaptopScraperServiceLaurem extends LaptopScraperService {

    protected static final String LISTING_URL = "https://laurem.pl/pol_m_Laptopy-100.html";
    //private final LaptopAukcjaRepository repo;


    public LaptopScraperServiceLaurem(LaptopAukcjaRepository repo) {
        super(repo);
    }

    @Override
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


    @Override
    public LaptopAukcja scrapeLaptopDetails(String url) {
        System.out.println("Rozpoczynam scrapowanie: " + url);
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .referrer("https://www.bing.com")
                    .timeout(30_000)
                    .maxBodySize(0)
                    .followRedirects(true)
                    .get();
            System.out.println("Pobrano dokument HTML");

            // Inicjalizacja encji JPA z konstruktora (tytuł i URL)
            LaptopAukcjaJPA laptop = new LaptopAukcjaJPA(url);

            // Tytuł aukcji
            Element h1 = doc.selectFirst("h1");
            laptop.setAuctionTitle(h1 != null ? h1.text().trim() : "Brak tytułu");

            // Producent
            laptop.setManufacturer(
                    Optional.ofNullable(doc.selectFirst(".basic_info .producer a.brand"))
                            .map(Element::text)
                            .map(String::trim)
                            .orElse("N/A")
            );

            // Seria (status): Diamentowy / Złoty / Srebrny
            laptop.setItemCondition(
                    Optional.ofNullable(doc.selectFirst(".basic_info .proj_laur"))
                            .map(Element::text)
                            .map(String::trim)
                            .orElse("N/A")
            );

            // Cechy
            for (Element trait : doc.select(".traits_info .param_trait")) {
                String label = Optional.ofNullable(trait.selectFirst("span"))
                        .map(Element::text)
                        .map(s -> s.replace(":", "").trim())
                        .orElse("N/A");

                String value = Optional.ofNullable(trait.selectFirst("strong.lt_description"))
                        .map(Element::text)
                        .map(String::trim)
                        .orElse("N/A");

                switch (label) {
                    case "Procesor"                 -> laptop.setCpuModel(value);
                    case "Pamięć RAM"               -> laptop.setRamAmount(value);
                    case "Dysk Twardy"              -> {
                        String[] parts = value.split("\\s+", 2);
                        laptop.setDiskSize((parts.length > 0 ? parts[0] : "N/A").replaceAll("[^0-9]", "").concat(" GB"));
                        laptop.setDiskType(parts.length > 1 ? parts[1] : "N/A");
                    }
                    case "Przekątna ekranu"         -> laptop.setScreenSizeInches(value);
                    case "Rozdzielczość ekranu"     -> laptop.setResolution(value);
                    case "Karta Graficzna"          -> laptop.setGraphics(value);
                }
            }

            // Dodatkowe cechy z tabeli
            for (Element row : doc.select("tbody tr")) {
                String label = Optional.ofNullable(row.selectFirst("td:nth-child(1) span"))
                        .map(Element::text)
                        .map(s -> s.replace(":", "").trim())
                        .orElse("N/A");

                String value = Optional.ofNullable(row.selectFirst("td:nth-child(2) .n54117_item_b_sub"))
                        .map(Element::text)
                        .map(String::trim)
                        .orElse("N/A");

                switch (label) {
                    case "Model"                    -> laptop.setModel(value);
                    case "Liczba rdzeni"            -> laptop.setCpuCores( value.replaceAll("[^0-9]", ""));
                    case "Taktowanie procesora"     -> laptop.setCpuFrequencyGHz(value.split("\\s+")[0] + " GHz");
                    case "Typ matrycy"              -> laptop.setScreenType(value);
                    case "Ekran dotykowy"           -> laptop.setTouchScreen(value);
                    case "Stan produktu"            -> laptop.setItemCondition(value); // nadpisz, jeśli lepsze
                    case "System operacyjny"        -> laptop.setOperatingSystem(value);
                }
            }

            // Cena
            laptop.setPrice(
                    Optional.ofNullable(doc.selectFirst("strong.projector_price_value"))
                            .map(Element::text)
                            .map(s -> s.replace("\u00A0", " ").trim())
                            .orElse("Brak ceny")
            );

            Elements options = doc.select("#mw_44 ul.options li");

            String systems = options.stream()
                    .map(el -> el.attr("data-title"))
                    .filter(s -> !s.isEmpty() && !s.equalsIgnoreCase("Brak systemu"))
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(Collectors.joining("\n"));

            laptop.setOperatingSystem(systems.isEmpty() ? "Brak systemu" : systems);


            // Timestamp
            laptop.setCreatedAt(
                    LocalDateTime.now()
            );

            System.out.println("Scrapowanie zakończone: " + laptop.getAuctionTitle());
            return LaptopMapper.mapEntityToDto(laptop);

        } catch (IOException e) {
            System.err.println("Błąd przy scrapowaniu szczegółów: " + e.getMessage());
            return null;
        }
    }



}

//public void removeStaleRecords(List<LaptopAukcja> scraped) {
//    // Zbierz wszystkie aktualne strony aukcji z nowych danych
//    Set<String> livePages = scraped.stream()
//            .map(LaptopAukcja::auctionPage)
//            .collect(Collectors.toSet());
//
//    // Filtruj rekordy, które już nie istnieją w najnowszym scrape
//    List<LaptopAukcjaJPA> stale = repo.findAll().stream()
//            .filter(e -> !livePages.contains(e.getAuctionPage()))
//            .toList();
//
//    // Usuń nieaktualne rekordy z bazy
//    repo.deleteAll(stale);
//}
//
//
//private static String cleanUnits(String value) {
//    return value
//            .replaceAll("(\\d+),(\\d+)\\s*GHz", "$1.$2 GHz")     // zamiana przecinków na kropki w GHz
//            .replaceAll("(\\d+)\\s*GB\\s*GB", "$1 GB")           // usunięcie duplikatów GB
//            .replaceAll("(\\d+)\\\"\\s*[”\"]?", "$1\"")          // uporządkowanie znaków cali
//            .replaceAll("\\s+", " ")                             // usunięcie zbędnych spacji
//            .trim();
//}
//
