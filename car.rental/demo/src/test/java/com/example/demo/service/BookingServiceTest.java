package com.example.demo.service;

import com.example.demo.dto.BookingForm;
import com.example.demo.entity.AppUser;
import com.example.demo.entity.Booking;
import com.example.demo.entity.Car;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookingServiceTest {

    private BookingRepository bookingRepository;
    private CarService carService;
    private AppUserRepository userRepository;
    private BookingService bookingService;
    private Car car;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        carService = mock(CarService.class);
        userRepository = mock(AppUserRepository.class);
        bookingService = new BookingService(bookingRepository, carService, userRepository);

        car = new Car();
        car.setId(1L);
        car.setBrand("Toyota");
        car.setModel("Corolla");
        car.setYear(2022);
        car.setDailyPrice(new BigDecimal("35.00"));
        car.setStatus(Car.STATUS_AVAILABLE);

        AppUser user = new AppUser();
        user.setId(1L);

        when(carService.findById(1L)).thenReturn(car);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private BookingForm form(LocalDate start, LocalDate end) {
        BookingForm form = new BookingForm();
        form.setCarId(1L);
        form.setStartDate(start);
        form.setEndDate(end);
        return form;
    }

    @Test
    void priceIsDaysTimesDailyPrice() {
        // Näide PDF-ist: 5 päeva x 35 € = 175 €
        BigDecimal price = bookingService.calculateTotalPrice(
                new BigDecimal("35.00"), LocalDate.of(2030, 1, 10), LocalDate.of(2030, 1, 15));
        assertEquals(0, new BigDecimal("175.00").compareTo(price));

        // Näide PDF-ist: 4 päeva x 40 € = 160 €
        price = bookingService.calculateTotalPrice(
                new BigDecimal("40.00"), LocalDate.of(2030, 1, 10), LocalDate.of(2030, 1, 14));
        assertEquals(0, new BigDecimal("160.00").compareTo(price));
    }

    @Test
    void createSavesBookingWithCalculatedPrice() {
        LocalDate start = LocalDate.now().plusDays(10);
        when(bookingRepository.existsOverlapping(anyLong(), eq(Booking.STATUS_ACTIVE), any(), any()))
                .thenReturn(false);

        Booking booking = bookingService.create(form(start, start.plusDays(5)), 1L);

        assertEquals(0, new BigDecimal("175.00").compareTo(booking.getTotalPrice()));
        assertEquals(Booking.STATUS_ACTIVE, booking.getStatus());
    }

    @Test
    void createRejectsOverlappingBooking() {
        LocalDate start = LocalDate.now().plusDays(10);
        when(bookingRepository.existsOverlapping(anyLong(), eq(Booking.STATUS_ACTIVE), any(), any()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.create(form(start, start.plusDays(3)), 1L));
    }

    @Test
    void createRejectsEndNotAfterStart() {
        LocalDate start = LocalDate.now().plusDays(10);
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.create(form(start, start), 1L));
    }

    @Test
    void createRejectsUnavailableCar() {
        car.setStatus(Car.STATUS_UNAVAILABLE);
        LocalDate start = LocalDate.now().plusDays(10);
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.create(form(start, start.plusDays(2)), 1L));
    }

    private Booking bookingOwnedBy(long userId) {
        AppUser owner = new AppUser();
        owner.setId(userId);
        Booking booking = new Booking();
        booking.setId(7L);
        booking.setUser(owner);
        booking.setStatus(Booking.STATUS_ACTIVE);
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));
        return booking;
    }

    @Test
    void userCanCancelOwnBooking() {
        Booking booking = bookingOwnedBy(1L);
        bookingService.cancel(7L, 1L);
        assertEquals(Booking.STATUS_CANCELLED, booking.getStatus());
    }

    @Test
    void userCannotCancelSomeoneElsesBooking() {
        Booking booking = bookingOwnedBy(2L);
        assertThrows(IllegalStateException.class, () -> bookingService.cancel(7L, 1L));
        assertEquals(Booking.STATUS_ACTIVE, booking.getStatus());
    }

    @Test
    void adminCanCancelAnyBooking() {
        Booking booking = bookingOwnedBy(2L);
        bookingService.cancel(7L, null);
        assertEquals(Booking.STATUS_CANCELLED, booking.getStatus());
    }
}
