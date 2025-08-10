package com.example.crud.util;

import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

public class TimerUtil {

    private static final Logger log = LoggerFactory.getLogger(TimerUtil.class);

    private TimerUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Mengukur waktu eksekusi sebuah Supplier (aksi yang mengembalikan nilai).
     * 
     * @param actionName Nama aksi untuk logging.
     * @param supplier   Fungsi yang akan dieksekusi.
     * @return Hasil dari eksekusi supplier.
     */
    public static <T> T time(String actionName, Supplier<T> supplier) {
        StopWatch stopWatch = new StopWatch(actionName);
        stopWatch.start();
        T result = supplier.get();
        stopWatch.stop();
        log.debug("Execution time for '{}': {} ms", stopWatch.getId(), stopWatch.getTotalTimeMillis());
        return result;
    }

    // Versi untuk Runnable (jika diperlukan)
    public static void time(String actionName, Runnable action) {
        StopWatch stopWatch = new StopWatch(actionName);
        stopWatch.start();
        action.run();
        stopWatch.stop();
        log.debug("Execution time for '{}': {} ms", stopWatch.getId(), stopWatch.getTotalTimeMillis());
    }
}