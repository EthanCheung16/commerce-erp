package com.example.commerce.common;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 以 Map 返回的查询结果保持 SELECT 中的列顺序，便于页面与导出按查询顺序展示。 */
@Configuration
public class MyBatisConfig {
    @Bean
    ConfigurationCustomizer orderedResultMaps() {
        return configuration -> configuration.setObjectFactory(new DefaultObjectFactory() {
            @Override
            protected Class<?> resolveInterface(Class<?> type) {
                return type == Map.class ? LinkedHashMap.class : super.resolveInterface(type);
            }
        });
    }
}
