-- =====================================================================
--  Autode rendisüsteem (Car Rental System) – MySQL / MariaDB andmebaas
--  Käivitamine:  phpMyAdmin -> Import  (või: mysql -u root -p < sql/car_rental_db.sql)
--
--  Tabelid:  users (id, name, email, password, role)  |  cars  |  bookings
--  Rollid:   ADMIN – haldab autosid, näeb kliente ja kõiki broneeringuid
--            USER  – teeb broneeringuid, näeb/tühistab ainult enda omi
--
--  NB! Skript kustutab olemasolevad tabelid ja loob need uuesti.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS car_rental_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE car_rental_db;

DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS cars;
DROP TABLE IF EXISTS customers;   -- vana tabel varasemast versioonist

-- ---------------------------------------------------------------------
-- users – kasutajad (kasutajanimi sisselogimisel = email)
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id        BIGINT        NOT NULL AUTO_INCREMENT,
    name      VARCHAR(100)  NOT NULL,
    email     VARCHAR(100)  NOT NULL,
    password  VARCHAR(100)  NOT NULL,
    role      VARCHAR(20)   NOT NULL DEFAULT 'USER',
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'))
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- cars – autod
-- ---------------------------------------------------------------------
CREATE TABLE cars (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    brand       VARCHAR(50)    NOT NULL,
    model       VARCHAR(50)    NOT NULL,
    `year`      INT            NOT NULL,
    daily_price DECIMAL(10,2)  NOT NULL,
    status      VARCHAR(20)    NOT NULL DEFAULT 'Saadaval',
    PRIMARY KEY (id),
    CONSTRAINT chk_cars_year   CHECK (`year` BETWEEN 1980 AND 2100),
    CONSTRAINT chk_cars_price  CHECK (daily_price > 0),
    CONSTRAINT chk_cars_status CHECK (status IN ('Saadaval', 'Pole saadaval'))
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- bookings – broneeringud
-- ---------------------------------------------------------------------
CREATE TABLE bookings (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    car_id       BIGINT         NOT NULL,
    user_id      BIGINT         NOT NULL,
    start_date   DATE           NOT NULL,
    end_date     DATE           NOT NULL,
    total_price  DECIMAL(10,2)  NOT NULL,
    status       VARCHAR(20)    NOT NULL DEFAULT 'Aktiivne',
    PRIMARY KEY (id),
    CONSTRAINT fk_bookings_car  FOREIGN KEY (car_id)  REFERENCES cars (id)  ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_bookings_dates  CHECK (end_date > start_date),
    CONSTRAINT chk_bookings_price  CHECK (total_price >= 0),
    CONSTRAINT chk_bookings_status CHECK (status IN ('Aktiivne', 'Tühistatud')),
    INDEX idx_bookings_car_dates (car_id, start_date, end_date),
    INDEX idx_bookings_user (user_id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Näidisandmed
--
-- Paroolid on siin kujul '{noop}parool' (= avatekst), mida rakendus tunneb ära.
-- Rakenduse kaudu registreeritud kasutajate paroolid salvestatakse automaatselt
-- räsituna ('{bcrypt}...'). Päris kasutuses vaheta need paroolid välja.
--
--   admin@rental.ee   /  admin123   (ADMIN)
--   martin@gmail.com  /  user123    (USER)
--   liis@mail.ee      /  user123    (USER)
-- ---------------------------------------------------------------------
INSERT INTO users (name, email, password, role) VALUES
    ('Administraator', 'admin@rental.ee',  '{noop}admin123', 'ADMIN'),
    ('Martin Tamm',    'martin@gmail.com', '{noop}user123',  'USER'),
    ('Liis Kask',      'liis@mail.ee',     '{noop}user123',  'USER');

INSERT INTO cars (brand, model, `year`, daily_price, status) VALUES
    ('Toyota',     'Corolla', 2022, 35.00, 'Saadaval'),
    ('Volkswagen', 'Golf',    2021, 40.00, 'Saadaval'),
    ('Skoda',      'Octavia', 2023, 45.00, 'Saadaval'),
    ('BMW',        '320d',    2020, 70.00, 'Saadaval'),
    ('Ford',       'Focus',   2019, 30.00, 'Pole saadaval');

-- total_price = päevade arv × päevahind (päevade arv = end_date - start_date)
INSERT INTO bookings (car_id, user_id, start_date, end_date, total_price, status) VALUES
    (1, 2, '2030-01-10', '2030-01-15', 175.00, 'Aktiivne'),   -- 5 × 35 €
    (2, 3, '2030-02-01', '2030-02-05', 160.00, 'Aktiivne'),   -- 4 × 40 €
    (3, 2, '2030-03-10', '2030-03-12',  90.00, 'Tühistatud'); -- 2 × 45 €

-- ---------------------------------------------------------------------
-- Kasulikud päringud
-- ---------------------------------------------------------------------

-- Kõik broneeringud koos kasutaja ja auto andmetega
-- SELECT b.id, u.name AS klient, CONCAT(a.brand, ' ', a.model) AS auto,
--        b.start_date, b.end_date,
--        DATEDIFF(b.end_date, b.start_date) AS paevi,
--        b.total_price, b.status
-- FROM bookings b
-- JOIN users u ON u.id = b.user_id
-- JOIN cars a  ON a.id = b.car_id
-- ORDER BY b.start_date DESC;

-- Kliendid (administraatori leht "Kliendid")
-- SELECT id, name, email FROM users WHERE role = 'USER' ORDER BY name;

-- Uue administraatori lisamine
-- INSERT INTO users (name, email, password, role)
-- VALUES ('Uus Admin', 'uus@rental.ee', '{noop}parool123', 'ADMIN');

-- Kas auto 1 on perioodil 2030-01-12 .. 2030-01-14 vaba? (0 = vaba, >0 = kattub)
-- SELECT COUNT(*) FROM bookings
-- WHERE car_id = 1 AND status = 'Aktiivne'
--   AND start_date < '2030-01-14' AND end_date > '2030-01-12';
