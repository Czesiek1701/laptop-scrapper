package com.example.scraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScraperApplication {

    public static void main(String[] args) {
        System.setProperty("webdriver.gecko.driver", "/usr/local/bin/geckodriver");

        SpringApplication.run(ScraperApplication.class, args);
    }

}
