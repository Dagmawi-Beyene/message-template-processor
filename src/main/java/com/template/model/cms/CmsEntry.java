package com.template.model.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsEntry {
    private Metadata metadata;
    private Sys sys;
    private Fields fields;
}

@Data
class Metadata {
    private String[] tags;
    private String[] concepts;
} 