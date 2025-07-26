package com.example.scraper.controller;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.stream.Collectors;

import com.example.scraper.model.LaptopAukcja;
import com.example.scraper.model.LaptopAukcjaJPA;
import com.example.scraper.repository.LaptopAukcjaRepository;


import java.util.Collections;
import com.example.scraper.service.LaptopScraperLaurem;


@RestController
@RequestMapping("/api")
public class LaptopController {

    private final LaptopScraperLaurem scraper;

    private final LaptopAukcjaRepository repo;

    public LaptopController(LaptopScraperLaurem scraper, LaptopAukcjaRepository repo) {
        this.scraper = scraper;
        this.repo = repo;
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
                entity.setTitle(laptop.name());
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
            entity.setAuctionTitle(laptop.name());
            entity.setItemCondition(laptop.condition());
            entity.setRamAmount(laptop.ramAmount());
            entity.setDiskType(laptop.diskType());
            entity.setDiskSize(laptop.diskSize());
            entity.setCpuModel(laptop.cpuModel());
            entity.setCpuFrequencyGHz(laptop.cpuFrequencyGHz());
            entity.setCpuCores(laptop.cpuCores());
            entity.setScreenType(laptop.screenType());
            entity.setFoldingScreen(laptop.foldingScreen());
            entity.setTouchScreen(laptop.touchScreen());
            entity.setScreenSizeInches(laptop.screenSizeInches());
            entity.setResolution(laptop.resolution());
            entity.setGraphics(laptop.graphics());
            entity.setOperatingSystem(laptop.operatingSystem());
            entity.setTitle(laptop.name());
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
                    entity.getItemCondition(),       // stan
                    entity.getRamAmount(),           // RAM
                    entity.getDiskType(),            // typ dysku
                    entity.getDiskSize(),            // pojemność dysku
                    entity.getCpuModel(),            // CPU
                    entity.getCpuFrequencyGHz(),     // taktowanie
                    entity.getCpuCores(),            // liczba rdzeni
                    entity.getScreenType(),          // typ ekranu
                    entity.getFoldingScreen(),       // ekran składany
                    entity.getTouchScreen(),         // ekran dotykowy
                    entity.getScreenSizeInches(),    // przekątna
                    entity.getResolution(),          // rozdzielczość
                    entity.getGraphics(),            // grafika
                    entity.getOperatingSystem()      // system operacyjny
            ))
            .toList();
    }

}
