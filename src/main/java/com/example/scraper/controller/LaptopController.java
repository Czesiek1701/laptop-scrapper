package com.example.scraper.controller;

import com.example.scraper.model.LaptopAukcja;
import com.example.scraper.model.LaptopAukcjaJPA;
import com.example.scraper.repository.LaptopAukcjaRepository;
import com.example.scraper.service.LaptopScraperLaurem;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;


import java.util.List;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/api/laptops")
public class LaptopController {

    private final LaptopScraperLaurem scraper;
    private final LaptopAukcjaRepository repo;

    public LaptopController(LaptopScraperLaurem scraper, LaptopAukcjaRepository repo) {
        this.scraper = scraper;
        this.repo = repo;
    }

    /** Odświeża dane: scrap + upsert + usuwanie starych */
    @Transactional
    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshAll() {
        List<LaptopAukcja> scraped = scraper.getLaptops();
        upsertScraped(scraped);
        removeStaleRecords(scraped);
        return ResponseEntity.ok().build();
    }

    /** Pobiera dane i zapisuje tylko nowe aukcje (krótka forma) */
    @Transactional
    @GetMapping("/scrap")
    public List<LaptopAukcja> getLaptops() {
        List<LaptopAukcja> scraped = scraper.getLaptops();

        List<LaptopAukcjaJPA> newEntries = scraped.stream()
                .filter(laptop -> !repo.existsByAuctionPage(laptop.auctionPage()))
                .map(laptop -> {
                    LaptopAukcjaJPA entity = new LaptopAukcjaJPA();
                    entity.setAuctionPage(laptop.auctionPage());
                    entity.setAuctionTitle(laptop.auctionTitle());
                    entity.setCompleted(false);
                    return entity;
                }).toList();

        repo.saveAll(newEntries);
        return scraped;
    }

    /** Zapisuje pełne dane laptopów (z listy) */
    @Transactional
    @PostMapping("/save")
    public ResponseEntity<String> saveLaptops(@RequestBody List<LaptopAukcja> laptops) {
        List<LaptopAukcjaJPA> entities = laptops.stream()
                .map(this::mapDtoToEntity)
                .toList();

        repo.saveAll(entities);
        return ResponseEntity.ok("Laptopy zapisane: " + entities.size());
    }

    // --- Prywatne metody pomocnicze ---

    private void upsertScraped(List<LaptopAukcja> scraped) {
        Map<String, LaptopAukcjaJPA> existing = repo.findAll().stream()
                .collect(Collectors.toMap(LaptopAukcjaJPA::getAuctionPage, e -> e));

        LocalDateTime now = LocalDateTime.now();
        List<LaptopAukcjaJPA> toSave = new ArrayList<>();

        for (LaptopAukcja dto : scraped) {
            LaptopAukcjaJPA entity = existing.getOrDefault(dto.auctionPage(), new LaptopAukcjaJPA());
            mapDtoToEntity(dto, entity);
            entity.setCompleted(true);
            if (entity.getCreatedAt() == null) entity.setCreatedAt(now);
            toSave.add(entity);
        }

        repo.saveAll(toSave);
    }

    private void removeStaleRecords(List<LaptopAukcja> scraped) {
        Set<String> livePages = scraped.stream()
                .map(LaptopAukcja::auctionPage)
                .collect(Collectors.toSet());

        List<LaptopAukcjaJPA> stale = repo.findAll().stream()
                .filter(e -> !livePages.contains(e.getAuctionPage()))
                .toList();

        repo.deleteAll(stale);
    }

    private LaptopAukcjaJPA mapDtoToEntity(LaptopAukcja dto) {
        LaptopAukcjaJPA entity = new LaptopAukcjaJPA();
        return mapDtoToEntity(dto, entity);
    }

    private LaptopAukcjaJPA mapDtoToEntity(LaptopAukcja src, LaptopAukcjaJPA trg) {
        trg.setAuctionPage(src.auctionPage());
        trg.setAuctionTitle(src.auctionTitle());
        trg.setManufacturer(src.manufacturer());
        trg.setModel(src.model());
        trg.setPrice(src.price());
        trg.setItemCondition(src.condition());
        trg.setRamAmount(src.ramAmount());
        trg.setDiskType(src.diskType());
        trg.setDiskSize(src.diskSize());
        trg.setCpuModel(src.cpuModel());
        trg.setCpuFrequencyGHz(src.cpuFrequencyGHz());
        trg.setCpuCores(src.cpuCores());
        trg.setScreenType(src.screenType());
        trg.setTouchScreen(src.touchScreen());
        trg.setScreenSizeInches(src.screenSizeInches());
        trg.setResolution(src.resolution());
        trg.setGraphics(src.graphics());
        trg.setOperatingSystem(src.operatingSystem());
        trg.setCreatedAt(LocalDateTime.now());
        return trg;
    }


    @Transactional(readOnly = true)
    @GetMapping("/readfromDB")
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
    @GetMapping("/complete")
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
