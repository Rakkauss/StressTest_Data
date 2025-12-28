-- Stress Test Data 数据库初始化脚本
-- 电商压测数据处理平台

-- 创建数据库
CREATE DATABASE IF NOT EXISTS stress_test_data CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE stress_test_data;

-- 测试用户表
CREATE TABLE IF NOT EXISTS test_user (
    uid BIGINT PRIMARY KEY COMMENT '用户ID',
    user_status INT DEFAULT 0 COMMENT '用户状态：0-可用，1-不可用',
    user_type INT NOT NULL COMMENT '用户类型：1-平台A用户，2-平台B用户',
    address_id BIGINT COMMENT '地址ID',
    ppu VARCHAR(500) COMMENT '用户认证令牌',
    platform_b_uid BIGINT COMMENT '平台B用户ID（用于双平台映射）',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_type (user_type),
    INDEX idx_user_status (user_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试用户表';

-- 压测订单表
CREATE TABLE IF NOT EXISTS load_test_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    buyer_id BIGINT NOT NULL COMMENT '买家用户ID',
    current_status TINYINT COMMENT '当前订单状态',
    business_line_id BIGINT COMMENT '业务线ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_id (order_id),
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_business_line (business_line_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='压测订单表';

-- 测试批次表
CREATE TABLE IF NOT EXISTS test_batch (
    batch_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '批次ID',
    create_user VARCHAR(100) NOT NULL COMMENT '创建用户',
    create_time BIGINT COMMENT '创建时间戳',
    total_count BIGINT DEFAULT 0 COMMENT '计划总数',
    real_count BIGINT DEFAULT 0 COMMENT '实际执行数',
    batch_status INT DEFAULT 0 COMMENT '批次状态：0-新建，1-进行中，2-已完成',
    INDEX idx_create_user (create_user),
    INDEX idx_batch_status (batch_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试批次表';

-- 红包记录表
CREATE TABLE IF NOT EXISTS red_envelope_record (
    receive_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    uid BIGINT NOT NULL COMMENT '用户ID',
    red_envelope_id BIGINT NOT NULL COMMENT '红包ID',
    receive_time BIGINT COMMENT '领取时间戳',
    batch_id BIGINT COMMENT '批次ID',
    amount BIGINT COMMENT '红包金额（分）',
    status INT DEFAULT 1 COMMENT '红包状态：0-待领取，1-已领取，2-已过期',
    platform_type INT COMMENT '平台类型：1-平台A，2-平台B',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_uid (uid),
    INDEX idx_batch_id (batch_id),
    INDEX idx_red_envelope_id (red_envelope_id),
    INDEX idx_platform_type (platform_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='红包记录表';

-- 下载任务表
CREATE TABLE IF NOT EXISTS download_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    task_name VARCHAR(200) COMMENT '任务名称',
    file_name VARCHAR(200) COMMENT '文件名',
    file_url VARCHAR(500) COMMENT '文件地址',
    task_status INT DEFAULT 0 COMMENT '任务状态：0-处理中，1-成功，2-失败',
    total_count BIGINT DEFAULT 0 COMMENT '总记录数',
    processed_count BIGINT DEFAULT 0 COMMENT '已处理记录数',
    error_message TEXT COMMENT '错误信息',
    create_user VARCHAR(100) COMMENT '创建用户',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    complete_time TIMESTAMP NULL COMMENT '完成时间',
    INDEX idx_task_status (task_status),
    INDEX idx_create_user (create_user),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='下载任务表';

-- 商品信息表
CREATE TABLE IF NOT EXISTS product_info (
    product_id BIGINT PRIMARY KEY COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    category_id INT COMMENT '分类ID',
    category_name VARCHAR(100) COMMENT '分类名称',
    business_line_id INT COMMENT '业务线ID：1-平台A，2-平台B',
    price DECIMAL(10,2) COMMENT '价格',
    status INT DEFAULT 1 COMMENT '状态：1-正常，0-下架，2-售罄',
    stock_count INT DEFAULT 0 COMMENT '库存数量',
    description TEXT COMMENT '商品描述',
    image_url VARCHAR(500) COMMENT '商品图片URL',
    seller_id BIGINT COMMENT '卖家ID',
    seller_name VARCHAR(100) COMMENT '卖家名称',
    tags VARCHAR(500) COMMENT '商品标签（JSON格式）',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_business_line (business_line_id),
    INDEX idx_status (status),
    INDEX idx_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品信息表';
