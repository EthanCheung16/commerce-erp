package com.example.commerce.dashboard;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DashboardMapper {
    List<Map<String, Object>> orderStatusCounts();
    java.math.BigDecimal receivedAmount();
    Map<String, Object> inventorySummary();
    List<Map<String, Object>> channelBoard(@Param("from") LocalDate from, @Param("until") LocalDate until);

    @Select("SELECT id,name FROM category ORDER BY id")
    List<Map<String, Object>> categories();

    @Select("SELECT id,name,city FROM warehouse ORDER BY id")
    List<Map<String, Object>> warehouses();

    @Select("SELECT id,name FROM tenant WHERE enabled ORDER BY id")
    List<Map<String, Object>> tenants();
}
