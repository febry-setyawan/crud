package com.example.crud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CrudApplicationTest {

    @Test
    void contextLoads() {
        // Add a trivial assertion to ensure the context loads
        assert true;
    }

    @Test
    void main() {
    CrudApplication.main(new String[]{});
    // Add a trivial assertion to ensure main runs
    assert true;
    }
}