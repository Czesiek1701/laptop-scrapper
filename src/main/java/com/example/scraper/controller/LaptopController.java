package com.example.scraper.controller;

import com.example.scraper.model.LaptopAukcja;
import com.example.scraper.model.LaptopAukcjaJPA;
import com.example.scraper.repository.LaptopAukcjaRepository;
import com.example.scraper.service.LaptopScraperService;
import com.example.scraper.service.LaptopScraperServiceLaurem;
import com.example.scraper.service.LaptopScraperServiceAmso;
import com.example.scraper.model.LaptopMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;


import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;


import java.util.List;


@RestController
@RequestMapping("/api/laptops")
public class LaptopController {

    private final LaptopScraperServiceLaurem scraper1;
    private final LaptopScraperServiceAmso scraper2;
    private final LaptopAukcjaRepository repo;
//    private final LaptopMapper laptopMapper;

    public LaptopController(
            LaptopScraperServiceLaurem scraper1,
            LaptopScraperServiceAmso scraper2,
            LaptopAukcjaRepository repo
    ) {
        this.scraper1 = scraper1;
        this.scraper2 = scraper2;
        this.repo = repo;
//        this.laptopMapper = laptopMapper;
    }

    /** Odświeża dane: scrap + upsert + usuwanie starych */
    @Transactional
    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshAll() {
        repo.markAllAsIncomplete();

        List<LaptopAukcja> scraped = new ArrayList<>();
        scraped.addAll(scraper1.getLaptops());
        scraped.addAll(scraper2.getLaptops());

        scraper1.upsertScraped(scraped); // to edytowac zeby ozanczało compelted
        //scraper.removeStaleRecords(scraped); // to usunac
        repo.deleteByCompletedFalse();
        return ResponseEntity.ok().build();
    }


    /** Pobiera dane i zapisuje tylko nowe aukcje (krótka forma) */
    @Transactional
    @GetMapping("/scrapLinks")
    public List<LaptopAukcja> getLaptops() {
        List<LaptopAukcja> scraped = new ArrayList<>();
        System.out.println("Scrapowanie Laurem");
        scraped.addAll(scraper1.getLaptops());
        System.out.println("Scrapowanie Amso");
        scraped.addAll(scraper2.getLaptops());
        System.out.println("Scrapowanie Koniec");

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
                .map(LaptopMapper::mapDtoToEntity)
                .toList();

        repo.saveAll(entities);
        return ResponseEntity.ok("Laptopy zapisane: " + entities.size());
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
            LaptopAukcja result = null;
            if (url.contains("laurem.pl"))
                result = scraper1.scrapeLaptopDetails(url);
            else if (url.contains("amso.pl"))
                result = scraper2.scrapeLaptopDetails(url);

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
                String url = entity.getAuctionPage();

                LaptopAukcja details = null;
                if (url.contains("laurem.pl"))
                    details = scraper1.scrapeLaptopDetails(url);
                else if (url.contains("laurem.pl"))
                    details = scraper2.scrapeLaptopDetails(url);

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
