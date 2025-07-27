

package com.example.scraper.repository;

import com.example.scraper.model.LaptopAukcjaJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;


import org.springframework.stereotype.Repository;


@Repository
public interface LaptopAukcjaRepository extends JpaRepository<LaptopAukcjaJPA, Long> {

    Optional<LaptopAukcjaJPA> findFirstByOrderByIdAsc();

    boolean existsByAuctionPage(String auctionPage);

    List<LaptopAukcjaJPA> findByCompletedFalse();

}

