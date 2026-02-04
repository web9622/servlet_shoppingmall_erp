package com.company1.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import com.company1.DBManager;
import com.company1.dto.MonthlySale;
import com.company1.dto.MonthlyCancel;
import com.company1.dto.MonthlyRanking;

@WebServlet("/ReportingServlet")
public class ReportingServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<MonthlySale> monthlySales = new ArrayList<>();
        List<MonthlyCancel> monthlyCancels = new ArrayList<>();
        List<MonthlyRanking> monthlyRankings = new ArrayList<>();

        String saleSql = "SELECT TO_CHAR(o.order_date,'YYYY-MM') AS month, " +
                "       SUM(oi.quantity) AS total_quantity, " +
                "       SUM(oi.quantity * oi.unit_price) AS total_sales " +
                "FROM orders o " +
                "JOIN order_items oi ON o.oid = oi.order_id " +
                "LEFT JOIN order_cancellations oc ON oc.order_id = o.oid " +
                "WHERE oc.order_id IS NULL " + // ← 취소된 주문 제외
                "GROUP BY TO_CHAR(o.order_date, 'YYYY-MM') " +
                "ORDER BY month";

        // 취소 통계 쿼리
        String cancelSql = "SELECT TO_CHAR(cancel_date, 'YYYY-MM') AS cancel_month, " +
                "COUNT(*) AS cancel_count " +
                "FROM order_cancellations " +
                "GROUP BY TO_CHAR(cancel_date, 'YYYY-MM') " +
                "ORDER BY cancel_month";

        // 월별 상품의 판매금액 합계액이 TOP5
        String rankingSql = "SELECT TO_CHAR(o.order_date, 'YYYY-MM') AS month, " +
                "       p.pname AS product_name, " +
                "       SUM(oi.quantity * oi.unit_price) AS total_sales " +
                "FROM orders o " +
                "JOIN order_items oi ON o.oid = oi.order_id " +
                "JOIN products p ON oi.product_id = p.pid " +
                "LEFT JOIN order_cancellations oc ON oc.order_id = o.oid " +
                "WHERE oc.order_id IS NULL " +
                "GROUP BY TO_CHAR(o.order_date, 'YYYY-MM'), p.pname " +
                "ORDER BY month DESC, total_sales DESC " +
                "LIMIT 5";

        try (Connection conn = DBManager.getDBConnection()) {
            // 판매 통계 조회
            try (PreparedStatement pstmt = conn.prepareStatement(saleSql);
                    ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    MonthlySale sale = new MonthlySale(
                            rs.getString("month"),
                            rs.getInt("total_quantity"),
                            rs.getDouble("total_sales"));
                    monthlySales.add(sale);
                }
            }

            // 주문취소 통계 조회
            try (PreparedStatement pstmt = conn.prepareStatement(cancelSql);
                    ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    MonthlyCancel cancel = new MonthlyCancel(
                            rs.getString("cancel_month"),
                            rs.getInt("cancel_count"));
                    monthlyCancels.add(cancel);
                }
            }
            // 월별 판매 랭킹 조회
            try (PreparedStatement pstmt = conn.prepareStatement(rankingSql);
                    ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MonthlyRanking ranking = new MonthlyRanking( // 자바객체에 담음
                            rs.getString("month"),
                            rs.getString("product_name"),
                            rs.getDouble("total_sales"));

                    // 로그를 추가하여 객체에 제대로 데이터가 담겼는지 확인
                    System.out.println("Month: " + ranking.getMonth());
                    System.out.println("Product Name: " + ranking.getProductName());
                    System.out.println("Total Sales: " + ranking.getTotalSales());

                    monthlyRankings.add(ranking);// 서블릿리스트객체는 java객체에 담은것을 리스트에 추가함
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 통계 데이터 JSP로 전달
        request.setAttribute("monthlySales", monthlySales);
        request.setAttribute("monthlyCancels", monthlyCancels);
        request.setAttribute("monthlyRankings", monthlyRankings);
        request.getRequestDispatcher("reporting.jsp").forward(request, response);
    }
}
