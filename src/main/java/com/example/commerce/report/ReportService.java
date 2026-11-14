package com.example.commerce.report;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@DependsOn({"xmlReportMapper", "annotationReportMapper"})
public class ReportService {
    private final Map<String, ReportDefinition> catalog = new LinkedHashMap<>();
    private final SqlSessionTemplate sessions;

    public ReportService(ObjectMapper json, SqlSessionTemplate sessions) throws IOException {
        this.sessions = sessions;
        try (var stream = new ClassPathResource("reports.json").getInputStream()) {
            List<ReportDefinition> definitions = json.readValue(stream, new TypeReference<>() {});
            for (var definition : definitions) {
                if (catalog.putIfAbsent(definition.code(), definition) != null
                        || !sessions.getConfiguration().hasStatement(definition.statement())) {
                    throw new IllegalStateException("重复或缺失的报表映射: " + definition.code());
                }
            }
        }
        if (catalog.isEmpty()) throw new IllegalStateException("未配置业务报表");
    }

    public List<ReportDefinition> catalog() {
        return List.copyOf(catalog.values());
    }

    public List<Map<String, Object>> run(String code, ReportFilter filter) {
        var definition = catalog.get(code);
        if (definition == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "报表不存在: " + code);
        // SQL ID 只来自静态白名单；请求参数不能提供 SQL 或任意 Mapper 名称。
        return sessions.selectList(definition.statement(), filter);
    }
}
