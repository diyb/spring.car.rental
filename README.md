# Autode rendisüsteem

Veebirakendus autode ja rendibroneeringute haldamiseks. Kasutajad saavad
registreeruda ja teha broneeringuid; administraator haldab autosid ning näeb
kliente ja kõiki broneeringuid.

## Tehnoloogiad

- Java 21
- Spring Boot 4.1.2, Spring MVC, Spring Data JPA ja Spring Security
- Thymeleaf
- MySQL
- Gradle Wrapper

## Eeldused

- Java 21
- MySQL-server

## Seadistamine ja käivitamine (Windows)

1. Loo andmebaas ning soovi korral laadi näidisandmed:
   - Ava fail `car.rental\demo\sql\car_rental_db.sql` MySQL Workbenchis või
     phpMyAdminis ja käivita skript.
   - Skript loob `car_rental_db` andmebaasi, tabelid ja näidisandmed. **See
     kustutab sama nimega olemasolevad `bookings`, `users` ja `cars` tabelid.**
   - Sama skript on koopia ka failis `car.rental.db.sql`.
2. Seadista ühendus failis
   `car.rental\demo\src\main\resources\application.properties` või määra
   Spring Booti keskkonnamuutujad `SPRING_DATASOURCE_URL`,
   `SPRING_DATASOURCE_USERNAME` ja `SPRING_DATASOURCE_PASSWORD`.
   Vaikimisi ühendus kasutab `localhost:3306`, andmebaasi `car_rental_db` ja
   kasutajat `root`.
3. Ava PowerShell projektikaustas ja käivita:



4. Ava brauseris [http://localhost:8080](http://localhost:8080).

Kui soovid alustada tühja andmebaasiga, loo ainult `car_rental_db` andmebaas.
Hibernate loob ja uuendab tabelid rakenduse käivitamisel.

## Funktsioonid ja rollid

- **Kasutaja:** saab registreeruda, vaadata enda broneeringuid, broneerida
  saadaolevaid autosid ja tühistada enda broneeringuid.
- **Administraator:** saab autosid lisada, muuta ja kustutada, vaadata kliente
  ning kõiki broneeringuid ja neid tühistada.
- Broneeringu hind arvutatakse valemiga päevade arv × auto päevahind.
  Broneeringu lõppkuupäev peab olema alguskuupäevast hilisem.
- Aktiivse broneeringuga kattuvaks perioodiks sama autot broneerida ei saa.
- Broneeringuid tühistatakse, mitte ei kustutata; broneeringutega seotud autot
  ei saa kustutada.
- Kasutaja registreerimisel salvestatakse parool BCrypt-räsina.

