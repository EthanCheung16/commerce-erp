package com.example.commerce.purchase;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PurchaseMapper {
    List<Map<String, Object>> purchases(@Param("status") String status, @Param("beforeId") Long beforeId,
            @Param("limit") int limit);
    Map<String, Object> purchase(long id);
    List<Map<String, Object>> purchaseItems(long id);

    List<Map<String, Object>> purchaseByNo(@Param("poNo") String poNo);
    List<Map<String, Object>> supplierReceipts(@Param("supplierId") long supplierId);
    List<Map<String, Object>> warehouseReceipts(@Param("warehouseId") long warehouseId,
            @Param("from") LocalDate from, @Param("until") LocalDate until);
    List<Map<String, Object>> supplierSkuSales(@Param("supplierId") long supplierId,
            @Param("from") LocalDate from, @Param("until") LocalDate until);
    List<Map<String, Object>> restockSuggestions(@Param("supplierId") long supplierId,
            @Param("asOf") LocalDate asOf);
}
