package com.example.commerce.catalog;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CatalogMapper {
    List<Map<String, Object>> products(@Param("categoryId") Long categoryId, @Param("afterId") long afterId,
            @Param("limit") int limit);

    @Select("SELECT id,code,specification,price,weight_grams FROM sku WHERE product_id=#{productId} ORDER BY id LIMIT 100")
    List<Map<String, Object>> productSkus(long productId);

    @Select("SELECT id,name FROM category ORDER BY id")
    List<Map<String, Object>> categories();

    List<Map<String, Object>> productSearch(@Param("keyword") String keyword);
    List<Map<String, Object>> offlineStock();
    List<Map<String, Object>> brandBestSellers(@Param("brandId") long brandId);
    List<Map<String, Object>> brandLatestPrices(@Param("brandId") long brandId);
    List<Map<String, Object>> categoryReturnRates(@Param("categoryId") long categoryId);
    List<Map<String, Object>> categoryFirstBuyers(@Param("categoryId") long categoryId);
    List<Map<String, Object>> promotions(@Param("status") String status);
    List<Map<String, Object>> promotionDaily(@Param("code") String code, @Param("from") LocalDate from,
            @Param("until") LocalDate until);
    List<Map<String, Object>> couponThresholds(@Param("tenantId") long tenantId, @Param("from") LocalDate from,
            @Param("until") LocalDate until);
}
