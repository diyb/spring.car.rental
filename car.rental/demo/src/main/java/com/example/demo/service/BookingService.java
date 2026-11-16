package com.example.demo.service;

import com.example.demo.dto.BookingForm;
import com.example.demo.entity.AppUser;
import com.example.demo.entity.Booking;
import com.example.demo.entity.Car;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CarService carService;
    private final AppUserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          CarService carService,
                          AppUserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.carService = carService;
        this.userRepository = userRepository;
    }

    public List<Booking> findAll() {
        return bookingRepository.findAllByOrderByStartDateDesc();
    }

    public List<Booking> findByUser(Long userId) {
        return bookingRepository.findByUserIdOrderByStartDateDesc(userId);
    }

    /**
     * Hind = päevade arv × päevahind.
     * Päevade arv on lõppkuupäev miinus alguskuupäev (nt 10.–15. = 5 päeva).
     */
    public BigDecimal calculateTotalPrice(BigDecimal dailyPrice, LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return dailyPrice.multiply(BigDecimal.valueOf(days));
    }

    /**
     * Loob uue broneeringu. Viskab {@link IllegalArgumentException}, kui andmed ei sobi
     * (kuupäevad valed, auto pole saadaval või on perioodil juba broneeritud).
     */
    @Transactional
    public Booking create(BookingForm form, Long userId) {
        LocalDate start = form.getStartDate();
        LocalDate end = form.getEndDate();

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Lõppkuupäev peab olema hilisem kui alguskuupäev.");
        }
        if (start.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Alguskuupäev ei tohi olla minevikus.");
        }

        Car car = carService.findById(form.getCarId());
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Kasutajat ei leitud"));

        if (!Car.STATUS_AVAILABLE.equals(car.getStatus())) {
            throw new IllegalArgumentException("Auto " + car.getDisplayName() + " ei ole saadaval.");
        }
        if (bookingRepository.existsOverlapping(car.getId(), Booking.STATUS_ACTIVE, start, end)) {
            throw new IllegalArgumentException(
                    "Auto " + car.getDisplayName() + " on valitud perioodil juba broneeritud.");
        }

        Booking booking = new Booking();
        booking.setCar(car);
        booking.setUser(user);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setTotalPrice(calculateTotalPrice(car.getDailyPrice(), start, end));
        booking.setStatus(Booking.STATUS_ACTIVE);
        return bookingRepository.save(booking);
    }

    /**
     * Tühistab broneeringu.
     *
     * @param userId tühistaja ID; {@code null} tähendab administraatorit (tohib kõiki).
     */
    @Transactional
    public void cancel(Long id, Long userId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Broneeringut ID-ga " + id + " ei leitud"));
        if (userId != null && !booking.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Sul pole õigust seda broneeringut tühistada.");
        }
        booking.setStatus(Booking.STATUS_CANCELLED);
        bookingRepository.save(booking);
    }
}
