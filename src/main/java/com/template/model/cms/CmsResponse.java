package com.template.model.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsResponse {
    private Sys sys;
    private int total;
    private int skip;
    private int limit;
    private List<CmsEntry> items;
    private List<String> errors;
    private Includes includes;

    public Includes getIncludes() {
        return includes;
    }

    public void setIncludes(Includes includes) {
        this.includes = includes;
    }
}
