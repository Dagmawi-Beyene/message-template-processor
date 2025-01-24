package com.template.model.cms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class TemplateTest {
    private Template template;

    @BeforeEach
    void setUp() {
        template = new Template();
    }

    @Test
    void testTemplateBasicProperties() {
        String id = "template-123";
        String name = "Welcome Email";
        String description = "Template for welcome emails";

        template.setId(id);
        template.setName(name);
        template.setDescription(description);

        assertEquals(id, template.getId());
        assertEquals(name, template.getName());
        assertEquals(description, template.getDescription());
    }

    @Test
    void testTemplateFields() {
        Map<String, Field> fields = new HashMap<>();
        Field subjectField = new Field();
        subjectField.setType("String");
        subjectField.setLabel("Email Subject");
        subjectField.setRequired(true);

        fields.put("subject", subjectField);
        template.setFields(fields);

        assertEquals(1, template.getFields().size());
        assertEquals(subjectField, template.getFields().get("subject"));
    }

    @Test
    void testDocumentNodes() {
        DocumentNode node1 = new DocumentNode();
        node1.setNodeType("text");
        node1.setContent("Hello");

        DocumentNode node2 = new DocumentNode();
        node2.setNodeType("paragraph");
        node2.setContent("World");

        ArrayList<DocumentNode> nodes = new ArrayList<>();
        nodes.add(node1);
        nodes.add(node2);

        template.setDocumentNodes(nodes);

        assertEquals(2, template.getDocumentNodes().size());
        assertEquals("text", template.getDocumentNodes().get(0).getNodeType());
        assertEquals("paragraph", template.getDocumentNodes().get(1).getNodeType());
    }

    @Test
    void testTemplateEqualsAndHashCode() {
        Template template1 = new Template();
        template1.setId("123");
        template1.setName("Test Template");
        
        Template template2 = new Template();
        template2.setId("123");
        template2.setName("Test Template");
        
        assertEquals(template1, template2);
        assertEquals(template1.hashCode(), template2.hashCode());
    }

    @Test
    void testTemplateFieldsNull() {
        assertNull(template.getFields());
        
        template.setFields(null);
        assertNull(template.getFields());
    }

    @Test
    void testTemplateDocumentNodesNull() {
        assertNull(template.getDocumentNodes());
        
        template.setDocumentNodes(null);
        assertNull(template.getDocumentNodes());
    }
} 