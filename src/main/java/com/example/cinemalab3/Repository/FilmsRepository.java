package com.example.cinemalab3.Repository;

import com.example.cinemalab3.Models.Films;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FilmsRepository extends JpaRepository<Films, Long> {
    @Query("SELECT f FROM Films f " +
            "WHERE (:title IS NULL OR LOWER(f.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:studio IS NULL OR LOWER(f.studio) LIKE LOWER(CONCAT('%', :studio, '%'))) " +
            "AND (:sessionDateTime IS NULL OR f.sessionDateTime = :sessionDateTime) " +
            "AND (:ticketCount IS NULL OR f.ticketCount = :ticketCount)")
    List<Films> findByParams(
            @Param("title") String title,
            @Param("studio") String studio,
            @Param("sessionDateTime") LocalDateTime sessionDateTime,
            @Param("ticketCount") Integer ticketCount,
            Sort sort);
    @Query("SELECT DATE(f.sessionDateTime), COUNT(f) FROM Films f GROUP BY DATE(f.sessionDateTime)")
    List<Object[]> findFilmIssueStats();

}
