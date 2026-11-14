# 商序 ERP

订单、商品、仓储、采购与售后管理系统。前端随 Spring Boot 应用一起打包，启动 JAR 后即可通过浏览器使用。

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

脚本使用本机 MySQL，创建 `commerce_erp` 数据库并导入业务初始数据。数据库已有业务表时会停止，避免重复导入。也可按顺序执行 `database/schema.sql` 和 `database/initial-data.sql`。

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
- 销售订单：客户与状态筛选、订单明细、新建订单与库存预留。
- 商品目录：品类筛选、品牌信息与 SKU 规格。
- 仓储库存：各仓库可用数量、预留数量与补货阈值。
- 采购与售后：采购进度、收货明细、售后状态与退货明细。
- 报表中心：销售经营、客户分析、商品分析、仓储履约、采购供应和财务结算，支持日期查询及 CSV 导出。

新建订单时，收货地址必须属于所选客户。商品单价以数据库记录为准，创建成功后预留对应仓库库存；库存不足时整个订单事务回滚。

报表日期采用左闭右开区间。期间统计使用所选日期，累计与状态类报表按各自业务条件汇总。 部分报表按标题限定商户、订单状态或履约范围。

## 代码结构

```text
database/                         数据库结构与初始数据
scripts/                          初始化、启动与停止脚本
src/main/java/com/example/commerce/
  business/                       订单、商品、库存、采购与售后
  report/                         报表目录与查询服务
src/main/resources/
  mapper/                         MyBatis XML Mapper
  reports.json                    业务报表配置
  static/                         随应用发布的前端资源
src/test/java/                    集成测试
```

## 接口

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| GET | `/api/health` | 应用状态 |
| GET | `/api/dashboard` | 经营概览 |
| GET | `/api/options` | 仓库、品类等选项 |
| GET / POST | `/api/orders` | 查询、新建订单 |
| GET | `/api/orders/{id}` | 订单详情 |
| GET | `/api/products` | 商品目录 |
| GET | `/api/products/{id}/skus` | 商品规格 |
| GET | `/api/inventory` | 库存查询 |
| GET | `/api/purchases` | 采购列表 |
| GET | `/api/purchases/{id}` | 采购详情 |
| GET | `/api/returns` | 售后列表 |
| GET | `/api/returns/{id}` | 售后详情 |
| GET | `/api/reports` | 报表目录 |
| GET | `/api/reports/{code}?from=2025-01-01&until=2026-01-01` | 报表结果 |

## 测试

完成数据库初始化后运行 `mvn test`。集成测试覆盖报表配置、业务读取、前端资源、订单创建、库存预留与事务回滚。
