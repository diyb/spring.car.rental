package com.example.demo.repository;

import com.example.demo.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByOrderByStartDateDesc();

    List<Booking> findByUserIdOrderByStartDateDesc(Long userId);

    boolean existsByCarId(Long carId);

    /**
     * Kas autol on sellel perioodil juba aktiivne broneering?
     * Lõppkuupäev on tagastamise päev, seega järjestikused broneeringud
     * (üks lõpeb 10., teine algab 10.) ei kattu.
     */
    @Query("""
            select count(b) > 0 from Booking b
            where b.car.id = :carId
              and b.status = :status
              and b.startDate < :endDate
              and b.endDate > :startDate
            """)
    boolean existsOverlapping(@Param("carId") Long carId,
                              @Param("status") String status,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);
}
