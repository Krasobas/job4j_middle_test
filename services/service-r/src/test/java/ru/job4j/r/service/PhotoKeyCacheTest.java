package ru.job4j.r.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhotoKeyCacheTest {

    private final PhotoKeyCache cache = new PhotoKeyCache();

    @Test
    void putAndGetReturnsStoredValue() {
        cache.put("RB-001", "photos/student1.jpg");

        assertThat(cache.get("RB-001")).isEqualTo("photos/student1.jpg");
    }

    @Test
    void getMissingKeyReturnsNull() {
        assertThat(cache.get("UNKNOWN")).isNull();
    }
}