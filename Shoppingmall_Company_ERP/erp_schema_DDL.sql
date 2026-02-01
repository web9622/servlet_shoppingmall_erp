-- ======================================
-- 0. 사용자 테이블 (USERS) - CUSTOMERS에서 참조
-- ======================================
CREATE TABLE USERS (
    ID NUMBER(10) PRIMARY KEY,
    USERID VARCHAR2(50) NOT NULL,
    USERPW VARCHAR2(50) NOT NULL,
    USERNAME VARCHAR2(50) NOT NULL
);

CREATE SEQUENCE USERS_ID_SEQ START WITH 1 INCREMENT BY 1;

-- ======================================
-- 1. 직원 테이블 (EMPLOYEES)
-- ======================================
CREATE TABLE employees (
    emp_id      VARCHAR2(20) PRIMARY KEY,     
    emp_pw      VARCHAR2(50) NOT NULL,        
    emp_name    VARCHAR2(50) NOT NULL,        
    position    VARCHAR2(50),                 
    email       varchar2(70) NOT NULL,        
    auth        VARCHAR2(10) DEFAULT 'user' NOT NULL 
);


-- ======================================
-- 2. 상품 테이블 (PRODUCTS)
-- ======================================
CREATE TABLE PRODUCTS (
    PID NUMBER(10) PRIMARY KEY,             -- 상품 고유번호 (PK, 자동 증가)
    PNAME VARCHAR2(100) NOT NULL,           -- 상품명
    PRICE NUMBER(10,2) NOT NULL,            -- 상품 가격 (소수점 2자리)
    STOCK NUMBER(10) NOT NULL               -- 재고 수량
);

CREATE SEQUENCE PRODUCTS_PID_SEQ START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER PRODUCTS_ON_INSERT
BEFORE INSERT ON PRODUCTS
FOR EACH ROW
BEGIN
    SELECT PRODUCTS_PID_SEQ.NEXTVAL INTO :NEW.PID FROM DUAL;
END;
/
-- :NEW.PID → 새로 INSERT될 행의 상품 ID에 시퀀스 값 대입


-- ======================================
-- 3. 고객 테이블 (CUSTOMERS)
-- ======================================
CREATE TABLE CUSTOMERS (
    CID NUMBER(10) PRIMARY KEY,             -- 고객 고유번호 (PK, 자동 증가)
		ID	NUMBER(10) NOT NULL,				        -- 사용자 교유번호 (FK)
    CNAME VARCHAR2(100) NOT NULL,           -- 고객 이름
    EMAIL VARCHAR2(100),                    -- 고객 이메일
    PHONE VARCHAR2(100),					          -- 고객 전화번호
    CONSTRAINT FK_USERS_ID FOREIGN KEY (ID) REFERENCES USERS(ID) ON DELETE CASCADE
);

CREATE SEQUENCE CUSTOMERS_CID_SEQ START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER CUSTOMERS_ON_INSERT
BEFORE INSERT ON CUSTOMERS
FOR EACH ROW
BEGIN
    SELECT CUSTOMERS_CID_SEQ.NEXTVAL INTO :NEW.CID FROM DUAL;
END;
/
-- :NEW.CID → 새로 INSERT될 고객 ID에 시퀀스 값 대입


-- ======================================
-- 4. 주문 테이블 (ORDERS)
-- ======================================
CREATE TABLE ORDERS (
    OID NUMBER(10) PRIMARY KEY,             -- 주문 고유번호 (PK, 자동 증가)
    CID NUMBER(10) NOT NULL,                 -- 주문한 고객 ID (FK → CUSTOMERS.CID)
    PID NUMBER(10) NOT NULL,                 -- 주문한 상품 ID (FK → PRODUCTS.PID)
    QUANTITY NUMBER(10) NOT NULL,            -- 주문 수량
    ORDER_DATE TIMESTAMP DEFAULT SYSTIMESTAMP, -- 주문일시 (기본: 현재 시간)
    -- FOREIGN KEY 설정 시:
     CONSTRAINT FK_ORDERS_CID FOREIGN KEY (CID) REFERENCES CUSTOMERS(CID) ON DELETE CASCADE,
     CONSTRAINT FK_ORDERS_PID FOREIGN KEY (PID) REFERENCES PRODUCTS(PID) ON DELETE CASCADE
);

CREATE SEQUENCE ORDERS_OID_SEQ START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER ORDERS_ON_INSERT
BEFORE INSERT ON ORDERS
FOR EACH ROW
BEGIN
    SELECT ORDERS_OID_SEQ.NEXTVAL INTO :NEW.OID FROM DUAL;
END;
/
-- :NEW.OID → 새로 INSERT될 주문 ID에 시퀀스 값 대입
