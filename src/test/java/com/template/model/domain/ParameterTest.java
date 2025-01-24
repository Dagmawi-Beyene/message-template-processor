package com.template.model.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ParameterTest {

    @Test
    void shouldCreateParameterWithAllFields() {
        // given
        String id = "param-123";
        String key = "userName";
        String name = "User Name";

        // when
        Parameter parameter = Parameter.builder()
                .id(id)
                .key(key)
                .name(name)
                .build();

        // then
        assertThat(parameter.getId()).isEqualTo(id);
        assertThat(parameter.getKey()).isEqualTo(key);
        assertThat(parameter.getName()).isEqualTo(name);
    }
} 