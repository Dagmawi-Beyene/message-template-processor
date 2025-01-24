package com.template.model.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Node {
    private Map<String, Object> data;
    private List<Node> content;
    private String nodeType;
    private String value;
    private List<String> marks;
} 