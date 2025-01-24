package com.template.model.domain;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageTemplate {
    private String id;
    private String key;
    private String name;
    private TrafficType trafficType;
    private String subject;
    private String body;
    private List<String> parameters;
}