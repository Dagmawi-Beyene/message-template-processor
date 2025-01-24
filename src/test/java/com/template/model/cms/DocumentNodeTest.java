package com.template.model.cms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

class DocumentNodeTest {
    private DocumentNode node;

    @BeforeEach
    void setUp() {
        node = new DocumentNode();
    }

    @Test
    void testNodeTypeProperty() {
        String nodeType = "paragraph";
        node.setNodeType(nodeType);
        assertEquals(nodeType, node.getNodeType());
    }

    @Test
    void testContentProperty() {
        String content = "Hello World";
        node.setContent(content);
        assertEquals(content, node.getContent());
    }

    @Test
    void testDataProperty() {
        Map<String, Object> data = new HashMap<>();
        data.put("align", "center");
        data.put("fontSize", 14);
        data.put("bold", true);

        node.setData(data);

        assertEquals(3, node.getData().size());
        assertEquals("center", node.getData().get("align"));
        assertEquals(14, node.getData().get("fontSize"));
        assertEquals(true, node.getData().get("bold"));
    }

    @Test
    void testEqualsAndHashCode() {
        DocumentNode node1 = new DocumentNode();
        node1.setNodeType("text");
        node1.setContent("Test");

        DocumentNode node2 = new DocumentNode();
        node2.setNodeType("text");
        node2.setContent("Test");

        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());
    }
} 