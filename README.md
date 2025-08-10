# Generic CRUD Framework dengan Spring Boot

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=febry-setyawan_crud&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=febry-setyawan_crud) [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=febry-setyawan_crud&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=febry-setyawan_crud) [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=febry-setyawan_crud&metric=bugs)](https://sonarcloud.io/summary/new_code?id=febry-setyawan_crud) [![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=febry-setyawan_crud&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=febry-setyawan_crud) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=febry-setyawan_crud&metric=coverage)](https://sonarcloud.io/summary/new_code?id=febry-setyawan_crud) 
[![Build Status](https://github.com/febry-setyawan/crud/actions/workflows/build-and-analyze.yml/badge.svg)](https://github.com/febry-setyawan/crud/actions)

Framework ini adalah sebuah proyek demonstrasi untuk membangun sistem CRUD (Create, Read, Update, Delete) yang generik dan dapat digunakan kembali (*reusable*) menggunakan Spring Boot. Proyek ini dibangun tanpa JPA/Hibernate, dan sebagai gantinya menggunakan `JdbcClient` untuk interaksi dengan database.

## Fitur Utama ✨

-   **Repository Generik**: Logika CRUD terpusat di `AbstractJdbcRepository`, mengurangi duplikasi kode.
-   **Clean Architecture**: Pemisahan yang jelas antara Controller, Service, Repository, dan Model.
-   **Service Layer Decorator**: Menggunakan *Decorator Pattern* untuk menambahkan fungsionalitas secara transparan, seperti *Circuit Breaker*.
-   **Resilience**: Terintegrasi dengan **Resilience4j** (*Circuit Breaker*) untuk meningkatkan ketahanan aplikasi terhadap kegagalan layanan.
-   **Pencarian & Pengurutan Dinamis**: Endpoint list mendukung filter dinamis menggunakan DTO Filter.
-   **Pagination**: Dukungan penuh untuk pagination menggunakan `Pageable` dari Spring Data.
-   **Audit Trail Otomatis**: Field `createdAt`, `createdBy`, `updatedAt`, dan `updatedBy` diisi secara otomatis menggunakan AOP.
-   **DTO Pattern**: Memisahkan model internal dari request/response API untuk keamanan dan fleksibilitas.
-   **Penanganan Error Terpusat**: Menggunakan `@ControllerAdvice` untuk respons error JSON yang konsisten.
-   **Containerization**: Dilengkapi dengan **Dockerfile** dan **docker-compose.yml** untuk kemudahan setup dan deployment.
-   **Performance Testing**: Menyertakan skrip dasar untuk *load testing* menggunakan **k6**.
-   **Dokumentasi API**: Dokumentasi API interaktif dibuat secara otomatis menggunakan SpringDoc (Swagger UI).
-   **Analisis Kualitas Kode**: Terintegrasi dengan SonarCloud untuk analisis statis dan laporan *test coverage*.
-   **Unit & Integration Testing**: Cakupan testing yang tinggi untuk memastikan keandalan kode.
-   **Monitoring**: Dilengkapi dengan endpoint monitoring dasar dari Spring Boot Actuator.
-   **Caching**: Implementasi cache *in-memory* dengan TTL menggunakan Caffeine.

---

## Teknologi yang Digunakan 🛠️

-   **Java 21**
-   **Spring Boot 3.5.x**
-   **Docker & Docker Compose**
-   **Undertow** (sebagai embedded server)
-   **Spring Data JDBC** (`JdbcClient`)
-   **Spring AOP** untuk audit trail
-   **Spring Boot Actuator** untuk monitoring
-   **Spring Security** untuk otentikasi dasar
-   **Spring Cache** & **Caffeine** untuk *caching*
-   **Resilience4j** untuk *Circuit Breaker*
-   **Flyway** untuk migrasi database
-   **SLF4J & Logback** untuk logging
-   **Maven** sebagai build tool
-   **H2 Database** (In-memory) & **PostgreSQL**
-   **MapStruct** untuk mapping DTO
-   **SpringDoc OpenAPI** untuk Swagger UI
-   **SonarCloud** & **JaCoCo** untuk analisis kualitas kode
-   **JUnit 5 & Mockito** untuk testing
-   **k6** untuk *performance testing*

---

## Struktur Folder 📁

Struktur proyek ini dirancang untuk memisahkan setiap kepentingan (*separation of concerns*) agar mudah dikelola dan dikembangkan.

```
com.example.crud
├── aop                  # Logic cross-cutting (AuditTrailAspect)
├── common               # Komponen generik/reusable
│   ├── exception        # GlobalExceptionHandler, Custom Exceptions
│   ├── model            # Entitas dasar dan interface
│   └── repository       # Repository generik
├── config               # Konfigurasi Spring (SecurityConfig)
├── feature              # Direktori untuk setiap fitur bisnis
│   └── user             # Contoh fitur: User Management
│       ├── controller   # UserController (REST API Layer)
│       ├── dto          # DTO dan Mapper
│       ├── model        # Entitas spesifik (User)
│       ├── repository   # Repository spesifik
│       └── service      # Service Layer (Interface, Default & Resilient Impl)
├── util                 # Kelas utilitas (TimerUtil)
└── CrudApplication.java # Kelas utama aplikasi
```

---

## Cara Menjalankan Proyek

### Prasyarat

-   JDK 21 atau lebih baru
-   Apache Maven
-   Docker & Docker Compose

### Menjalankan dengan Docker Compose (Direkomendasikan)
Cara ini akan menjalankan aplikasi beserta database PostgreSQL dalam kontainer.

1.  **Build & Jalankan Kontainer**:
    Perintah ini akan membangun *image* aplikasi dan menjalankan semua layanan di latar belakang (`-d`).
    ```bash
    docker-compose up -d --build
    ```

2.  **Melihat Log Aplikasi**:
    Untuk melihat log dari aplikasi Spring Boot Anda:
    ```bash
    docker-compose logs -f app
    ```

3.  **Menghentikan Kontainer**:
    Untuk menghentikan dan menghapus kontainer yang berjalan:
    ```bash
    docker-compose down
    ```

### Menjalankan Secara Lokal dengan Maven (Profil Dev)
Cara ini akan menjalankan aplikasi dengan database H2 in-memory.
```bash
mvn spring-boot:run
```
---

## Analisis Kualitas Kode (SonarCloud)

Proyek ini dikonfigurasi untuk mengirim laporan analisis ke SonarCloud.

1.  Pastikan Anda sudah memiliki token dari SonarCloud.
2.  Jalankan perintah berikut dari root proyek:
    ```bash
    mvn clean verify sonar:sonar -Dsonar.token=TOKEN_SONARCLOUD_ANDA
    ```
3.  Lihat hasilnya di dashboard SonarCloud Anda.

---

## Cara Menjalankan Performance Test (k6)

Proyek ini sudah menyertakan script k6 (`script.js`) untuk performance/load testing endpoint API.

### Menjalankan k6 secara lokal:
1. Install k6: https://k6.io/docs/getting-started/installation/
2. Jalankan aplikasi (pastikan endpoint API bisa diakses)
3. Jalankan perintah berikut:
    ```bash
    k6 run script.js
    ```

### Menjalankan k6 ke k6 Cloud:
    
    k6 run script.js
    
Hasil test akan muncul di terminal atau dashboard k6 cloud.

---

## Workflow Pengembangan (Branching & CI/CD)

- Branch utama: `main` (selalu stabil, siap rilis)
- Branch pengembangan: `development` (integrasi fitur sebelum merge ke main)
- Setiap fitur/bugfix dibuat di branch `feature/xxx` atau `bugfix/xxx` lalu merge ke `development`
- CI/CD otomatis build, test, dan analisis kualitas kode setiap push/PR
- Rilis ke production dilakukan dari branch `main`

---

## Akses API dan Dokumentasi

Setelah aplikasi berjalan, Anda dapat mengakses beberapa endpoint berikut:

-   **Swagger UI (Dokumentasi API Interaktif):**
    [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
    (Login dengan `user` / `password` yang di-generate di konsol)

-   **Actuator Health Endpoint:**
    [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

-   **H2 Database Console (jika menggunakan profil dev):**
    [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
    -   **JDBC URL**: `jdbc:h2:mem:testdb`
    -   **User Name**: `sa`
    -   **Password**: (kosongkan)