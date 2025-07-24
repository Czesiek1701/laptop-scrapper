package com.example.scraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.scraper.repository.LaptopAukcjaRepository;
import org.springframework.beans.factory.annotation.Autowired;


@SpringBootApplication
public class ScraperApplication {

    @Autowired
    private LaptopAukcjaRepository repo;

    public static void main(String[] args) {
        System.setProperty("webdriver.gecko.driver", "/usr/local/bin/geckodriver");

        SpringApplication.run(ScraperApplication.class, args);
    }

}
