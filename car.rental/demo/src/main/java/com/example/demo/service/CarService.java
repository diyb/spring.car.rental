package com.example.demo.service;

import com.example.demo.entity.Car;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;

    public CarService(CarRepository carRepository, BookingRepository bookingRepository) {
        this.carRepository = carRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Car> findAll() {
        return carRepository.findAll();
    }

    public List<Car> findAvailable() {
        return carRepository.findByStatus(Car.STATUS_AVAILABLE);
    }

    public Car findById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Autot ID-ga " + id + " ei leitud"));
    }

    public Car save(Car car) {
        return carRepository.save(car);
    }

    public void delete(Long id) {
        if (bookingRepository.existsByCarId(id)) {
            throw new IllegalStateException(
                    "Autot ei saa kustutada, sest sellel on broneeringuid. Märgi auto hoopis staatusega \""
                            + Car.STATUS_UNAVAILABLE + "\".");
        }
        carRepository.deleteById(id);
    }
}
