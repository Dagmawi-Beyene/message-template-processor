package com.template.service;

import com.template.model.cms.*;
import com.template.model.domain.MessageTemplate;
import com.template.model.domain.TrafficType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TemplateTransformer {

    /**
     * Transforms a CMS response to a domain MessageTemplate
     * @param response The CMS response containing template and includes
     * @return Transformed MessageTemplate
     */
    public MessageTemplate transform(CmsResponse response) {
        if (response.getItems() == null || response.getItems().isEmpty()) {
            log.warn("No items found in CMS response");
            return null;
        }

        CmsEntry templateEntry = response.getItems().get(0);
        Map<String, CmsEntry> includesMap = createIncludesMap(response);

        return transformEntry(templateEntry, includesMap);
    }

    private Map<String, CmsEntry> createIncludesMap(CmsResponse response) {
        if (response.getIncludes() == null || response.getIncludes().getEntry() == null) {
            return Map.of();
        }

        return response.getIncludes().getEntry().stream()
            .collect(Collectors.toMap(
                entry -> entry.getSys().getId(),
                entry -> entry
            ));
    }

    private MessageTemplate transformEntry(CmsEntry entry, Map<String, CmsEntry> includes) {
        Fields fields = entry.getFields();
        List<String> parameters = new ArrayList<>();

        return MessageTemplate.builder()
            .id(entry.getSys().getId())
            .key(fields.getKey())
            .name(fields.getName())
            .trafficType(extractTrafficType(fields.getTrafficType(), includes))
            .subject(transformDocument(fields.getSubject(), includes, parameters))
            .body(transformDocument(fields.getBody(), includes, parameters))
            .parameters(parameters)
            .build();
    }

    private TrafficType extractTrafficType(Sys trafficTypeRef, Map<String, CmsEntry> includes) {
        if (trafficTypeRef == null || trafficTypeRef.getId() == null) {
            return null;
        }

        CmsEntry trafficType = includes.get(trafficTypeRef.getId());
        if (trafficType == null) {
            log.warn("Traffic type not found for ID: {}", trafficTypeRef.getId());
            return null;
        }

        return TrafficType.builder()
            .key(trafficType.getFields().getKey())
            .name(trafficType.getFields().getName())
            .build();
    }

    private String transformDocument(Document document, Map<String, CmsEntry> includes, List<String> parameters) {
        if (document == null || document.getContent() == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (Node paragraph : document.getContent()) {
            if (paragraph.getContent() != null) {
                for (Node node : paragraph.getContent()) {
                    switch (node.getNodeType()) {
                        case "text":
                            result.append(node.getValue());
                            break;
                            
                        case "embedded-entry-inline":
                            String paramId = extractParameterId(node);
                            if (paramId != null && includes.containsKey(paramId)) {
                                CmsEntry param = includes.get(paramId);
                                String paramKey = param.getFields().getKey();
                                result.append("{").append(paramKey).append("}");
                                if (!parameters.contains(paramKey)) {
                                    parameters.add(paramKey);
                                }
                            }
                            break;
                            
                        default:
                            log.debug("Ignoring node type: {}", node.getNodeType());
                    }
                }
                result.append("\n");
            }
        }

        return result.toString().trim();
    }

    private String extractParameterId(Node node) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> target = (Map<String, Object>) node.getData().get("target");
            @SuppressWarnings("unchecked")
            Map<String, Object> sys = (Map<String, Object>) target.get("sys");
            return (String) sys.get("id");
        } catch (Exception e) {
            log.warn("Failed to extract parameter ID from node", e);
            return null;
        }
    }
} 