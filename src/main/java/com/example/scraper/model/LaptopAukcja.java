

package com.example.scraper.model;

import com.example.scraper.model.LaptopAukcja;

public record LaptopAukcja(
        String id,
        String auctionPage,
        String name,
        String manufacturer,
        String model,
        String condition,
        String ramAmount,
        String diskType,
        String diskSize,
        String cpuModel,
        String cpuFrequencyGHz,
        String cpuCores,
        String screenType,
        String foldingScreen,
        String touchScreen,
        String screenSizeInches,
        String resolution,
        String graphics,
        String operatingSystem
) {}



