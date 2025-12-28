# StressTest_Data - 压测数据处理平台

## 📋 项目简介

电商压测数据处理平台，提供测试数据生成、管理和导出能力。支持高并发数据处理、异步任务执行、QPS限流控制。

## 🛠 技术栈

- **Spring Boot** 2.3.4 + **MyBatis** 2.1.4
- **MySQL** 8.0+ + **Redis**
- **EasyExcel** 3.2.1 + **Hutool** 5.6.6 + **Guava** 30.1
- **JDK** 1.8 + **Maven** 3.6+

## 📦 功能模块

- **用户管理** - 用户CRUD、PPU生成、批量填充
- **批次管理** - 批次创建、状态跟踪、统计分析
- **订单管理** - 订单记录、批量操作、数据导出
- **商品管理** - 商品查询、筛选、统计
- **红包管理** - 红包发放、QPS限流、统计分析
- **扩展服务** - 订单服务、购物车服务、商品筛选（Redis存储）

## 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+（可选）

### 启动步骤

1. **初始化数据库**
```bash
mysql -uroot -p stress_test_data < src/main/resources/sql/schema.sql
```

2. **配置数据库连接**（application-dev.yml）
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/stress_test_data
    username: root
    password: root123
  redis:
    host: localhost
    port: 6379
```

3. **启动应用**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

4. **访问地址**
```
http://localhost:8080/stress-test-data
```

## API接口

- **用户管理**: `/user/*` - 用户CRUD、批量填充、线程池状态
- **批次管理**: `/batch/*` - 批次创建、查询、统计
- **订单管理**: `/order/*` - 订单记录、批量取消、导出
- **商品管理**: `/product/*` - 商品查询、筛选
- **红包管理**: `/redEnvelope/*` - 红包发放、查询、统计
- **缓存监控**: `/cache/*` - Redis缓存监控、统计

## 项目结构

```
src/main/java/com/ecommerce/loadtest/
├── controller/          # 控制器层
├── service/            # 服务层
│   └── impl/          # 服务实现
├── dao/               # 数据访问层
├── entity/            # 实体类
├── dto/               # 数据传输对象
├── config/            # 配置类
├── utils/             # 工具类
└── exception/         # 异常处理
```

## 核心特性

- **异步处理** - ThreadPoolExecutor、ForkJoinPool并行处理
- **QPS限流** - 时间窗口限流算法（可配置）
- **混合存储** - MySQL持久化 + Redis缓存
- **数据导出** - EasyExcel异步导出

## 配置说明

### 应用配置（application.yml）
```yaml
server:
  port: 8080
  servlet:
    context-path: /stress-test-data

stresstest:
  processing:
    max-product-count: 300
    red-envelope-qps-limit: 300
    default-thread-pool-size: 10
  file:
    temp-dir: /tmp/stress-test-data
    retention-days: 7
```

## 注意事项

1. 数据库默认密码：`root123`，请根据实际情况修改
2. Redis为可选，如不使用可注释相关配置（扩展服务将不可用）
3. 临时文件默认保留7天，定期清理避免磁盘占用过大

## 开发说明

### 编译打包
```bash
mvn clean package -DskipTests
```

### 运行测试
```bash
mvn test
```

---

**项目状态**: ✅ 开发完成  
**版本**: v1.0.0  
**作者**: rakkaus
