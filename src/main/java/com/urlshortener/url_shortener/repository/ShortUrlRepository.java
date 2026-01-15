package com.urlshortener.url_shortener.repository;

import com.urlshortener.url_shortener.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    @Query("""
    SELECT s.shortCode
    FROM ShortUrl s
    WHERE s.expiresAt IS NOT NULL
    AND s.expiresAt < :now
""")
    List<String> findExpiredShortCodes(@Param("now") LocalDateTime now);


    @Modifying
    @Transactional
    @Query("""
    UPDATE ShortUrl s
    SET s.clickCount = s.clickCount + :clicks
    WHERE s.shortCode = :shortCode
""")
    int incrementClickCount(
            @Param("shortCode") String shortCode,
            @Param("clicks") long clicks
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM ShortUrl s WHERE s.expiresAt IS NOT NULL AND s.expiresAt < :now")
    int deleteExpired(@Param("now") LocalDateTime now);

    @Query("""
    SELECT s.shortCode, s.clickCount
    FROM ShortUrl s
    ORDER BY s.clickCount DESC
""")
    List<Object[]> findTopUrls(org.springframework.data.domain.Pageable pageable);
}