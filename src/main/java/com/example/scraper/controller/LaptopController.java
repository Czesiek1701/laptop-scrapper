package com.example.scraper.controller;


import com.example.scraper.model.LaptopAukcja;
import com.example.scraper.model.LaptopAukcjaJPA;
import com.example.scraper.repository.LaptopAukcjaRepository;
import com.example.scraper.service.LaptopScraperLaurem;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.stream.Collectors;

import com.example.scraper.model.LaptopAukcja;
import com.example.scraper.model.LaptopAukcjaJPA;
import com.example.scraper.repository.LaptopAukcjaRepository;


import java.util.Collections;
import com.example.scraper.service.LaptopScraperLaurem;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class LaptopController {

    private final LaptopScraperLaurem scraper;
    private final LaptopAukcjaRepository repo;

    public LaptopController(LaptopScraperLaurem scraper, LaptopAukcjaRepository repo) {
        this.scraper = scraper;
        this.repo = repo;
    }


    /**
     * POST /api/laptops/refresh
     * 0) resetujemy completed = false
     * 1) scrapujemy pełne dane
     * 2) upsertujemy + ustawiamy completed = true
     * 3) usuwamy nieistniejące już rekordy
     */
    @Transactional
    @PostMapping("/laptops/refresh")
    public ResponseEntity<Void> refreshAll() {
        resetCompletedFlags();
        List<LaptopAukcja> scraped = scraper.getLaptops();
        upsertScraped(scraped);
        removeStaleRecords(scraped);
        return ResponseEntity.ok().build();
    }

    // 0) reset completed = false
    private void resetCompletedFlags() {
        List<LaptopAukcjaJPA> all = repo.findAll();
        all.forEach(e -> e.setCompleted(false));
        repo.saveAll(all);
    }

    // 1+2) insert lub update każdej aukcji
    private void upsertScraped(List<LaptopAukcja> scraped) {
        Map<String, LaptopAukcjaJPA> existing = repo.findAll()
                .stream()
                .collect(Collectors.toMap(
                        LaptopAukcjaJPA::getAuctionPage,
                        e -> e
                ));

        List<LaptopAukcjaJPA> toSave = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (LaptopAukcja dto : scraped) {
            LaptopAukcjaJPA entity = existing
                    .getOrDefault(dto.auctionPage(), new LaptopAukcjaJPA());
            mapDtoToEntity(dto, entity);

            // ustawiamy completed oraz pierwszy znacznik createdAt
            entity.setCompleted(true);
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(now);
            }

            toSave.add(entity);
        }

        repo.saveAll(toSave);
    }

    // 3) usuwamy rekordy, których nie ma w aktualnym scrapie
    private void removeStaleRecords(List<LaptopAukcja> scraped) {
        Set<String> livePages = scraped.stream()
                .map(LaptopAukcja::auctionPage)
                .collect(Collectors.toSet());

        List<LaptopAukcjaJPA> stale = repo.findAll()
                .stream()
                .filter(e -> !livePages.contains(e.getAuctionPage()))
                .collect(Collectors.toList());

        if (!stale.isEmpty()) {
            repo.deleteAll(stale);
        }
    }

    // mapowanie z DTO na encję (bez updatedAt)
    private void mapDtoToEntity(LaptopAukcja src, LaptopAukcjaJPA trg) {
        trg.setAuctionPage(     src.auctionPage()       );
        trg.setAuctionTitle(    src.auctionTitle()      );
        trg.setManufacturer(    src.manufacturer()      );
        trg.setModel(           src.model()             );
        trg.setPrice(           src.price()             );
        trg.setItemCondition(   src.condition()         );
        trg.setRamAmount(       src.ramAmount()         );
        trg.setDiskType(        src.diskType()          );
        trg.setDiskSize(        src.diskSize()          );
        trg.setCpuModel(        src.cpuModel()          );
        trg.setCpuFrequencyGHz( src.cpuFrequencyGHz()   );
        trg.setCpuCores(        src.cpuCores()          );
        trg.setScreenType(      src.screenType()        );
        trg.setTouchScreen(     src.touchScreen()       );
        trg.setScreenSizeInches(src.screenSizeInches()  );
        trg.setResolution(      src.resolution()        );
        trg.setGraphics(        src.graphics()          );
        trg.setOperatingSystem( src.operatingSystem()   );
    }

        @Transactional
    @GetMapping("/laptops/scrap")
    public List<LaptopAukcja> getLaptops() {
        System.out.println("Scrap + krótki zapis");
        try {
//            System.out.println("🔐 Zmienna SPRING_DATASOURCE_PASSWORD: " + System.getenv("SPRING_DATASOURCE_PASSWORD"));
            List<LaptopAukcja> laptopy_nazwa_link = scraper.getLaptops();

            List<LaptopAukcjaJPA> entities = laptopy_nazwa_link.stream().map(laptop -> {
                LaptopAukcjaJPA entity = new LaptopAukcjaJPA();
                entity.setAuctionPage(laptop.auctionPage());
                entity.setAuctionTitle(laptop.auctionTitle());
                entity.setCompleted(false);
                return entity;
            }).filter(entity -> !repo.existsByAuctionPage(entity.getAuctionPage())).toList();

            repo.saveAll(entities);

            return laptopy_nazwa_link;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

    }

    @Transactional
    @PostMapping("/laptops/save")
    public String saveLaptops(List<LaptopAukcja> scraped_laptops){
//        List<LaptopAukcja> scraped = scraper.getLaptops();
        System.out.println("Zapisywanie...");
        List<LaptopAukcjaJPA> entities = scraped_laptops.stream().map(laptop -> {
            LaptopAukcjaJPA entity = new LaptopAukcjaJPA();

            entity.setAuctionPage(laptop.auctionPage());
            entity.setManufacturer(laptop.manufacturer());
            entity.setModel(laptop.model());
            entity.setPrice(laptop.price());
            entity.setAuctionTitle(laptop.auctionTitle());
            entity.setItemCondition(laptop.condition());
            entity.setRamAmount(laptop.ramAmount());
            entity.setDiskType(laptop.diskType());
            entity.setDiskSize(laptop.diskSize());
            entity.setCpuModel(laptop.cpuModel());
            entity.setCpuFrequencyGHz(laptop.cpuFrequencyGHz());
            entity.setCpuCores(laptop.cpuCores());
            entity.setScreenType(laptop.screenType());
            entity.setTouchScreen(laptop.touchScreen());
            entity.setScreenSizeInches(laptop.screenSizeInches());
            entity.setResolution(laptop.resolution());
            entity.setGraphics(laptop.graphics());
            entity.setOperatingSystem(laptop.operatingSystem());
            entity.setAuctionTitle(laptop.auctionTitle());
            entity.setCreatedAt(java.time.LocalDateTime.now());

            return entity;
        }).toList();

        System.out.println("Do zapisu: " + entities.size());  // powinno być 65
        repo.saveAll(entities);
        System.out.println("Zapis zakończony");

        return "Laptopy zapisane do bazy: " + entities.size();
    }


    @Transactional(readOnly = true)
    @GetMapping("/laptops/readfromDB")
    public List<LaptopAukcja> readFromDB() {
        return repo.findAll().stream()
            .map(entity -> new LaptopAukcja(
                    String.valueOf(entity.getId()),
                    entity.getAuctionPage(),         // link do aukcji
                    entity.getAuctionTitle(),        // tytuł aukcji
                    entity.getManufacturer(),        // producent
                    entity.getModel(),               // model
                    entity.getPrice(),
                    entity.getItemCondition(),       // stan
                    entity.getRamAmount(),           // RAM
                    entity.getDiskType(),            // typ dysku
                    entity.getDiskSize(),            // pojemność dysku
                    entity.getCpuModel(),            // CPU
                    entity.getCpuFrequencyGHz(),     // taktowanie
                    entity.getCpuCores(),            // liczba rdzeni
                    entity.getScreenType(),          // typ ekranu
                    entity.getTouchScreen(),         // ekran dotykowy
                    entity.getScreenSizeInches(),    // przekątna
                    entity.getResolution(),          // rozdzielczość
                    entity.getGraphics(),            // grafika
                    entity.getOperatingSystem()      // system operacyjny
            ))
            .toList();
    }


    /**
     * GET /api/laptop?url={productUrl}
     * Zwraca bezpośrednio LaptopAukcja (status 200 OK domyślnie).
     */
    @GetMapping("/laptop")
    public LaptopAukcja scrapeSingleLaptop(@RequestParam("url") String url) {
        System.out.println("🔍 Scrapowanie pojedynczego laptopa z URL: " + url);

        if (url == null || url.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Parametr 'url' nie może być pusty"
            );
        }

        if (!url.matches("^https?://.*$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Niepoprawny format URL"
            );
        }

        try {
            LaptopAukcja result = scraper.scrapeLaptopDetails(url);
            if (result == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Nie znaleziono danych dla URL: " + url
                );
            }
            return result;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Błąd scrapowania: " + e.getMessage(), e
            );
        }
    }

    /**
     * POST /api/laptops/complete
     * Dla każdej aukcji z completed=false pobiera szczegóły i je zapisuje.
     */
    @Transactional
    @GetMapping("/laptops/complete")
    public String completeLaptopDetails() {
        // 1. Pobierz wszystkie nieukończone wpisy
        List<LaptopAukcjaJPA> toComplete = repo.findByCompletedFalse();
        if (toComplete.isEmpty()) {
            return "Brak nowych wpisów do uzupełnienia.";
        }

        // 2. Dla każdego wywołaj scraper i zaktualizuj pola
        List<LaptopAukcjaJPA> updated = toComplete.stream().map(entity -> {
            try {
                LaptopAukcja details = scraper.scrapeLaptopDetails(entity.getAuctionPage());
                if (details != null) {
                    entity.setManufacturer(details.manufacturer());
                    entity.setModel(details.model());
                    entity.setPrice(details.price());
                    entity.setAuctionTitle(details.auctionTitle());
                    entity.setItemCondition(details.condition());
                    entity.setRamAmount(details.ramAmount());
                    entity.setDiskType(details.diskType());
                    entity.setDiskSize(details.diskSize());
                    entity.setCpuModel(details.cpuModel());
                    entity.setCpuFrequencyGHz(details.cpuFrequencyGHz());
                    entity.setCpuCores(details.cpuCores());
                    entity.setScreenType(details.screenType());
                    entity.setTouchScreen(details.touchScreen());
                    entity.setScreenSizeInches(details.screenSizeInches());
                    entity.setResolution(details.resolution());
                    entity.setGraphics(details.graphics());
                    entity.setOperatingSystem(details.operatingSystem());
                    entity.setCreatedAt(java.time.LocalDateTime.now());
                    entity.setCompleted(true);
                }
            } catch (Exception e) {
                // loguj błąd i nie przerwij całej operacji
                System.err.println("Błąd przy uzupełnianiu: " + e.getMessage());
            }
            return entity;
        }).toList();

        // 3. Zapisz wszystkie zmodyfikowane rekordy
        repo.saveAll(updated);

        return "Uzupełniono szczegóły dla " + updated.size() + " wpisów.";
    }


}
