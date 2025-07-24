package com.example.scraper.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
@Table(name = "laptop_aukcje")
public class LaptopAukcjaJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String auctionPage;
    private String manufacturer;
    private String model;
    private String auctionTitle;
    private String itemCondition;
    private String ramAmount;
    private String diskType;
    private String diskSize;
    private String cpuModel;
    private String cpuFrequencyGHz;
    private String cpuCores;
    private String screenType;
    private String foldingScreen;
    private String touchScreen;
    private String screenSizeInches;
    private String resolution;
    private String graphics;
    private String operatingSystem;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private String title;

    // Gettery i settery

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuctionPage() {
        return auctionPage;
    }

    public void setAuctionPage(String auctionPage) {
        this.auctionPage = auctionPage;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getAuctionTitle() {
        return auctionTitle;
    }

    public void setAuctionTitle(String auctionTitle) {
        this.auctionTitle = auctionTitle;
    }

    public String getItemCondition() {
        return itemCondition;
    }

    public void setItemCondition(String itemCondition) {
        this.itemCondition = itemCondition;
    }

    public String getRamAmount() {
        return ramAmount;
    }

    public void setRamAmount(String ramAmount) {
        this.ramAmount = ramAmount;
    }

    public String getDiskType() {
        return diskType;
    }

    public void setDiskType(String diskType) {
        this.diskType = diskType;
    }

    public String getDiskSize() {
        return diskSize;
    }

    public void setDiskSize(String diskSize) {
        this.diskSize = diskSize;
    }

    public String getCpuModel() {
        return cpuModel;
    }

    public void setCpuModel(String cpuModel) {
        this.cpuModel = cpuModel;
    }

    public String getCpuFrequencyGHz() {
        return cpuFrequencyGHz;
    }

    public void setCpuFrequencyGHz(String cpuFrequencyGHz) {
        this.cpuFrequencyGHz = cpuFrequencyGHz;
    }

    public String getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(String cpuCores) {
        this.cpuCores = cpuCores;
    }

    public String getScreenType() {
        return screenType;
    }

    public void setScreenType(String screenType) {
        this.screenType = screenType;
    }

    public String getFoldingScreen() {
        return foldingScreen;
    }

    public void setFoldingScreen(String foldingScreen) {
        this.foldingScreen = foldingScreen;
    }

    public String getTouchScreen() {
        return touchScreen;
    }

    public void setTouchScreen(String touchScreen) {
        this.touchScreen = touchScreen;
    }

    public String getScreenSizeInches() {
        return screenSizeInches;
    }

    public void setScreenSizeInches(String screenSizeInches) {
        this.screenSizeInches = screenSizeInches;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getGraphics() {
        return graphics;
    }

    public void setGraphics(String graphics) {
        this.graphics = graphics;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
