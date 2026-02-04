-- ======================================
-- Shoppingmall Company ERP - PostgreSQL DDL
-- ======================================

-- 1. USERS 테이블
CREATE TABLE IF NOT EXISTS users (
    id       SERIAL PRIMARY KEY,
    userid   VARCHAR(50) NOT NULL UNIQUE,
    userpw   VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL
);

-- 2. EMPLOYEES 테이블
CREATE TABLE IF NOT EXISTS employees (
    emp_id      VARCHAR(20) PRIMARY KEY,     
    emp_pw      VARCHAR(50) NOT NULL,        
    emp_name    VARCHAR(50) NOT NULL,        
    position    VARCHAR(50),                 
    email       VARCHAR(70) NOT NULL,        
    auth        VARCHAR(10) DEFAULT 'user' NOT NULL 
);

-- 3. PRODUCTS 테이블
CREATE TABLE IF NOT EXISTS products (
    pid     SERIAL PRIMARY KEY,             
    pname   VARCHAR(100) NOT NULL,           
    price   DECIMAL(12, 2) NOT NULL,            
    stock   INTEGER NOT NULL               
);

-- 4. CUSTOMERS 테이블
CREATE TABLE IF NOT EXISTS customers (
    cid     SERIAL PRIMARY KEY,             
    id      INTEGER NOT NULL,				        
    cname   VARCHAR(100) NOT NULL,           
    email   VARCHAR(100),                    
    phone   VARCHAR(100),					          
    CONSTRAINT fk_customers_users FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. ORDERS 테이블
CREATE TABLE IF NOT EXISTS orders (
    oid         SERIAL PRIMARY KEY,             
    cid         INTEGER NOT NULL,                 
    order_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_customers FOREIGN KEY (cid) REFERENCES customers(cid) ON DELETE CASCADE
);

-- 6. ORDER_ITEMS 테이블
CREATE TABLE IF NOT EXISTS order_items (
    item_id     SERIAL PRIMARY KEY,
    order_id    INTEGER NOT NULL,
    product_id  INTEGER NOT NULL,
    quantity    INTEGER NOT NULL,
    unit_price  DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES orders(oid) ON DELETE CASCADE,
    CONSTRAINT fk_oi_product FOREIGN KEY (product_id) REFERENCES products(pid) ON DELETE CASCADE
);

-- 7. ORDER_CANCELLATIONS 테이블
CREATE TABLE IF NOT EXISTS order_cancellations (
    cancel_id   SERIAL PRIMARY KEY,
    order_id    INTEGER NOT NULL,
    cancel_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason      VARCHAR(255),
    CONSTRAINT fk_oc_order FOREIGN KEY (order_id) REFERENCES orders(oid) ON DELETE CASCADE
);
