package com.example.scraper;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class LaptopScraper {

    public List<String> getLaptops() {
        List<String> laptops = new ArrayList<>();

        try {
            // Ustaw ścieżkę do geckodriver.exe dynamicznie
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                System.setProperty("webdriver.gecko.driver", "C:\\webdrivers\\geckodriver.exe");
            } else {
                System.setProperty("webdriver.gecko.driver", "/usr/local/bin/geckodriver");
            }

            // Sprawdź, czy plik geckodriver istnieje
            String driverPath = System.getProperty("webdriver.gecko.driver");
            if (!new File(driverPath).exists()) {
                System.out.println("❌ Nie znaleziono geckodriver: " + driverPath);
                return laptops;
            }

            FirefoxOptions options = new FirefoxOptions();
            options.addArguments("--headless"); // uruchom bez okna

            WebDriver driver = new FirefoxDriver(options);

            try {
                driver.get("https://laurem.pl/pol_m_Laptopy-100.html");

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
                driver.quit(); // zamknij przeglądarkę
            }

        } catch (Exception e) {
            System.out.println("Błąd podczas scrape'owania laptopów:");
            e.printStackTrace();
        }

        return laptops;
    }
}
