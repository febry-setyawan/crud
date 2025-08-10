# --- STAGE 1: Build ---
# Gunakan image Maven resmi untuk melakukan build aplikasi
FROM maven:3.9-eclipse-temurin-21 AS build

# Tentukan direktori kerja di dalam container
WORKDIR /app

# Salin file pom.xml terlebih dahulu untuk memanfaatkan cache layer Docker
COPY pom.xml .

# Unduh semua dependensi
RUN mvn dependency:go-offline

# Salin sisa source code
COPY src ./src

# Build aplikasi dan buat file JAR
RUN mvn clean package -DskipTests


# --- STAGE 2: Final Image ---
# Gunakan base image ubi-minimal yang sangat ringan
FROM registry.redhat.io/ubi9-minimal:9.6-1754000177

# Tentukan direktori kerja
WORKDIR /app

# Gabungkan instalasi JRE, pembuatan user, dan pembersihan dalam satu layer
RUN microdnf install -y java-21-openjdk-headless && \
    microdnf clean all && \
    adduser --uid 1001 --gid 0 appuser

# Salin file JAR yang sudah di-build dari stage sebelumnya
COPY --from=build /app/target/crud-0.0.1-SNAPSHOT.jar app.jar

# Ubah kepemilikan file ke user baru
RUN chown appuser:root app.jar

# Ganti ke user non-root
USER appuser

# Tentukan port yang akan diekspos
EXPOSE 8080

# Perintah untuk menjalankan aplikasi
ENTRYPOINT ["java", "-jar", "app.jar"]