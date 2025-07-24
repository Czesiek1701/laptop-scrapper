package com.example.scraper.controller;

import com.example.scraper.model.LaptopAukcjaJPA;
import com.example.scraper.repository.LaptopAukcjaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class LaptopJPAController {

    private final LaptopAukcjaRepository repo;

    @Autowired
    public LaptopJPAController(LaptopAukcjaRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/jpa/laptops/first")
    public String showFirstLaptopTitle() {
        Optional<LaptopAukcjaJPA> pierwszy = repo.findFirstByOrderByIdAsc();

        return pierwszy
                .map(l -> "📄 Pierwszy laptop w bazie to: " + l.getTitle())
                .orElse("😕 Brak danych w bazie.");
    }
}
