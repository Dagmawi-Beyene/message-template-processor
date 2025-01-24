package com.template.model.cms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DocumentTest {
    private Document document;

    @BeforeEach
    void setUp() {
        document = new Document();
    }

    @Test
    void testNodeTypeProperty() {
        String nodeType = "document";
        document.setNodeType(nodeType);
        assertEquals(nodeType, document.getNodeType());
    }

    @Test
    void testContent() {
        Node node1 = new Node();
        node1.setNodeType("paragraph");
        node1.setValue("First paragraph");

        Node node2 = new Node();
        node2.setNodeType("paragraph");
        node2.setValue("Second paragraph");

        List<Node> content = Arrays.asList(node1, node2);
        document.setContent(content);

        assertEquals(2, document.getContent().size());
        assertEquals("First paragraph", document.getContent().get(0).getValue());
        assertEquals("Second paragraph", document.getContent().get(1).getValue());
    }

    @Test
    void testData() {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Test Document");
        data.put("version", 1);
        data.put("published", true);

        document.setData(data);

        assertEquals(3, document.getData().size());
        assertEquals("Test Document", document.getData().get("title"));
        assertEquals(1, document.getData().get("version"));
        assertEquals(true, document.getData().get("published"));
    }

    @Test
    void testEqualsAndHashCode() {
        Document doc1 = new Document();
        doc1.setNodeType("document");
        Map<String, Object> data1 = new HashMap<>();
        data1.put("title", "Test");
        doc1.setData(data1);

        Document doc2 = new Document();
        doc2.setNodeType("document");
        Map<String, Object> data2 = new HashMap<>();
        data2.put("title", "Test");
        doc2.setData(data2);

        assertEquals(doc1, doc2);
        assertEquals(doc1.hashCode(), doc2.hashCode());
    }

    @Test
    void testNullHandling() {
        assertNull(document.getContent());
        assertNull(document.getData());
        assertNull(document.getNodeType());

        document.setContent(null);
        document.setData(null);
        document.setNodeType(null);

        assertNull(document.getContent());
        assertNull(document.getData());
        assertNull(document.getNodeType());
    }
} 