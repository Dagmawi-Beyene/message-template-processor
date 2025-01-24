// implement the Includes class
package com.template.model.cms;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Includes {
    @JsonProperty("Entry")
    private List<CmsEntry> entry;
} 