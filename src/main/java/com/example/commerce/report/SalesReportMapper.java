package com.example.commerce.report;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SalesReportMapper {
    List<Map<String, Object>> merchantDailySales(ReportFilter filter);
    List<Map<String, Object>> merchantCategoryAnnual(ReportFilter filter);
    List<Map<String, Object>> channelPaymentReceipts(ReportFilter filter);
    List<Map<String, Object>> afterSalesCategorySales(ReportFilter filter);
    List<Map<String, Object>> merchantOrderMargin(ReportFilter filter);
    List<Map<String, Object>> channelKpi(ReportFilter filter);
    List<Map<String, Object>> offlineProductSales(ReportFilter filter);
    List<Map<String, Object>> premiumSkuBuyers(ReportFilter filter);
    List<Map<String, Object>> brandStockRiskSales(ReportFilter filter);
    List<Map<String, Object>> repeatBuyerProvince(ReportFilter filter);
    List<Map<String, Object>> categorySkuRank(ReportFilter filter);
    List<Map<String, Object>> categoryStatusSales(ReportFilter filter);
    List<Map<String, Object>> supplierPriceBand(ReportFilter filter);
    List<Map<String, Object>> brandPurchaseBatches(ReportFilter filter);
    List<Map<String, Object>> brandMargin(ReportFilter filter);
    List<Map<String, Object>> monthlySales(ReportFilter filter);
    List<Map<String, Object>> categoryWeightBand(ReportFilter filter);
    List<Map<String, Object>> orderSequence(ReportFilter filter);
    List<Map<String, Object>> merchantQuarterSales(ReportFilter filter);
}
