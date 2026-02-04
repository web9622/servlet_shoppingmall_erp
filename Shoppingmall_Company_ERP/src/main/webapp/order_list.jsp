<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ page import="java.util.*, com.company1.dto.OrderDTO" %>
        <%@ page import="java.sql.*" %>

            <!DOCTYPE html>
            <html lang="ko">

            <head>
                <meta charset="UTF-8">
                <title>주문 관리 - Shoppingmall ERP</title>
                <link rel="stylesheet" href="<%= request.getContextPath() %>/css/common.css">
                <link rel="stylesheet" href="<%= request.getContextPath() %>/css/order.css">
            </head>

            <body>
                <%@ include file="common-jsp/header.jsp" %>

                    <% List<OrderDTO> orderList = (List<OrderDTO>) request.getAttribute("orderList");
                            Connection conn = com.company1.DBManager.getDBConnection();
                            int totalOrders = 0;
                            int monthOrders = 0;
                            double totalSales = 0;
                            List<Map<String, Object>> customerList = new ArrayList<>();
                                    List<Map<String, Object>> productList = new ArrayList<>();

                                            try {
                                            if (conn != null) {
                                            PreparedStatement stmt;
                                            ResultSet rs;

                                            String sqlTotal = "SELECT COUNT(*) AS cnt FROM orders";
                                            stmt = conn.prepareStatement(sqlTotal);
                                            rs = stmt.executeQuery();
                                            if (rs.next()) totalOrders = rs.getInt("cnt");
                                            rs.close(); stmt.close();

                                            String sqlMonth = "SELECT COUNT(*) AS cnt FROM orders WHERE " +
                                            "TO_CHAR(order_date, 'YYYYMM') = TO_CHAR(CURRENT_DATE, 'YYYYMM')";
                                            stmt = conn.prepareStatement(sqlMonth);
                                            rs = stmt.executeQuery();
                                            if (rs.next()) monthOrders = rs.getInt("cnt");
                                            rs.close(); stmt.close();

                                            String sqlSales = "SELECT COALESCE(SUM(quantity * unit_price), 0) AS total
                                            FROM order_items";
                                            stmt = conn.prepareStatement(sqlSales);
                                            rs = stmt.executeQuery();
                                            if (rs.next()) totalSales = rs.getDouble("total");
                                            rs.close(); stmt.close();

                                            String sqlCust = "SELECT cid, cname FROM customers ORDER BY cname ASC";
                                            stmt = conn.prepareStatement(sqlCust);
                                            rs = stmt.executeQuery();
                                            while (rs.next()) {
                                            Map<String, Object> map = new HashMap<>();
                                                    map.put("cid", rs.getInt("cid"));
                                                    map.put("cname", rs.getString("cname"));
                                                    customerList.add(map);
                                                    }
                                                    rs.close(); stmt.close();

                                                    String sqlProd = "SELECT pid, pname FROM products ORDER BY pname
                                                    ASC";
                                                    stmt = conn.prepareStatement(sqlProd);
                                                    rs = stmt.executeQuery();
                                                    while (rs.next()) {
                                                    Map<String, Object> map = new HashMap<>();
                                                            map.put("pid", rs.getInt("pid"));
                                                            map.put("pname", rs.getString("pname"));
                                                            productList.add(map);
                                                            }
                                                            rs.close(); stmt.close();
                                                            conn.close();
                                                            }
                                                            } catch (Exception e) {
                                                            e.printStackTrace();
                                                            }
                                                            %>

                                                            <div class="container">
                                                                <div class="stats order-stats">
                                                                    <div class="stat-item">
                                                                        <div class="stat-number">
                                                                            <%= totalOrders %>
                                                                        </div>
                                                                        <div class="stat-label">전체 주문 수</div>
                                                                    </div>
                                                                    <div class="stat-item">
                                                                        <div class="stat-number">
                                                                            <%= monthOrders %>
                                                                        </div>
                                                                        <div class="stat-label">이번 달 주문</div>
                                                                    </div>
                                                                    <div class="stat-item">
                                                                        <div class="stat-number">₩<%=
                                                                                String.format("%,.0f", totalSales) %>
                                                                        </div>
                                                                        <div class="stat-label">총 매출액</div>
                                                                    </div>
                                                                </div>

                                                                <div class="form-section order-form">
                                                                    <h2>🛒 신규 주문 등록</h2>
                                                                    <form
                                                                        action="<%= request.getContextPath() %>/OrderServlet"
                                                                        method="post"
                                                                        onsubmit="return confirm('주문을 등록하시겠습니까?');">
                                                                        <input type="hidden" name="action"
                                                                            value="insert" />
                                                                        <div class="order-form-row">
                                                                            <div class="form-group">
                                                                                <label>고객 선택</label>
                                                                                <select name="cid" required>
                                                                                    <option value="">고객을 선택하세요</option>
                                                                                    <% for (Map<String, Object> c :
                                                                                        customerList) { %>
                                                                                        <option value="<%= c.get(" cid")
                                                                                            %>"><%= c.get("cname") %>
                                                                                        </option>
                                                                                        <% } %>
                                                                                </select>
                                                                            </div>
                                                                            <div class="form-group">
                                                                                <label>상품 선택</label>
                                                                                <select name="pid" required>
                                                                                    <option value="">상품을 선택하세요</option>
                                                                                    <% for (Map<String, Object> p :
                                                                                        productList) { %>
                                                                                        <option value="<%= p.get(" pid")
                                                                                            %>"><%= p.get("pname") %>
                                                                                        </option>
                                                                                        <% } %>
                                                                                </select>
                                                                            </div>
                                                                            <div class="form-group">
                                                                                <label>수량</label>
                                                                                <input type="number" name="quantity"
                                                                                    min="1" value="1" required />
                                                                            </div>
                                                                            <div class="form-actions">
                                                                                <input type="submit" value="주문 생성" />
                                                                            </div>
                                                                        </div>
                                                                    </form>
                                                                </div>

                                                                <div class="list-section">
                                                                    <h2>📋 최근 주문 내역</h2>
                                                                    <table class="order-table">
                                                                        <thead>
                                                                            <tr>
                                                                                <th>No.</th>
                                                                                <th>고객명</th>
                                                                                <th>상품명</th>
                                                                                <th>수량</th>
                                                                                <th style="text-align: right;">단가</th>
                                                                                <th style="text-align: right;">합계</th>
                                                                                <th>주문일시</th>
                                                                                <th>관리</th>
                                                                            </tr>
                                                                        </thead>
                                                                        <tbody>
                                                                            <% if (orderList !=null &&
                                                                                !orderList.isEmpty()) { for (OrderDTO
                                                                                order : orderList) { %>
                                                                                <tr>
                                                                                    <td>#<%= order.getOid() %>
                                                                                    </td>
                                                                                    <td>
                                                                                        <%= order.getCustomerName() %>
                                                                                    </td>
                                                                                    <td>
                                                                                        <%= order.getProductName() %>
                                                                                    </td>
                                                                                    <td style="text-align: center;">
                                                                                        <%= order.getQuantity() %>
                                                                                    </td>
                                                                                    <td style="text-align: right;">₩<%=
                                                                                            String.format("%,.0f",
                                                                                            order.getUnitPrice()) %>
                                                                                    </td>
                                                                                    <td
                                                                                        style="text-align: right; font-weight: 700;">
                                                                                        ₩<%= String.format("%,.0f",
                                                                                            order.getQuantity() *
                                                                                            order.getUnitPrice()) %>
                                                                                    </td>
                                                                                    <td>
                                                                                        <%= order.getOrderDate() %>
                                                                                    </td>
                                                                                    <td>
                                                                                        <a href="<%= request.getContextPath() %>/OrderServlet?action=delete&oid=<%= order.getOid() %>"
                                                                                            class="btn-delete"
                                                                                            onclick="return confirm('삭제하시겠습니까?');">삭제</a>
                                                                                    </td>
                                                                                </tr>
                                                                                <% } } else { %>
                                                                                    <tr>
                                                                                        <td colspan="8" class="no-data">
                                                                                            주문 내역이 없습니다.</td>
                                                                                    </tr>
                                                                                    <% } %>
                                                                        </tbody>
                                                                    </table>
                                                                </div>
                                                            </div>
            </body>

            </html>