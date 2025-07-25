package com.example.scraper;

import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;


import java.util.Collections;
import java.util.List;

import com.example.scraper.model.LaptopAukcja;
import com.example.scraper.model.LaptopAukcjaJPA;
import com.example.scraper.service.LaptopScraperLaurem;
import com.example.scraper.repository.LaptopAukcjaRepository;


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
            }).toList();

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

}
