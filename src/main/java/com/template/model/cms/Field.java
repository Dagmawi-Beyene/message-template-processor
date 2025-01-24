package com.template.model.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Field {
    private String type;
    private String label;
    private boolean required;
    private Object defaultValue;
} 