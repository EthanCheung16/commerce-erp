-- 业务数据初始化；仅适用于空表，重复导入会因主键冲突停止。

USE commerce_erp;

SET NAMES utf8mb4;

CREATE TEMPORARY TABLE import_sequence (n INT PRIMARY KEY);

INSERT INTO import_sequence WITH digit AS (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) SELECT 1+a.n+10*b.n+100*c.n+1000*d.n+10000*e.n+100000*f.n AS n FROM digit a CROSS JOIN digit b CROSS JOIN digit c CROSS JOIN digit d CROSS JOIN digit e CROSS JOIN digit f WHERE 1+a.n+10*b.n+100*c.n+1000*d.n+10000*e.n+100000*f.n<=360000;

START TRANSACTION;

INSERT INTO tenant (id, name) SELECT n, CONCAT('商户-',n) FROM import_sequence WHERE n<=8;

INSERT INTO category (id,name,parent_id) SELECT n,CONCAT('品类-',n),IF(n<=8,NULL,1+MOD(n,8)) FROM import_sequence WHERE n<=80;

INSERT INTO brand (id,name,country) SELECT n,CONCAT('品牌-',n),ELT(1+MOD(n,4),'中国','马来西亚','日本','德国') FROM import_sequence WHERE n<=80;

INSERT INTO supplier (id,name,province,rating) SELECT n,CONCAT('供应商-',n),ELT(1+MOD(n,5),'广东','浙江','江苏','福建','山东'),1+MOD(n,5) FROM import_sequence WHERE n<=200;

INSERT INTO warehouse (id,name,city) SELECT n,CONCAT('区域仓-',n),ELT(n,'广州','杭州','武汉','成都') FROM import_sequence WHERE n<=4;

INSERT INTO customer (id,tenant_id,name,email,phone,tier,province,registered_at) SELECT n,1+MOD(n-1,8),CONCAT('客户-',n),CONCAT('customer',n,'@example.com'),CONCAT('138',LPAD(n,8,'0')),ELT(1+MOD(n,4),'BRONZE','SILVER','GOLD','PLATINUM'),ELT(1+MOD(n,5),'广东','浙江','江苏','四川','湖北'),TIMESTAMPADD(DAY,-MOD(n,730),'2025-01-01') FROM import_sequence WHERE n<=20000;

INSERT INTO customer_address (id,customer_id,province,city,street,postal_code) SELECT n,1+MOD(n-1,20000),ELT(1+MOD(n,5),'广东','浙江','江苏','四川','湖北'),ELT(1+MOD(n,5),'广州','杭州','南京','成都','武汉'),CONCAT('示例路',n,'号'),LPAD(100000+MOD(n,50000),6,'0') FROM import_sequence WHERE n<=25000;

INSERT INTO product (id,tenant_id,category_id,brand_id,supplier_id,name,status,created_at) SELECT n,1+MOD(n-1,8),1+MOD(n,80),1+MOD(n*3,80),1+MOD(n,200),CONCAT('商品-',n),IF(MOD(n,20)=0,'OFFLINE','ACTIVE'),TIMESTAMPADD(DAY,-MOD(n,500),'2025-01-01') FROM import_sequence WHERE n<=8000;

INSERT INTO sku (id,product_id,code,specification,price,cost,weight_grams) SELECT n,1+MOD(n-1,8000),CONCAT('SKU-',LPAD(n,8,'0')),CONCAT('规格-',1+MOD(n,6)),10+MOD(n*37,990)+MOD(n,100)/100,5+MOD(n*19,400),100+MOD(n*23,5000) FROM import_sequence WHERE n<=12000;

INSERT INTO inventory (id,warehouse_id,sku_id,available_qty,reserved_qty,reorder_point,updated_at) SELECT n,1+FLOOR((n-1)/12000),1+MOD(n-1,12000),50+MOD(n*17,450),MOD(n,30),30,TIMESTAMPADD(DAY,MOD(n,365),'2025-01-01') FROM import_sequence WHERE n<=48000;

INSERT INTO sales_order (id,order_no,tenant_id,customer_id,address_id,warehouse_id,status,channel,ordered_at,paid_at,discount_amount,shipping_fee,remark) SELECT n,CONCAT('SO2025',LPAD(n,9,'0')),1+MOD(customer_no-1,8),customer_no,customer_no,1+MOD(n,4),
ELT(1+MOD(n,6),'PENDING','PAID','SHIPPED','COMPLETED','CANCELLED','REFUNDING'),
ELT(1+MOD(FLOOR(n/7),4),'WEB','APP','MARKETPLACE','STORE'),
TIMESTAMPADD(SECOND,MOD(n*7919,31536000),'2025-01-01'),
IF(MOD(n,6) IN (0,4),NULL,TIMESTAMPADD(SECOND,MOD(n*7919,31536000)+600,'2025-01-01')),
MOD(n,50),IF(MOD(n,3)=0,0,8),CONCAT('订单配送备注-',n,'；工作日优先配送') FROM (SELECT n,IF(MOD(n,10)<7,1+MOD(n*37,2000),1+MOD(n*37,20000)) customer_no FROM import_sequence WHERE n<=120000) x;

INSERT INTO sales_order_item (id,order_id,sku_id,quantity,unit_price,discount_amount,line_amount,created_at,fulfillment_status,promotion_code) SELECT n,o.id,s.id,1+MOD(n,4),s.price,MOD(n,10),s.price*(1+MOD(n,4))-MOD(n,10),o.ordered_at,
ELT(1+MOD(n,4),'ALLOCATED','PACKED','SHIPPED','RETURNED'),IF(MOD(n,3)=0,CONCAT('PROMO-',MOD(n,30)),NULL) FROM import_sequence z JOIN sales_order o ON o.id=1+FLOOR((z.n-1)/3)
JOIN sku s ON s.id=IF(MOD(z.n,10)<7,1+MOD(z.n*7919,900),1+MOD(z.n*7919,12000)) WHERE z.n<=360000;

UPDATE sales_order o JOIN (SELECT order_id,SUM(line_amount) amount FROM sales_order_item GROUP BY order_id) x ON x.order_id=o.id SET o.total_amount=x.amount-o.discount_amount+o.shipping_fee;

INSERT INTO payment (id,order_id,method,status,amount,paid_at,transaction_no) SELECT n,o.id,ELT(1+MOD(n,4),'ALIPAY','WECHAT','CARD','BANK'),IF(MOD(n,17)=0,'REFUNDED','SUCCESS'),o.total_amount,
COALESCE(o.paid_at,o.ordered_at),CONCAT('TX-',LPAD(n,12,'0')) FROM import_sequence z JOIN sales_order o ON o.id=FLOOR((n-1)/4)*6+ELT(1+MOD(n-1,4),1,2,3,5) WHERE n<=80000;

INSERT INTO shipment (id,order_id,warehouse_id,carrier,tracking_no,status,shipped_at,delivered_at,freight_cost) SELECT n,o.id,o.warehouse_id,ELT(1+MOD(n,4),'SF','YTO','JD','ZTO'),CONCAT('TRACK-',LPAD(n,12,'0')),
IF(MOD(n,7)=0,'IN_TRANSIT','DELIVERED'),TIMESTAMPADD(DAY,1,o.ordered_at),
IF(MOD(n,7)=0,NULL,TIMESTAMPADD(DAY,2+MOD(n,7),o.ordered_at)),5+MOD(n,12) FROM import_sequence z JOIN sales_order o ON o.id=FLOOR((n-1)/3)*6+ELT(1+MOD(n-1,3),2,3,5) WHERE n<=50000;

INSERT INTO shipment_event (id,shipment_id,event_type,occurred_at,location) SELECT n,s.id,IF(n<=50000,'PICKED_UP','IN_TRANSIT'),TIMESTAMPADD(HOUR,FLOOR((n-1)/50000)*8,s.shipped_at),CONCAT('转运中心-',MOD(n,100)) FROM import_sequence z JOIN shipment s ON s.id=1+MOD(n-1,50000) WHERE n<=60000;

INSERT INTO purchase_order (id,supplier_id,warehouse_id,status,ordered_at,expected_at) SELECT n,1+MOD(n,200),1+MOD(n,4),ELT(1+MOD(n,3),'ORDERED','RECEIVED','PARTIAL'),TIMESTAMPADD(DAY,MOD(n,365),'2025-01-01'),TIMESTAMPADD(DAY,7+MOD(n,365),'2025-01-01') FROM import_sequence WHERE n<=5000;

INSERT INTO purchase_order_item (id,purchase_order_id,sku_id,quantity,received_qty,unit_cost) SELECT n,1+FLOOR((n-1)/3),1+MOD(n*7919,12000),100+MOD(n,200),LEAST(100+MOD(n,200),50+MOD(n,250)),5+MOD(n*19,400) FROM import_sequence WHERE n<=15000;

INSERT INTO return_order (id,order_id,customer_id,reason,status,requested_at,refund_amount) SELECT n,o.id,o.customer_id,ELT(1+MOD(n,4),'DAMAGED','WRONG_ITEM','NO_LONGER_NEEDED','LATE_DELIVERY'),
ELT(1+MOD(n,3),'REQUESTED','APPROVED','REFUNDED'),TIMESTAMPADD(DAY,10,o.ordered_at),LEAST(20+MOD(n,400),o.total_amount) FROM import_sequence z JOIN sales_order o ON o.id=n*6-1 WHERE n<=8000;

INSERT INTO return_item (id,return_order_id,order_item_id,quantity,disposition) SELECT n,r.id,(r.order_id-1)*3+1+FLOOR((n-1)/8000),1,IF(MOD(n,5)=0,'SCRAP','RESTOCK') FROM import_sequence z JOIN return_order r ON r.id=1+MOD(n-1,8000) WHERE n<=12000;

INSERT INTO stock_movement (id,warehouse_id,sku_id,movement_type,quantity,occurred_at,reference_no) SELECT n,1+MOD(n,4),1+MOD(n*7919,12000),ELT(1+MOD(n,4),'PURCHASE','SALE','RETURN','ADJUSTMENT'),IF(MOD(n,4)=1,-1,1)*(1+MOD(n,30)),TIMESTAMPADD(SECOND,MOD(n*7919,31536000),'2025-01-01'),CONCAT('STK-',n) FROM import_sequence WHERE n<=100000;

INSERT INTO coupon (id,tenant_id,code,minimum_amount,discount_amount,valid_until) SELECT n,1+MOD(n-1,8),CONCAT('COUPON-',n),100+MOD(n,500),5+MOD(n,50),'2026-01-01' FROM import_sequence WHERE n<=300;

INSERT INTO customer_coupon (id,customer_id,coupon_id,status,issued_at) SELECT n,1+MOD(n*37,20000),1+MOD(n,300),IF(MOD(n,3)=0,'USED','AVAILABLE'),TIMESTAMPADD(DAY,MOD(n,365),'2025-01-01') FROM import_sequence WHERE n<=10000;

INSERT INTO product_review (id,order_item_id,customer_id,sku_id,rating,content,created_at) SELECT n,i.id,o.customer_id,i.sku_id,1+MOD(n,5),CONCAT('商品评价-',n,'：包装完整，物流及时，使用体验记录。'),TIMESTAMPADD(DAY,12,o.ordered_at) FROM import_sequence z JOIN sales_order_item i ON i.id=n*9 JOIN sales_order o ON o.id=i.order_id WHERE n<=30000;

INSERT INTO shopping_cart (id,customer_id,updated_at) SELECT n,n,TIMESTAMPADD(DAY,MOD(n,365),'2025-01-01') FROM import_sequence WHERE n<=10000;

INSERT INTO shopping_cart_item (id,cart_id,sku_id,quantity,added_at) SELECT n,1+FLOOR((n-1)/2),1+MOD(n*7919,12000),1+MOD(n,3),TIMESTAMPADD(DAY,MOD(n,365),'2025-01-01') FROM import_sequence WHERE n<=20000;

COMMIT;

ANALYZE TABLE tenant,category,brand,supplier,warehouse,customer,customer_address,product,sku,inventory,sales_order,sales_order_item,payment,shipment,shipment_event,purchase_order,purchase_order_item,return_order,return_item,stock_movement,coupon,customer_coupon,product_review,shopping_cart,shopping_cart_item;

DROP TEMPORARY TABLE import_sequence;
