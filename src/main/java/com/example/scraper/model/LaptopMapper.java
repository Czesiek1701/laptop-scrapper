package com.example.scraper.model;

import com.example.scraper.model.LaptopAukcja;
import com.example.scraper.model.LaptopAukcjaJPA;

import java.time.LocalDateTime;

public class LaptopMapper {

    public static LaptopAukcjaJPA mapDtoToEntity(LaptopAukcja dto) {
        return mapDtoToEntity(dto, new LaptopAukcjaJPA());
    }

    public static LaptopAukcjaJPA mapDtoToEntity(LaptopAukcja src, LaptopAukcjaJPA trg) {
        trg.setAuctionPage(src.auctionPage());
        trg.setAuctionTitle(src.auctionTitle());
        trg.setManufacturer(src.manufacturer());
        trg.setModel(src.model());
        trg.setPrice(src.price());
        trg.setItemCondition(src.condition());
        trg.setRamAmount(src.ramAmount());
        trg.setDiskType(src.diskType());
        trg.setDiskSize(src.diskSize());
        trg.setCpuModel(src.cpuModel());
        trg.setCpuFrequencyGHz(src.cpuFrequencyGHz());
        trg.setCpuCores(src.cpuCores());
        trg.setScreenType(src.screenType());
        trg.setTouchScreen(src.touchScreen());
        trg.setScreenSizeInches(src.screenSizeInches());
        trg.setResolution(src.resolution());
        trg.setGraphics(src.graphics());
        trg.setOperatingSystem(src.operatingSystem());
        trg.setCreatedAt(LocalDateTime.now());
        return trg;
    }
}
