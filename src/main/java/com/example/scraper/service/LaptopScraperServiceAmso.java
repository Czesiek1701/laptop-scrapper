package com.example.scraper.service;

import com.example.scraper.model.LaptopAukcja;
import com.example.scraper.model.LaptopAukcjaJPA;
import com.example.scraper.model.LaptopMapper;
import com.example.scraper.repository.LaptopAukcjaRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LaptopScraperServiceAmso extends LaptopScraperService {

    protected static final String LISTING_URL = "https://amso.pl/pol_m_Produkty_Laptopy-poleasingowe-188.html";
    //private final LaptopAukcjaRepository repo;

    public LaptopScraperServiceAmso(LaptopAukcjaRepository repo) {
        super(repo);
    }

    @Override
    public List<LaptopAukcja> getLaptops() {
        System.out.println("Scrapowanie Amso Inside");
        List<LaptopAukcja> out = new ArrayList<>();
        try {
            Document listing = Jsoup.connect(LISTING_URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(30_000)
                    .maxBodySize(0)
                    .followRedirects(true)
                    .get();

            Elements cards = listing.select("a.product__name");
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
            laptop.setAuctionTitle(
                    h1 != null ? h1.text().trim() : "Brak tytułu"
            );

            // Seria (status): Klasa A B C
            laptop.setItemCondition(
                    Optional.ofNullable(doc.selectFirst(".technical__info .projector_multiversions__item_link.--selected"))
                            .map(Element::text)
                            .map(String::trim)
                            .orElse("N/A")
            );

            // Cechy
            for (Element param : doc.select(".projector_desc_param_wrapper #projector_dictionary .dictionary__param")) {

                // Etykieta (label)
                String label = Optional.ofNullable(param.selectFirst(".dictionary__name_txt"))
                        .map(Element::text)
                        .map(s -> s.replace(":", "").trim())
                        .orElse("N/A");
//                System.out.println("  Label znaleziony: " + label);

                // Wartość (value)
                String value = Optional.ofNullable(param.selectFirst(".dictionary__value_txt"))
                        .map(Element::text)
                        .map(String::trim)
                        .orElse("N/A");
//                System.out.println("  Value znalezione: " + value);

                switch (label) {
                    case "Model"                        -> laptop.setModel(value);
                    case "Marka"                        -> laptop.setManufacturer(value);
                    case "Taktowanie procesora (GHz)"  -> {
                        String cleanValue = value.endsWith("GHz") ? value : value + " GHz";
                        laptop.setCpuFrequencyGHz(cleanValue);
                    }
                    case "Liczba rdzeni procesora"     -> laptop.setCpuCores(value);
                    case "Pamięć RAM"                  -> {
                        laptop.setRamAmount(
                                value.replaceAll("[^0-9]", "").concat(" GB")
                        );
                    }
                    case "Typ dysku twardego"          -> laptop.setDiskType(value);
                    case "Dysk"                         -> {
                        laptop.setDiskSize(
                                value.replaceAll("[^0-9]", "").concat(" GB")
                        );
                    }
                    case "Procesor"                     -> laptop.setCpuModel(value);
                    case "Typ matrycy"                 -> laptop.setScreenType(value);
                    case "Ekran dotykowy"              -> laptop.setTouchScreen(value);
                    case "Przekątna ekranu w calach"  -> {
                        // Dodaj " jeśli nie ma
                        String cleanValue = value.endsWith("\"") ? value : value + "\"";
                        laptop.setScreenSizeInches(cleanValue);
                    }
                    case "Rozdzielczość natywna"      -> laptop.setResolution(value);
                    case "Model karty graficznej"      -> laptop.setGraphics(value);
                    case "System operacyjny"           -> laptop.setOperatingSystem(value);
                    case "Stan techniczny"             -> laptop.setItemCondition(value);
                }
            }

            // Cena
            laptop.setPrice(
                    Optional.ofNullable(doc.selectFirst(".projector_prices__price_wrapper #projector_price_value"))
                            .map(Element::text)
                            .map(s -> s.replace("\u00A0", " ").trim())
                            .orElse("Brak ceny")
            );

            // Timestamp
            laptop.setCreatedAt(
                    LocalDateTime.now()
            );

            System.out.println("Model: " + laptop.getModel());

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
