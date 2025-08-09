package com.example.crud.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class TimerUtilTest {

    @Test
    void timeSupplier() {
        // When
        String result = TimerUtil.time("test supplier", () -> "result");

        // Then
        assertThat(result).isEqualTo("result");
    }

    @Test
    void timeRunnable() {
        // Given
        AtomicBoolean executed = new AtomicBoolean(false);

        // When
        TimerUtil.time("test runnable", () -> executed.set(true));

        // Then
        assertThat(executed.get()).isTrue();
    }
}
