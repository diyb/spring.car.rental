# Autode rendisüsteem (Car Rental System)

Java 21 • Spring Boot 4.1.2 • Spring Data JPA • MySQL • Thymeleaf • Gradle

## Käivitamine
1. Loo andmebaas ja näidisandmed: `mysql -u root -p < sql/car_rental_db.sql`
   (või ainult `CREATE DATABASE car_rental_db;` – tabelid loob Hibernate ise).
2. Sisesta MySQL-i parool failis `src/main/resources/application.properties`
   (või keskkonnamuutuja `DB_PASSWORD`).
3. Käivita: `./gradlew bootRun` (Windowsis `gradlew.bat bootRun`)
4. Ava brauseris http://localhost:8080 ja logi sisse

## Testid
`./gradlew test` – kasutab mälusisest H2 andmebaasi, MySQL-i pole vaja.

## Reeglid
- Hind = päevade arv × päevahind (päevade arv = lõppkuupäev − alguskuupäev).
- Broneering ei õnnestu, kui auto pole saadaval või periood kattub aktiivse broneeringuga.
- Broneeringut ei kustutata, vaid tühistatakse (staatus „Tühistatud“).
- Autot/klienti, kellel on broneeringuid, kustutada ei saa.

## Sisselogimine ja rollid
Tabelis `users` on veerud id, name, email, password, role (`USER` / `ADMIN`).

| E-post | Parool | Roll |
|---|---|---|
| admin@rental.ee | admin123 | ADMIN |
| martin@gmail.com | user123 | USER |
| liis@mail.ee | user123 | USER |

- **Admin** lisab, muudab ja kustutab autosid, näeb kliente ja kõiki broneeringuid ning võib neid tühistada.
- **USER** registreerib end lehel `/register`, teeb broneeringuid ning näeb ja tühistab ainult enda omi.
- Rakenduse kaudu registreeritud paroolid salvestatakse BCrypt-räsina (`{bcrypt}...`).
  SQL-is võib parooli kirjutada kujul `{noop}parool` (avatekst) – see on mugav õppeprojektis, päris kasutuses mitte.
- Kui ADMIN kasutajat pole, loob rakendus käivitamisel ühe (`app.admin.*` failis `application.properties`).
