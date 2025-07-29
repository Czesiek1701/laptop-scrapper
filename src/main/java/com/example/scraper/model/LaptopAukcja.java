// DTO

package com.example.scraper.model;

public record LaptopAukcja(
        String id,
        String auctionPage,
        String auctionTitle,
        String manufacturer,
        String model,
        String price,
        String condition,
        String ramAmount,
        String diskType,
        String diskSize,
        String cpuModel,
        String cpuFrequencyGHz,
        String cpuCores,
        String screenType,
        String touchScreen,
        String screenSizeInches,
        String resolution,
        String graphics,
        String operatingSystem,
        String multimedia
) {


    public float getFloatPrice() {
        if (price == null || price.isBlank()) return 0f;

        try {
            // "1 200,50 zł" → "1200.50"
            String clean = price
                    .replaceAll("[^\\d,\\.]", "")   // usuń wszystko oprócz cyfr i przecinka
                    .replace(",", ".")              // zamień przecinek na kropkę

                    .replaceAll("\\s", "");         // usuń spacje jeśli są

            return Float.parseFloat(clean);
        } catch (NumberFormatException e) {
            return 0f; // fallback na nieprawidłową cenę
        }
    }

}



