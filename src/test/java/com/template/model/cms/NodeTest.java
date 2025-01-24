package com.template.model.cms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;

class NodeTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Node node;

    @BeforeEach
    void setUp() {
        node = new Node();
    }

    @Test
    void shouldDeserializeNode() throws Exception {
        String json = """
        {
            "data": {
                "target": {
                    "sys": {
                        "id": "333-333-333",
                        "type": "Link",
                        "linkType": "Entry"
                    }
                }
            },
            "content": [],
            "nodeType": "embedded-entry-inline"
        }
        """;

        Node node = objectMapper.readValue(json, Node.class);
        assertNotNull(node);
        assertEquals("embedded-entry-inline", node.getNodeType());
    }

    @Test
    void builder_Success() {
        // Given
        String nodeType = "text";
        String value = "Test Value";
        Map<String, Object> data = Map.of("key", "value");
        List<String> marks = List.of("bold", "italic");
        List<Node> content = List.of(new Node());

        // When
        Node node = new Node();
        node.setNodeType(nodeType);
        node.setValue(value);
        node.setData(data);
        node.setMarks(marks);
        node.setContent(content);

        // Then
        assertEquals(nodeType, node.getNodeType());
        assertEquals(value, node.getValue());
        assertEquals(data, node.getData());
        assertEquals(marks, node.getMarks());
        assertFalse(node.getContent().isEmpty());
    }

    @Test
    void testNodeTypeProperty() {
        String nodeType = "paragraph";
        node.setNodeType(nodeType);
        assertEquals(nodeType, node.getNodeType());
    }

    @Test
    void testValueProperty() {
        String value = "Test content";
        node.setValue(value);
        assertEquals(value, node.getValue());
    }

    @Test
    void testMarks() {
        List<String> marks = Arrays.asList("bold", "italic");
        node.setMarks(marks);
        
        assertEquals(2, node.getMarks().size());
        assertTrue(node.getMarks().contains("bold"));
        assertTrue(node.getMarks().contains("italic"));
    }

    @Test
    void testContent() {
        Node childNode1 = new Node();
        childNode1.setNodeType("text");
        childNode1.setValue("Hello");

        Node childNode2 = new Node();
        childNode2.setNodeType("text");
        childNode2.setValue("World");

        List<Node> content = Arrays.asList(childNode1, childNode2);
        node.setContent(content);

        assertEquals(2, node.getContent().size());
        assertEquals("Hello", node.getContent().get(0).getValue());
        assertEquals("World", node.getContent().get(1).getValue());
    }

    @Test
    void testData() {
        Map<String, Object> data = new HashMap<>();
        data.put("align", "center");
        data.put("fontSize", 14);
        data.put("color", "#000000");

        node.setData(data);

        assertEquals(3, node.getData().size());
        assertEquals("center", node.getData().get("align"));
        assertEquals(14, node.getData().get("fontSize"));
        assertEquals("#000000", node.getData().get("color"));
    }

    @Test
    void testEqualsAndHashCode() {
        Node node1 = new Node();
        node1.setNodeType("paragraph");
        node1.setValue("test");
        node1.setMarks(Arrays.asList("bold"));

        Node node2 = new Node();
        node2.setNodeType("paragraph");
        node2.setValue("test");
        node2.setMarks(Arrays.asList("bold"));

        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());
    }
} 