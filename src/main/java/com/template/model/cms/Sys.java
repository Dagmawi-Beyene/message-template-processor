package com.template.model.cms;

import lombok.Data;

@Data
public class Sys {
    private String type;
    private String id;
    private String linkType;
    private String createdAt;
    private String updatedAt;
    private Integer revision;
    private Space space;
    private Environment environment;
    private ContentType contentType;
}

@Data
class Space {
    private Sys sys;
}

@Data
class Environment {
    private Sys sys;
}

@Data
class ContentType {
    private Sys sys;
} 