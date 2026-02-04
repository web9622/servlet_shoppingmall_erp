// Source code is decompiled from a .class file using FernFlower decompiler.
package com.company1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DBManager {
   public DBManager() {
   }

   public static Connection getDBConnection() {
      Connection conn = null;

      // Render 등 배포 환경에서 설정한 환경 변수를 읽어옵니다.
      // 로컬 개발 환경에서도 환경 변수를 설정하여 사용할 수 있습니다.
      String url = System.getenv("DB_URL");
      String user = System.getenv("DB_USER");
      String password = System.getenv("DB_PASSWORD");

      // 환경 변수가 없는 경우 기본값(로컬 개발 환경 설정)을 사용합니다.
      if (url == null)
         url = "jdbc:postgresql://dpg-d5vip9coud1c738im700-a.virginia-postgres.render.com/t3_erp";
      if (user == null)
         user = "t3_erp_user";
      if (password == null)
         password = "3N7wjv9oE9EZb0gzWiAn8Q2NeySFJTl8";

      // Render가 주는 postgres:// 또는 postgresql:// 형식을 JDBC 표준인 jdbc:postgresql:// 로
      // 변환합니다.
      if (url != null) {
         if (url.startsWith("postgres://")) {
            url = "jdbc:postgresql://" + url.substring("postgres://".length());
         } else if (url.startsWith("postgresql://")) {
            url = "jdbc:postgresql://" + url.substring("postgresql://".length());
         } else if (url.startsWith("jdbc:postgres://")) {
            url = "jdbc:postgresql://" + url.substring("jdbc:postgres://".length());
         }

         // 만약 URL에 @(골뱅이)가 들어있다면 (유저:비번@호스트 형식), 정보를 분리합니다.
         if (url.contains("@")) {
            String prefix = "jdbc:postgresql://";
            if (url.startsWith(prefix)) {
               String rest = url.substring(prefix.length());
               int atIndex = rest.lastIndexOf("@");
               if (atIndex != -1) {
                  String credentials = rest.substring(0, atIndex);
                  String hostPart = rest.substring(atIndex + 1);

                  // URL에서 호스트 부분만 남김 (PG 드라이버 요구사항)
                  url = prefix + hostPart;

                  // 환경변수가 비어있을 경우 URL에서 추출한 계정 정보 사용
                  if (credentials.contains(":")) {
                     String[] parts = credentials.split(":", 2);
                     if (user == null || user.isEmpty())
                        user = parts[0];
                     if (password == null || password.isEmpty())
                        password = parts[1];
                  }
               }
            }
         }
      }

      try {
         Class.forName("org.postgresql.Driver");
         conn = DriverManager.getConnection(url, user, password);

         // 테이블 자동 생성 (스키마 초기화)
         initializeSchema(conn);

      } catch (Exception var2) {
         var2.printStackTrace();
      }

      return conn;
   }

   /**
    * PostgreSQL에 필요한 테이블이 없을 경우 자동으로 생성합니다.
    */
   private static boolean isSchemaInitialized = false;

   private static synchronized void initializeSchema(Connection conn) {
      if (isSchemaInitialized)
         return;

      String[] ddls = {
            "CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, userid VARCHAR(50) NOT NULL UNIQUE, userpw VARCHAR(50) NOT NULL, username VARCHAR(50) NOT NULL)",
            "CREATE TABLE IF NOT EXISTS employees (emp_id VARCHAR(20) PRIMARY KEY, emp_pw VARCHAR(50) NOT NULL, emp_name VARCHAR(50) NOT NULL, position VARCHAR(50), email VARCHAR(70) NOT NULL, auth VARCHAR(10) DEFAULT 'user' NOT NULL)",
            "CREATE TABLE IF NOT EXISTS products (pid SERIAL PRIMARY KEY, pname VARCHAR(100) NOT NULL, price DECIMAL(12, 2) NOT NULL, stock INTEGER NOT NULL)",
            "CREATE TABLE IF NOT EXISTS customers (cid SERIAL PRIMARY KEY, id INTEGER NOT NULL, cname VARCHAR(100) NOT NULL, email VARCHAR(100), phone VARCHAR(100), CONSTRAINT fk_customers_users FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS orders (oid SERIAL PRIMARY KEY, cid INTEGER NOT NULL, order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, CONSTRAINT fk_orders_customers FOREIGN KEY (cid) REFERENCES customers(cid) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS order_items (item_id SERIAL PRIMARY KEY, order_id INTEGER NOT NULL, product_id INTEGER NOT NULL, quantity INTEGER NOT NULL, unit_price DECIMAL(12, 2) NOT NULL, CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES orders(oid) ON DELETE CASCADE, CONSTRAINT fk_oi_product FOREIGN KEY (product_id) REFERENCES products(pid) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS order_cancellations (cancel_id SERIAL PRIMARY KEY, order_id INTEGER NOT NULL, cancel_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, reason VARCHAR(255), CONSTRAINT fk_oc_order FOREIGN KEY (order_id) REFERENCES orders(oid) ON DELETE CASCADE)"
      };

      try (java.sql.Statement stmt = conn.createStatement()) {
         for (String ddl : ddls) {
            stmt.execute(ddl);
         }
         isSchemaInitialized = true;
         System.out.println("Database schema initialized successfully.");
      } catch (Exception e) {
         System.err.println("Error initializing database schema: " + e.getMessage());
         e.printStackTrace();
      }
   }

   /**
    * DB 연결을 종료하는 메서드입니다.
    * ResultSet, PreparedStatement, Connection 객체를 순서대로 닫습니다.
    * 
    * @param rs    ResultSet 객체
    * @param pstmt PreparedStatement 객체
    * @param conn  Connection 객체
    */
   public static void close(ResultSet rs, PreparedStatement pstmt, Connection conn) {
      try {
         if (rs != null)
            rs.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
      try {
         if (pstmt != null)
            pstmt.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
      try {
         if (conn != null)
            conn.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}