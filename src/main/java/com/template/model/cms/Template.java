package com.template.model.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Template {
    private String id;
    private String name;
    private String description;
    private Map<String, Field> fields;
    private List<DocumentNode> documentNodes;
} 