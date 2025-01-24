package com.template.model.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TrafficTypeTest {

    @Test
    void builder_Success() {
        // Given
        String key = "LONG_DISTANCE";
        String name = "Kaukoliikenne";

        // When
        TrafficType trafficType = TrafficType.builder()
            .key(key)
            .name(name)
            .build();

        // Then
        assertEquals(key, trafficType.getKey());
        assertEquals(name, trafficType.getName());
    }
} 