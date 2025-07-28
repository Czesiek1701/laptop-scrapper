package com.example.scraper.service;

import com.example.scraper.model.LaptopAukcja;
import com.example.scraper.model.LaptopAukcjaJPA;
import com.example.scraper.repository.LaptopAukcjaRepository;

import com.example.scraper.model.LaptopMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public abstract class LaptopScraperService {

    protected String LISTING_URL = "https://laurem.pl/pol_m_Laptopy-100.html";
    protected final LaptopAukcjaRepository repo;


    public LaptopScraperService(LaptopAukcjaRepository repo) {
        this.repo = repo;
    }

    public abstract List<LaptopAukcja> getLaptops();


    public abstract LaptopAukcja scrapeLaptopDetails(String url);


    public void upsertScraped(List<LaptopAukcja> scraped) {
        // Mapowanie istniejących rekordów z bazy po auctionPage
        Map<String, LaptopAukcjaJPA> existing = repo.findAll().stream()
                .collect(Collectors.toMap(LaptopAukcjaJPA::getAuctionPage, e -> e));

        LocalDateTime now = LocalDateTime.now();
        List<LaptopAukcjaJPA> toSave = new ArrayList<>();

        for (LaptopAukcja dto : scraped) {
            // Znajdź istniejący rekord lub utwórz nowy
            LaptopAukcjaJPA entity = existing.getOrDefault(dto.auctionPage(), new LaptopAukcjaJPA());

            // Zmapuj dane DTO do encji
            LaptopMapper.mapDtoToEntity(dto, entity);

            // Oznacz jako "kompletne" i ustaw datę utworzenia, jeśli brak
            entity.setCompleted(true);
            if (entity.getCreatedAt() == null) entity.setCreatedAt(now);

            toSave.add(entity);
        }

        // Zapisz wszystkie nowe/zmodyfikowane encje do bazy
        repo.saveAll(toSave);
    }


    static public String getDomain(String url) {
        if (url.contains("laurem.pl")) {
            return "laurem";
        }
        if (url.contains("amso.pl")) {
            return "amso";
        }
        return "";
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
//private String extractProductIdFromUrl(String url) {
//    Matcher m = Pattern.compile("-(\\d+)\\.html$").matcher(url);
//    return m.find() ? m.group(1) : "0";
//}
