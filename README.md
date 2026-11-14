# 商序 ERP

订单、客户、商品、仓储、采购、售后、物流与财务管理系统。前端随 Spring Boot 应用一起打包，启动 JAR 后即可通过浏览器使用。

## 环境

- JDK 21
- Maven 3.9+
- MySQL 8

应用使用 Spring Boot 3.5、MyBatis 和 MySQL Connector/J。页面使用原生 JavaScript 与 CSS，无需单独安装 Node.js 或启动前端服务。

## 初始化数据库

在 PowerShell 中进入项目目录，首次使用时执行：

```powershell
.\scripts\init-db.ps1
```

脚本使用本机 MySQL，创建 `commerce_erp` 数据库并导入业务初始数据（通常不到 1 分钟）。数据库已有业务表时会停止，避免重复导入。也可按顺序执行 `database/schema.sql` 和 `database/initial-data.sql`。

MySQL 安装位置或连接信息不同时，可传入参数：

```powershell
.\scripts\init-db.ps1 -Mysql 'D:\DevelopProgramFiles\mysql-8.4\bin\mysql.exe' -DbHost '127.0.0.1' -DbPort 3306 -DbUser 'root' -DbPassword 'root'
```

## 构建和启动

```powershell
$env:JAVA_HOME = 'D:\DevelopProgramFiles\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn clean package
.\scripts\start.ps1
```

打开 [商序 ERP](http://127.0.0.1:8080/)。也可直接运行：

```powershell
& 'D:\DevelopProgramFiles\jdk-21\bin\java.exe' -jar .\target\commerce-erp-1.0.0.jar
```

`start.ps1 -Port 8081` 可指定端口；使用 `stop.ps1` 停止通过本项目 JAR 启动的进程。

连接配置位于 `src/main/resources/application.yml`，可通过环境变量覆盖：

| 变量 | 默认值 | 用途 |
| --- | --- | --- |
| `DB_HOST` | `127.0.0.1` | 数据库地址 |
| `DB_PORT` | `3306` | 数据库端口 |
| `DB_NAME` | `commerce_erp` | 数据库名称 |
| `DB_USER` | `root` | 数据库账号 |
| `DB_PASSWORD` | `root` | 数据库密码 |
| `SERVER_PORT` | `8080` | 应用端口 |

## 业务功能

- 经营概览：订单状态、累计成交额、商品规格与库存情况。
- 订单管理：订单列表与详情、新建订单与库存预留；订单搜索、分页浏览、导出、重复下单与金额核对、订单日历、商户成交流水、渠道看板。
- 客户管理：客户搜索、来电查单、客户概览与排名、客户动态、履约与出库记录、常购商品、客户分群与会员升级候选。
- 商品营销：商品目录与 SKU 规格；商品搜索、品牌畅销与成交价、品类退货率与首购、促销活动与优惠券门槛分析。
- 库存管理：各仓库可用与预留数量；仓库日报、库存流水账、库存核对、滞销与畅销商品、待出库与发货出库核对。
- 采购管理：采购列表与收货明细；供应商入库、仓库到货核对、供应商商品动销与补货建议。
- 售后管理：售后列表与退货明细；退货回库核对、退款核对与退货入库流水。
- 物流跟踪：物流查询、运单分页、客户运单与轨迹、配送超时与物流日报。
- 财务结算：支付流水与支付订单、支付查询、收款核对、收款日记账、资金日历、平台结算明细与批次对账。
- 报表中心：销售经营、客户分析、商品分析、仓储履约、采购供应和财务结算，支持期间、商户与品类条件及 CSV 导出。

新建订单时，收货地址必须属于所选客户。商品单价以数据库记录为准，创建成功后预留对应仓库库存；库存不足时整个订单事务回滚。

日期区间均为左闭右开，结束日期不包含当天。

## 代码结构

```text
database/                         数据库结构与初始数据
scripts/                          初始化、启动与停止脚本
src/main/java/com/example/commerce/
  order/                          订单查询、下单与库存预留
  customer/                       客户资料与客户服务查询
  catalog/                        商品、品牌、品类、促销与优惠券
  inventory/                      仓库库存与出入库
  purchase/                       采购与供应商
  aftersale/                      售后与退款
  logistics/                      运单与物流轨迹
  finance/                        支付与平台结算
  dashboard/                      经营概览与基础选项
  report/                         报表目录与查询服务
  common/                         公共参数校验
src/main/resources/
  mapper/                         MyBatis XML Mapper
  reports.json                    业务报表配置
  static/                         随应用发布的前端资源
src/test/java/                    集成测试
```

MyBatis 映射同时使用 XML 与注解两种写法，按业务模块组织。

## 接口

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| GET | `/api/health` | 应用状态 |
| GET | `/api/dashboard` | 经营概览 |
| GET | `/api/dashboard/channels` | 渠道经营看板 |
| GET | `/api/options` | 仓库、品类、商户等选项 |
| GET / POST | `/api/orders` | 查询、新建订单 |
| GET | `/api/orders/{id}` | 订单详情 |
| GET | `/api/orders/search`、`/search/suffix`、`/search/remark`、`/search/advanced`、`/by-product` | 订单搜索 |
| GET | `/api/orders/pages`、`/pages/by-amount`、`/items/pages`、`/count`、`/export` | 订单分页、计数与导出 |
| GET | `/api/orders/duplicates`、`/anomalies`、`/amount-check`、`/margins`、`/promotion`、`/calendar`、`/tenant-ledger` | 订单核对与分析 |
| GET | `/api/customers/search`、`/by-phone/latest-order`、`/segments`、`/upgrade-candidates` | 客户查询 |
| GET | `/api/customers/{id}/overview`、`/ranking`、`/timeline`、`/related-orders`、`/fulfillment`、`/outbound`、`/frequent-skus` | 客户详情 |
| GET | `/api/products`、`/api/products/{id}/skus`、`/api/products/search`、`/api/products/offline-stock` | 商品 |
| GET | `/api/brands/{id}/best-sellers`、`/latest-prices` | 品牌 |
| GET | `/api/categories/{id}/return-rates`、`/first-buyers` | 品类 |
| GET | `/api/promotions`、`/api/promotions/{code}/daily`、`/api/coupons/thresholds` | 促销与优惠券 |
| GET | `/api/inventory` | 库存查询 |
| GET | `/api/warehouses/{id}/daily-items`、`/daily`、`/ledger`、`/reconciliation`、`/stagnant-skus`、`/top-sellers`、`/pending-outbound`、`/shipped-check`、`/receipts` | 仓库 |
| GET | `/api/purchases`、`/api/purchases/{id}` | 采购 |
| GET | `/api/suppliers/{id}/receipts`、`/sku-sales`、`/restock-suggestions` | 供应商 |
| GET | `/api/returns`、`/api/returns/{id}`、`/restock-check`、`/refund-check`、`/movements` | 售后 |
| GET | `/api/shipments/search`、`/pages`、`/by-customer`、`/tracking`、`/overdue`、`/daily` | 物流 |
| GET | `/api/payments/daily`、`/paid-orders`、`/search`、`/pages`、`/order-check`、`/ledger`、`/calendar` | 支付 |
| GET | `/api/settlements`、`/summary`、`/differences`、`/{batchNo}` | 平台结算 |
| GET | `/api/reports` | 报表目录 |
| GET | `/api/reports/{code}?from=2025-01-01&until=2026-01-01&tenantId=1&categoryId=5` | 报表结果 |

## 测试

完成数据库初始化后运行 `mvn test`。集成测试覆盖报表配置、业务读取、参数校验、前端资源、订单创建、库存预留与事务回滚。
