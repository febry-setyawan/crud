
package com.example.crud.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptGenerateHash {
    private static final Logger logger = LoggerFactory.getLogger(BCryptGenerateHash.class);
    @Test
    void printBcryptHash() {
    String rawPassword = "s3cr3t";
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    String hash = encoder.encode(rawPassword);
    logger.debug("BCrypt hash for 's3cr3t': {}", hash);
    // Add a simple assertion to check the hash is not null or empty
    org.assertj.core.api.Assertions.assertThat(hash).isNotBlank();
    }
}
