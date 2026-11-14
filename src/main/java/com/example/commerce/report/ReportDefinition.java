package com.example.commerce.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** parameters 取值：period（统计期间）、tenant（商户）、category（品类）。 */
public record ReportDefinition(String code, String category, String title, List<String> parameters,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String statement) {
    public ReportDefinition {
        parameters = parameters == null ? List.of("period") : List.copyOf(parameters);
    }
}
