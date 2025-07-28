package com.example.scraper.repository;

import com.example.scraper.model.LaptopAukcjaJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;



import org.springframework.stereotype.Repository;

@Transactional
@Repository
public interface LaptopAukcjaRepository extends JpaRepository<LaptopAukcjaJPA, Long> {

    Optional<LaptopAukcjaJPA> findFirstByOrderByIdAsc();

    boolean existsByAuctionPage(String auctionPage);

    List<LaptopAukcjaJPA> findByCompletedFalse();

    @Modifying
    @Query("UPDATE LaptopAukcjaJPA l SET l.completed = false")
    void markAllAsIncomplete();

    @Modifying
    @Query("DELETE FROM LaptopAukcjaJPA l WHERE l.completed = false")
    void deleteByCompletedFalse();

}

