package com.template.model.cms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CmsResponseTest {
    private ObjectMapper objectMapper;
    private CmsResponse response;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        response = new CmsResponse();
    }

    @Test
    void shouldDeserializeTemplateJson() throws IOException {
        // Given
        var jsonResource = new ClassPathResource("samples/template.json");
        assertTrue(jsonResource.exists(), "template.json should exist");

        // Debug: Print JSON content
        String jsonContent = StreamUtils.copyToString(jsonResource.getInputStream(), StandardCharsets.UTF_8);
        System.out.println("Reading JSON: " + jsonContent);

        // When
        CmsResponse response = objectMapper.readValue(jsonResource.getInputStream(), CmsResponse.class);
        System.out.println("Parsed response: " + response);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotal());
        assertEquals(0, response.getSkip());
        assertEquals(1000, response.getLimit());
        assertEquals(1, response.getItems().size());

        // Verify first item
        CmsEntry item = response.getItems().get(0);
        assertEquals("111-111-111", item.getSys().getId());
        assertEquals("TRAIN_DELAYED", item.getFields().getKey());
        assertEquals("Juna myöhässä", item.getFields().getName());

        // Verify includes
        assertNotNull(response.getIncludes(), "Includes should not be null");
        assertNotNull(response.getIncludes().getEntry(), "Entry list should not be null");
        assertFalse(response.getIncludes().getEntry().isEmpty(), "Entry list should not be empty");
        assertEquals(4, response.getIncludes().getEntry().size(), "Should have 4 entries in includes");

        // Test specific entries
        CmsEntry firstEntry = response.getIncludes().getEntry().get(0);
        assertEquals("444-444-444", firstEntry.getSys().getId());
        assertEquals("TRAIN_NUMBER", firstEntry.getFields().getKey());
    }

    @Test
    void builder_Success() {
        // Given
        CmsEntry entry = createTestEntry();
        List<CmsEntry> includes = List.of(createTestInclude());

        // When
        CmsResponse response = new CmsResponse();
        response.setItems(List.of(entry));
        Includes includesObj = new Includes();
        includesObj.setEntry(includes);
        response.setIncludes(includesObj);
        response.setTotal(1);
        response.setSkip(0);
        response.setLimit(100);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotal());
        assertEquals(0, response.getSkip());
        assertEquals(100, response.getLimit());
        assertFalse(response.getItems().isEmpty());
        assertFalse(response.getIncludes().getEntry().isEmpty());
    }

    @Test
    void testBasicProperties() {
        response.setTotal(10);
        response.setSkip(0);
        response.setLimit(5);

        assertEquals(10, response.getTotal());
        assertEquals(0, response.getSkip());
        assertEquals(5, response.getLimit());
    }

    @Test
    void testItems() {
        CmsEntry entry1 = new CmsEntry();
        CmsEntry entry2 = new CmsEntry();
        List<CmsEntry> items = Arrays.asList(entry1, entry2);

        response.setItems(items);

        assertEquals(2, response.getItems().size());
    }

    @Test
    void testErrors() {
        List<String> errors = Arrays.asList("Error 1", "Error 2");
        response.setErrors(errors);

        assertEquals(2, response.getErrors().size());
        assertTrue(response.getErrors().contains("Error 1"));
        assertTrue(response.getErrors().contains("Error 2"));
    }

    @Test
    void testSys() {
        Sys sys = new Sys();
        sys.setType("Array");

        response.setSys(sys);

        assertNotNull(response.getSys());
        assertEquals("Array", response.getSys().getType());
    }

    @Test
    void testIncludes() {
        Includes includes = new Includes();
        List<CmsEntry> entries = Arrays.asList(new CmsEntry());
        includes.setEntry(entries);

        response.setIncludes(includes);

        assertNotNull(response.getIncludes());
        assertEquals(1, response.getIncludes().getEntry().size());
    }

    @Test
    void testEqualsAndHashCode() {
        CmsResponse response1 = new CmsResponse();
        response1.setTotal(10);
        response1.setLimit(5);

        CmsResponse response2 = new CmsResponse();
        response2.setTotal(10);
        response2.setLimit(5);

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    private CmsEntry createTestEntry() {
        CmsEntry entry = new CmsEntry();
        Sys sys = new Sys();
        sys.setId("test-id");
        entry.setSys(sys);

        Fields fields = new Fields();
        fields.setKey("TEST_KEY");
        fields.setName("Test Template");
        entry.setFields(fields);

        return entry;
    }

    private CmsEntry createTestInclude() {
        CmsEntry include = new CmsEntry();
        Sys sys = new Sys();
        sys.setId("include-id");
        include.setSys(sys);

        Fields fields = new Fields();
        fields.setKey("INCLUDE_KEY");
        fields.setName("Include Name");
        include.setFields(fields);

        return include;
    }
}