package com.example.commerce.logistics;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LogisticsMapper {
    List<Map<String, Object>> byTrackingNo(@Param("trackingNo") String trackingNo);
    List<Map<String, Object>> search(@Param("keyword") String keyword);
    List<Map<String, Object>> shipmentPage(@Param("offset") int offset, @Param("size") int size);
    List<Map<String, Object>> customerShipments(@Param("customerId") long customerId);
    List<Map<String, Object>> customerTracking(@Param("customerId") long customerId);
    List<Map<String, Object>> overdueDeliveries(@Param("hours") int hours);
    List<Map<String, Object>> daily(@Param("from") LocalDate from, @Param("until") LocalDate until);
}
