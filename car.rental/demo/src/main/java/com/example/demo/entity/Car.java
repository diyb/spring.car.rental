package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "cars")
public class Car {

    public static final String STATUS_AVAILABLE = "Saadaval";
    public static final String STATUS_UNAVAILABLE = "Pole saadaval";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Mark on kohustuslik")
    @Size(max = 50, message = "Mark võib olla kuni 50 tähemärki")
    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @NotBlank(message = "Mudel on kohustuslik")
    @Size(max = 50, message = "Mudel võib olla kuni 50 tähemärki")
    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @NotNull(message = "Aasta on kohustuslik")
    @Min(value = 1980, message = "Aasta peab olema vähemalt 1980")
    @Max(value = 2100, message = "Aasta on liiga suur")
    @Column(name = "year", nullable = false)
    private Integer year;

    @NotNull(message = "Päevahind on kohustuslik")
    @DecimalMin(value = "0.01", message = "Päevahind peab olema suurem kui 0")
    @Column(name = "daily_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyPrice;

    @NotBlank(message = "Staatus on kohustuslik")
    @Column(name = "status", nullable = false, length = 20)
    private String status = STATUS_AVAILABLE;

    public Car() {
    }

    /** Nimi kuvamiseks rippmenüüdes, nt "Toyota Corolla (2022)". */
    @Transient
    public String getDisplayName() {
        return brand + " " + model + " (" + year + ")";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public BigDecimal getDailyPrice() { return dailyPrice; }
    public void setDailyPrice(BigDecimal dailyPrice) { this.dailyPrice = dailyPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
