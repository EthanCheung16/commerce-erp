package com.example.commerce.business;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BusinessService {
    private final BusinessMapper mapper;
    public BusinessService(BusinessMapper mapper) { this.mapper = mapper; }

    public Map<String,Object> order(long id) {
        return detail(mapper.order(id),mapper.orderItems(id));
    }
    public Map<String,Object> purchase(long id) {
        return detail(mapper.purchase(id),mapper.purchaseItems(id));
    }
    public Map<String,Object> returnOrder(long id) {
        return detail(mapper.returnOrder(id),mapper.returnItems(id));
    }
    private Map<String,Object> detail(Map<String,Object> header,List<Map<String,Object>> items) {
        if (header == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"记录不存在");
        return Map.of("header",header,"items",items);
    }

    @Transactional
    public Map<String,Object> createOrder(OrderRequest request) {
        var customer=mapper.customer(request.customerId());
        if (customer == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"客户不存在");
        if (mapper.ownsAddress(request.customerId(),request.addressId()) != 1) {
            throw new IllegalArgumentException("收货地址不属于该客户");
        }
        var lines = new TreeMap<Long,Integer>();
        for (var line : request.items()) {
            if (lines.putIfAbsent(line.skuId(),line.quantity()) != null) {
                throw new IllegalArgumentException("同一 SKU 请合并为一条明细");
            }
        }
        var prices=new HashMap<Long,BigDecimal>();
        BigDecimal total=BigDecimal.ZERO;
        // 按 SKU 顺序修改库存，减少并发下单时的死锁机会；失败会回滚全部预留。
        for (var line : lines.entrySet()) {
            var sku=mapper.activeSku(line.getKey());
            if (sku == null) throw new IllegalArgumentException("SKU 不存在或已下架: "+line.getKey());
            if (mapper.reserve(request.warehouseId(),line.getKey(),line.getValue()) != 1) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,"库存不足: "+line.getKey());
            }
            var price=(BigDecimal)sku.get("price");
            prices.put(line.getKey(),price);
            total=total.add(price.multiply(BigDecimal.valueOf(line.getValue())));
        }
        var draft=new OrderDraft();
        draft.orderNo="SO-"+UUID.randomUUID().toString().replace("-","");
        draft.tenantId=((Number)customer.get("tenant_id")).longValue();
        draft.customerId=request.customerId(); draft.addressId=request.addressId();
        draft.warehouseId=request.warehouseId(); draft.channel=request.channel();
        draft.orderedAt=LocalDateTime.now(); draft.totalAmount=total;
        mapper.insertOrder(draft);
        for (var line : lines.entrySet()) {
            var price=prices.get(line.getKey());
            mapper.insertItem(draft.id,line.getKey(),line.getValue(),price,
                    price.multiply(BigDecimal.valueOf(line.getValue())),draft.orderedAt);
        }
        return order(draft.id);
    }
}
