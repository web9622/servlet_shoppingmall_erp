package com.company1.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.company1.DBManager;
import com.company1.dto.OrderDTO;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OrderServlet extends HttpServlet {

    // GET
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null)
            action = "list";

        switch (action) {
            case "list":
                listOrders(request, response);
                break;
            case "delete":
                deleteOrder(request, response);
                break;
            default:
                listOrders(request, response);
        }
    }

    // POST
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        switch (action) {
            case "insert":
                insertOrder(request, response);
                break;
            default:
                doGet(request, response);
        }
    }

    // 주문 목록 조회
    private void listOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<OrderDTO> orderList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBManager.getDBConnection();
            String sql = "SELECT o.oid, c.cname, p.pname, oi.quantity, oi.unit_price, o.order_date "
                    + "FROM orders o "
                    + "JOIN customers c ON o.cid = c.cid "
                    + "JOIN order_items oi ON o.oid = oi.order_id "
                    + "JOIN products p ON oi.product_id = p.pid "
                    + "ORDER BY o.order_date DESC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                OrderDTO order = new OrderDTO(
                        rs.getInt("oid"),
                        rs.getString("cname"),
                        rs.getString("pname"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getTimestamp("order_date"));
                orderList.add(order);
            }
            rs.close();
            pstmt.close();

            // 추가 통계 데이터 조회 (JSP의 컴파일 오류를 방지하기 위해 서블릿에서 처리)
            int totalOrders = 0;
            int monthOrders = 0;
            double totalSales = 0;

            // 1. 전체 주문 수
            pstmt = conn.prepareStatement("SELECT COUNT(*) AS cnt FROM orders");
            rs = pstmt.executeQuery();
            if (rs.next())
                totalOrders = rs.getInt("cnt");
            rs.close();
            pstmt.close();

            // 2. 이번 달 주문 수
            pstmt = conn.prepareStatement(
                    "SELECT COUNT(*) AS cnt FROM orders WHERE TO_CHAR(order_date, 'YYYYMM') = TO_CHAR(CURRENT_DATE, 'YYYYMM')");
            rs = pstmt.executeQuery();
            if (rs.next())
                monthOrders = rs.getInt("cnt");
            rs.close();
            pstmt.close();

            // 3. 총 매출
            pstmt = conn.prepareStatement("SELECT COALESCE(SUM(quantity * unit_price), 0) AS total FROM order_items");
            rs = pstmt.executeQuery();
            if (rs.next())
                totalSales = rs.getDouble("total");
            rs.close();
            pstmt.close();

            // 4. 고객 목록
            List<java.util.Map<String, Object>> customerList = new ArrayList<>();
            pstmt = conn.prepareStatement("SELECT cid, cname FROM customers ORDER BY cname ASC");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("cid", rs.getInt("cid"));
                map.put("cname", rs.getString("cname"));
                customerList.add(map);
            }
            rs.close();
            pstmt.close();

            // 5. 상품 목록
            List<java.util.Map<String, Object>> productList = new ArrayList<>();
            pstmt = conn.prepareStatement("SELECT pid, pname FROM products ORDER BY pname ASC");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("pid", rs.getInt("pid"));
                map.put("pname", rs.getString("pname"));
                productList.add(map);
            }
            rs.close();
            pstmt.close();

            request.setAttribute("orderList", orderList);
            request.setAttribute("totalOrders", totalOrders);
            request.setAttribute("monthOrders", monthOrders);
            request.setAttribute("totalSales", totalSales);
            request.setAttribute("customerList", customerList);
            request.setAttribute("productList", productList);
            RequestDispatcher rd = request.getRequestDispatcher("order_list.jsp");
            rd.forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("주문 목록 조회 중 오류 발생", e);
        } finally {
            DBManager.close(rs, pstmt, conn);
        }
    }

    private void deleteOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        PreparedStatement pstmt3 = null; // order_cancellations 삭제용

        try {
            int oid = Integer.parseInt(request.getParameter("oid"));
            conn = DBManager.getDBConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 1. order_cancellations 테이블에서 해당 주문의 취소 기록 삭제
            String cancelSql = "DELETE FROM order_cancellations WHERE order_id = ?";
            pstmt3 = conn.prepareStatement(cancelSql);
            pstmt3.setInt(1, oid);
            int rowsAffected1 = pstmt3.executeUpdate();
            System.out.println("삭제된 order_cancellations 개수: " + rowsAffected1);

            // 2. order_items 삭제
            String sql1 = "DELETE FROM order_items WHERE order_id = ?";
            pstmt1 = conn.prepareStatement(sql1);
            pstmt1.setInt(1, oid);
            int rowsAffected2 = pstmt1.executeUpdate();
            System.out.println("삭제된 order_items 개수: " + rowsAffected2);

            // 3. orders 테이블에서 주문 삭제
            String sql2 = "DELETE FROM orders WHERE oid = ?";
            pstmt2 = conn.prepareStatement(sql2);
            pstmt2.setInt(1, oid);
            int rowsAffected3 = pstmt2.executeUpdate();
            System.out.println("삭제된 orders 개수: " + rowsAffected3);

            // 커밋
            conn.commit();
            System.out.println("트랜잭션 커밋 완료");

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null)
                    conn.rollback(); // 오류 발생 시 롤백
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            DBManager.close(null, pstmt1, null);
            DBManager.close(null, pstmt2, null);
            DBManager.close(null, pstmt3, null);
            DBManager.close(null, null, conn);
        }

        // 삭제 후 주문 목록 페이지로 리다이렉트 (새로 고침)
        response.sendRedirect(request.getContextPath() + "/OrderServlet?action=list");
    }

    // 주문 등록
    private void insertOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            int productId = Integer.parseInt(request.getParameter("pid"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            int cid = Integer.parseInt(request.getParameter("cid"));

            conn = DBManager.getDBConnection();
            conn.setAutoCommit(false);

            // 가격과 재고 확인
            pstmt = conn.prepareStatement("SELECT price, stock FROM products WHERE pid=?");
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next())
                throw new Exception("상품 정보 없음");
            double unitPrice = rs.getDouble("price");
            int stock = rs.getInt("stock");
            rs.close();
            pstmt.close();

            if (stock < quantity)
                throw new Exception("재고 부족");

            // OID는 SERIAL로 자동 생성되므로 INSERT 문에서 제외
            // order_date는 CURRENT_TIMESTAMP로 자동 설정
            pstmt = conn.prepareStatement("INSERT INTO orders(cid, order_date) VALUES (?, CURRENT_TIMESTAMP)",
                    Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, cid);
            pstmt.executeUpdate();

            int newOrderId = 0;
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newOrderId = generatedKeys.getInt(1);
                }
            }
            pstmt.close();

            if (newOrderId == 0)
                throw new Exception("주문 번호 생성 실패");

            // order_items 테이블에 추가
            pstmt = conn.prepareStatement(
                    "INSERT INTO order_items(order_id, product_id, quantity, unit_price) "
                            + "VALUES(?, ?, ?, ? )");
            pstmt.setInt(1, newOrderId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            pstmt.setDouble(4, unitPrice);
            pstmt.executeUpdate();
            pstmt.close();

            // 재고 차감
            pstmt = conn.prepareStatement("UPDATE products SET stock = stock - ? WHERE pid = ?");
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);
            pstmt.executeUpdate();

            conn.commit();

            // 성공 후 리다이렉트
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('주문이 성공적으로 등록되었습니다.');");
            out.println("location.href='" + request.getContextPath() + "/OrderServlet?action=list';</script>");

        } catch (Exception e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();

            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('오류 발생: " + e.getMessage() + "'); history.back();</script>");
        } finally {
            DBManager.close(null, pstmt, conn);
        }
    }
}
