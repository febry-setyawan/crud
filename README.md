# Generic CRUD Framework dengan Spring Boot

Framework ini adalah sebuah proyek demonstrasi untuk membangun sistem CRUD (Create, Read, Update, Delete) yang generik dan dapat digunakan kembali (*reusable*) menggunakan Spring Boot. Proyek ini dibangun tanpa JPA/Hibernate, dan sebagai gantinya menggunakan `JdbcClient` untuk interaksi dengan database.

## Fitur Utama ✨

- **Repository Generik**: Logika CRUD terpusat di `AbstractJdbcRepository`, mengurangi duplikasi kode.
- **Clean Architecture**: Pemisahan yang jelas antara Controller, Service, Repository, dan Model.
- **Pencarian & Pengurutan Dinamis**: Endpoint list mendukung filter dan sort dinamis.
- **Pagination**: Dukungan penuh untuk pagination menggunakan `Pageable` dari Spring Data.
- **Audit Trail Otomatis**: Field `createdAt`, `createdBy`, `updatedAt`, dan `updatedBy` diisi secara otomatis menggunakan AOP.
- **DTO Pattern**: Memisahkan model internal dari request/response API untuk keamanan dan fleksibilitas.
- **Penanganan Error Terpusat**: Menggunakan `@ControllerAdvice` untuk respons error JSON yang konsisten.
- **Performance Logging**: Mengukur waktu eksekusi setiap operasi database menggunakan *utility class* fungsional.
- **Dokumentasi API**: Dokumentasi API interaktif dibuat secara otomatis menggunakan SpringDoc (Swagger UI).
- **Analisis Kualitas Kode**: Terintegrasi dengan SonarCloud untuk analisis statis dan laporan *test coverage*.
- **Unit Testing**: Cakupan testing yang tinggi untuk memastikan keandalan kode.
- **Monitoring**: Dilengkapi dengan endpoint monitoring dasar dari Spring Boot Actuator.

---
## Teknologi yang Digunakan 🛠️

- **Java 21**
- **Spring Boot 3.5.x**
- **Undertow** (sebagai embedded server)
- **Spring Data JDBC** (`JdbcClient`)
- **Spring AOP** untuk audit trail
- **Spring Boot Actuator** untuk monitoring
- **SLF4J & Logback** untuk logging
- **Maven** sebagai build tool
- **H2 Database** (In-memory)
- **MapStruct** untuk mapping DTO
- **SpringDoc OpenAPI** untuk Swagger UI
- **SonarCloud** & **JaCoCo** untuk analisis kualitas kode
- **JUnit 5 & Mockito** untuk testing

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
├── feature              # Direktori untuk setiap fitur bisnis
│   └── user             # Contoh fitur: User Management
│       ├── controller   # UserController (REST API Layer)
│       ├── dto          # DTO dan Mapper
│       ├── model        # Entitas spesifik (User)
│       ├── repository   # Repository spesifik
│       └── service      # Business Logic Layer (UserService)
├── util                 # Kelas utilitas (TimerUtil)
└── CrudApplication.java # Kelas utama aplikasi
```

---
## Logging

Konfigurasi logging diatur dalam file `src/main/resources/logback-spring.xml` untuk memberikan kontrol penuh atas output log. Secara default, semua paket aplikasi (`com.example.crud`) diatur ke level `DEBUG` untuk menampilkan log yang detail selama pengembangan.

---
## Cara Menjalankan Proyek

### Prasyarat

- JDK 21 atau lebih baru
- Apache Maven

### Langkah-langkah

1.  **Clone repository ini:**
    ```bash
    git clone [URL_GITHUB_ANDA]
    cd crud
    ```

2.  **Compile dan jalankan proyek menggunakan Maven:**
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

- **Swagger UI (Dokumentasi API Interaktif):**
  [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

- **Actuator Health Endpoint:**
  [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

- **H2 Database Console (untuk melihat data):**
  [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
    - **JDBC URL**: `jdbc:h2:mem:testdb`
    - **User Name**: `sa`
    - **Password**: (kosongkan)