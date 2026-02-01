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
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      return conn;
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