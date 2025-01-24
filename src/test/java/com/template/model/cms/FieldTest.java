package com.template.model.cms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FieldTest {
    private Field field;

    @BeforeEach
    void setUp() {
        field = new Field();
    }

    @Test
    void testFieldProperties() {
        field.setType("String");
        field.setLabel("Email Subject");
        field.setRequired(true);
        field.setDefaultValue("Welcome to our service!");

        assertEquals("String", field.getType());
        assertEquals("Email Subject", field.getLabel());
        assertTrue(field.isRequired());
        assertEquals("Welcome to our service!", field.getDefaultValue());
    }

    @Test
    void testDefaultValueTypes() {
        // Test string default value
        field.setDefaultValue("test");
        assertTrue(field.getDefaultValue() instanceof String);

        // Test numeric default value
        field.setDefaultValue(42);
        assertTrue(field.getDefaultValue() instanceof Integer);

        // Test boolean default value
        field.setDefaultValue(true);
        assertTrue(field.getDefaultValue() instanceof Boolean);
    }

    @Test
    void testEqualsAndHashCode() {
        Field field1 = new Field();
        field1.setType("String");
        field1.setLabel("Test");
        field1.setRequired(true);

        Field field2 = new Field();
        field2.setType("String");
        field2.setLabel("Test");
        field2.setRequired(true);

        assertEquals(field1, field2);
        assertEquals(field1.hashCode(), field2.hashCode());
    }
} 