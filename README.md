# Generic CRUD Framework dengan Spring Boot

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
-   **Dokumentasi API**: Dokumentasi API interaktif dibuat secara otomatis menggunakan SpringDoc (Swagger UI).
-   **Analisis Kualitas Kode**: Terintegrasi dengan SonarCloud untuk analisis statis dan laporan *test coverage*.
-   **Unit & Integration Testing**: Cakupan testing yang tinggi untuk memastikan keandalan kode.
-   **Monitoring**: Dilengkapi dengan endpoint monitoring dasar dari Spring Boot Actuator.
-   **Caching**: Implementasi cache *in-memory* dengan TTL menggunakan Caffeine.

---

## Teknologi yang Digunakan 🛠️

-   **Java 21**
-   **Spring Boot 3.5.x**
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
│       ├── repository   # Repository spesifik (kelas implementasi)
│       └── service      # Service Layer (Interface, Default & Resilient Impl)
├── util                 # Kelas utilitas (TimerUtil)
└── CrudApplication.java # Kelas utama aplikasi
```

---

## Cara Menjalankan Proyek

### Prasyarat

-   JDK 21 atau lebih baru
-   Apache Maven

### Langkah-langkah

1.  **Clone repository ini:**
    ```bash
    git clone [URL_GITHUB_ANDA]
    cd crud
    ```

2.  **Compile dan jalankan proyek menggunakan Maven:**
    Aplikasi akan berjalan dengan profil `dev` secara default.
    ```bash
    mvn spring-boot:run
    ```

3.  Aplikasi akan berjalan di port `8080`.

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

## Akses API dan Dokumentasi

Setelah aplikasi berjalan, Anda dapat mengakses beberapa endpoint berikut:

-   **Swagger UI (Dokumentasi API Interaktif):**
    [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
    (Login dengan `user` / `password` yang di-generate di konsol)

-   **Actuator Health Endpoint:**
    [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

-   **H2 Database Console (untuk melihat data):**
    [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
    -   **JDBC URL**: `jdbc:h2:mem:testdb`
    -   **User Name**: `sa`
    -   **Password**: (kosongkan)