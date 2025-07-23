package com.example.scraper;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LaptopScraper {

    public List<String> getLaptops() {

        List<String> laptops = new ArrayList<>();

        // Ustaw ścieżkę do geckodriver.exe
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            System.setProperty("webdriver.gecko.driver", "C:\\webdrivers\\geckodriver.exe");
        } else {
            System.setProperty("webdriver.gecko.driver", "/usr/local/bin/geckodriver");
        }


        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Opcjonalnie: uruchom w tle, bez otwartego okna

        WebDriver driver = new FirefoxDriver(options);

        try {
            driver.get("https://laurem.pl/pol_m_Laptopy-100.html");

            // Pobieramy elementy z nazwami laptopów i linkami
            List<WebElement> products = driver.findElements(By.cssSelector("a.product-name"));

            for (WebElement product : products) {
                String name = product.getText();
                String link = product.getAttribute("href");

                System.out.println("Laptop: " + name);
                System.out.println("Link: " + link);
                System.out.println("---------------------------");

                laptops.add(name + " -> " + link);
            }

        } finally {
            driver.quit();  // zamknij przeglądarkę
        }

        return laptops;
    }
}
