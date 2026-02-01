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
         url = "jdbc:oracle:thin:@localhost:1521/orcl";
      if (user == null)
         user = "project2_2504_team3";
      if (password == null)
         password = "1234";

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