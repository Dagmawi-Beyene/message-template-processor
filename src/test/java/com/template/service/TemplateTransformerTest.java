package com.template.service;

import com.template.model.cms.*;
import com.template.model.domain.MessageTemplate;
import com.template.model.domain.TrafficType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class TemplateTransformerTest {

    private TemplateTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new TemplateTransformer();
    }

    @Test
    void transform_FullTemplate_Success() {
        // Given
        CmsResponse response = createFullResponse();

        // When
        MessageTemplate result = transformer.transform(response);

        // Then
        assertNotNull(result);
        assertEquals("111-111-111", result.getId());
        assertEquals("TRAIN_DELAYED", result.getKey());
        assertEquals("Juna myöhässä", result.getName());
        
        // Check traffic type
        assertNotNull(result.getTrafficType());
        assertEquals("LONG_DISTANCE", result.getTrafficType().getKey());
        assertEquals("Kaukoliikenne", result.getTrafficType().getName());
        
        // Check subject
        String expectedSubject = "{TRAIN_TYPE} {TRAIN_NUMBER} - delayed";
        assertEquals(expectedSubject, result.getSubject());
        
        // Check body
        String expectedBody = "Dear passengers, train {TRAIN_TYPE} {TRAIN_NUMBER} is delayed due to {DELAY_REASON}.\n" +
                            "We apologise for the inconvenience. We will update you on the situation as soon as possible.";
        assertEquals(expectedBody, result.getBody());
        
        // Check parameters
        List<String> expectedParams = List.of("TRAIN_TYPE", "TRAIN_NUMBER", "DELAY_REASON");
        assertTrue(result.getParameters().containsAll(expectedParams));
        assertEquals(expectedParams.size(), result.getParameters().size());
    }

    @Test
    void transform_EmptyResponse_ReturnsNull() {
        // Given
        CmsResponse response = new CmsResponse();

        // When
        MessageTemplate result = transformer.transform(response);

        // Then
        assertNull(result);
    }

    @Test
    void transform_MissingIncludes_HandlesGracefully() {
        // Given
        CmsResponse response = createResponseWithoutIncludes();

        // When
        MessageTemplate result = transformer.transform(response);

        // Then
        assertNotNull(result);
        assertNull(result.getTrafficType());
        assertTrue(result.getParameters().isEmpty());
    }

    @Test
    void transform_whenTrafficTypeNotFound_shouldHandleGracefully() {
        // Given
        CmsResponse response = new CmsResponse();
        CmsEntry templateEntry = new CmsEntry();
        Sys sys = new Sys();
        sys.setId("test-id");
        templateEntry.setSys(sys);
        
        Fields fields = new Fields();
        fields.setKey("TEST_KEY");
        fields.setName("Test Name");
        
        // Set non-existent traffic type reference
        Sys trafficTypeRef = new Sys();
        trafficTypeRef.setId("non-existent-id");
        fields.setTrafficType(trafficTypeRef);
        
        templateEntry.setFields(fields);
        response.setItems(List.of(templateEntry));
        
        // When
        MessageTemplate result = transformer.transform(response);
        
        // Then
        assertNotNull(result);
        assertNull(result.getTrafficType());
    }

    @Test
    void transform_whenParameterNodeIsInvalid_shouldSkipInvalidParameters() {
        // Given
        CmsResponse response = new CmsResponse();
        CmsEntry templateEntry = new CmsEntry();
        Sys sys = new Sys();
        sys.setId("test-id");
        templateEntry.setSys(sys);
        
        Fields fields = new Fields();
        fields.setKey("TEST_KEY");
        fields.setName("Test Name");
        
        // Create document with invalid parameter node
        Document document = new Document();
        document.setNodeType("document");
        
        Node invalidNode = new Node();
        invalidNode.setNodeType("invalid");
        
        document.setContent(List.of(createParagraph(List.of(invalidNode))));
        fields.setBody(document);
        
        templateEntry.setFields(fields);
        response.setItems(List.of(templateEntry));
        
        // When
        MessageTemplate result = transformer.transform(response);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getParameters().isEmpty());
    }

    @Test
    void transform_whenDocumentFieldsAreMissing_shouldHandleGracefully() {
        // Given
        CmsResponse response = new CmsResponse();
        CmsEntry templateEntry = new CmsEntry();
        Sys sys = new Sys();
        sys.setId("test-id");
        templateEntry.setSys(sys);
        
        Fields fields = new Fields();
        fields.setKey("TEST_KEY");
        fields.setName("Test Name");
        
        // Don't set subject and body
        templateEntry.setFields(fields);
        response.setItems(List.of(templateEntry));
        
        // When
        MessageTemplate result = transformer.transform(response);
        
        // Then
        assertNotNull(result);
        assertEquals("", result.getBody());
        assertEquals("", result.getSubject());
    }

    private CmsResponse createFullResponse() {
        CmsResponse response = new CmsResponse();
        
        // Create main template entry
        CmsEntry templateEntry = new CmsEntry();
        Sys templateSys = new Sys();
        templateSys.setId("111-111-111");
        templateEntry.setSys(templateSys);
        
        Fields fields = new Fields();
        fields.setKey("TRAIN_DELAYED");
        fields.setName("Juna myöhässä");
        
        // Set traffic type reference
        Sys trafficTypeRef = new Sys();
        trafficTypeRef.setId("222-222-222");
        fields.setTrafficType(trafficTypeRef);
        
        // Create subject document
        Document subject = createDocument(List.of(createParagraph(List.of(
            createTextNode(""),
            createParameterNode("333-333-333"),
            createTextNode(" "),
            createParameterNode("444-444-444"),
            createTextNode(" - "),
            createTextNode("delayed")
        ))));
        fields.setSubject(subject);
        
        // Create body document
        Document body = createDocument(List.of(
            createParagraph(List.of(
                createTextNode("Dear passengers, train "),
                createParameterNode("333-333-333"),
                createTextNode(" "),
                createParameterNode("444-444-444"),
                createTextNode(" is delayed due to "),
                createParameterNode("555-555-555"),
                createTextNode(".")
            )),
            createParagraph(List.of(
                createTextNode("We apologise for the inconvenience. We will update you on the situation as soon as possible.")
            ))
        ));
        fields.setBody(body);
        
        templateEntry.setFields(fields);
        response.setItems(List.of(templateEntry));
        
        // Create includes
        Includes includes = new Includes();
        includes.setEntry(List.of(
            createIncludedEntry("222-222-222", "LONG_DISTANCE", "Kaukoliikenne"),
            createIncludedEntry("333-333-333", "TRAIN_TYPE", "Junalaji"),
            createIncludedEntry("444-444-444", "TRAIN_NUMBER", "Junan numero"),
            createIncludedEntry("555-555-555", "DELAY_REASON", "Myöhästymisen syy")
        ));
        response.setIncludes(includes);
        
        return response;
    }

    private CmsResponse createResponseWithoutIncludes() {
        CmsResponse response = new CmsResponse();
        CmsEntry templateEntry = new CmsEntry();
        Sys sys = new Sys();
        sys.setId("test-id");
        templateEntry.setSys(sys);
        
        Fields fields = new Fields();
        fields.setKey("TEST");
        fields.setName("Test Template");
        templateEntry.setFields(fields);
        
        response.setItems(List.of(templateEntry));
        return response;
    }

    private Node createParagraph(List<Node> content) {
        Node node = new Node();
        node.setNodeType("paragraph");
        node.setContent(content);
        return node;
    }

    private Node createTextNode(String value) {
        Node node = new Node();
        node.setNodeType("text");
        node.setValue(value);
        return node;
    }

    private Node createParameterNode(String id) {
        Node node = new Node();
        node.setNodeType("embedded-entry-inline");
        node.setData(Map.of("target", Map.of("sys", Map.of("id", id))));
        return node;
    }

    private CmsEntry createIncludedEntry(String id, String key, String name) {
        CmsEntry entry = new CmsEntry();
        Sys sys = new Sys();
        sys.setId(id);
        entry.setSys(sys);
        
        Fields fields = new Fields();
        fields.setKey(key);
        fields.setName(name);
        entry.setFields(fields);
        
        return entry;
    }

    private Document createDocument(List<Node> content) {
        Document document = new Document();
        document.setNodeType("document");
        document.setContent(content);
        return document;
    }

    private Document createTestDocument() {
        Document document = new Document();
        document.setNodeType("document");
        document.setContent(Collections.emptyList());
        return document;
    }
} 