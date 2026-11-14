package com.example.commerce.report;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReportDefinition(String code, String category, String title,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String statement) {}
