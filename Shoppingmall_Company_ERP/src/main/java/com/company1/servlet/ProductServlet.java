package com.company1.servlet;

import com.company1.DBManager;
import com.company1.dao.ProductDAO;
import com.company1.dto.ProductDTO;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@WebServlet(urlPatterns = { "/product", "/product/list", "/product/edit", "/product/delete" })
public class ProductServlet extends HttpServlet {

    private final ProductDAO productDAO = new ProductDAO();

    // Servlet이 처음 로드될 때 초기화합니다.
    public void init() {
        // ProductDAO 의존성 제거
    }

    // 메모리에 Servlet 객체가 생성되면 init() 메소드가 호출됩니다.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String action = request.getParameter("action");

        // action 파라미터가 없으면 요청 경로에서 action 유추
        if (action == null) {
            if (pathInfo == null || "/list".equals(pathInfo)) {
                action = "list";
            } else if ("/edit".equals(pathInfo)) {
                action = "edit";
            } else if ("/delete".equals(pathInfo)) {
                action = "delete";
            } else {
                action = "list"; // 기본값
            }
        }

        switch (action) {
            case "list":
            case "search":
                listOrSearchProducts(request, response);
                break;
            case "delete":
                deleteProduct(request, response);
                break;
            case "edit":
                editProductForm(request, response);
                break;
            case "details":
                getProductDetails(request, response);
                break;
            default:
                listOrSearchProducts(request, response);
                break;
        }
    }

    // 상품 목록 조회 및 검색 메소드
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        try {
            if ("insert".equals(action)) {
                insertProduct(request, response);
            } else if ("update".equals(action)) {
                updateProduct(request, response);
            } else {
                doGet(request, response);
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private void listOrSearchProducts(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        String search = req.getParameter("search");
        String pageParam = req.getParameter("page");

        // 페이징 파라미터 처리
        int currentPage = 1;
        int recordsPerPage = 10;

        if (pageParam != null) {
            try {
                currentPage = Integer.parseInt(pageParam);
            } catch (NumberFormatException e) {
                // 유효하지 않은 페이지 파라미터는 무시하고 1페이지로
            }
        }

        boolean hasKeyword = (search != null && !search.trim().isEmpty());
        int offset = (currentPage - 1) * recordsPerPage;

        try {
            List<ProductDTO> products;
            int totalRecords;

            if (hasKeyword) {
                products = productDAO.searchProducts(search.trim(), offset, recordsPerPage);
                totalRecords = productDAO.getNumberOfRecords(search.trim());
            } else {
                products = productDAO.getAllProducts(offset, recordsPerPage);
                totalRecords = productDAO.getNumberOfRecords();
            }

            int totalPages = (int) Math.ceil((double) totalRecords / recordsPerPage);

            // 결과를 request에 저장
            req.setAttribute("products", products);
            req.setAttribute("search", search);
            req.setAttribute("currentPage", currentPage);
            req.setAttribute("recordsPerPage", recordsPerPage);
            req.setAttribute("noOfRecords", totalRecords);
            req.setAttribute("noOfPages", totalPages);

            // 디버깅용 로그
            System.out.println("[Product] action=" + action +
                    ", keyword=" + search +
                    ", page=" + currentPage +
                    ", total=" + totalRecords);

        } catch (SQLException e) {
            throw new ServletException("Database error occurred", e);
        }

        req.getRequestDispatcher("/product_list.jsp").forward(req, resp);
    }

    // 위에 붙여둔 escapeLike 그대로 사용
    private String escapeLike(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private void insertProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            String pname = request.getParameter("pname");
            double price = Double.parseDouble(request.getParameter("price"));
            int stock = Integer.parseInt(request.getParameter("stock"));

            conn = DBManager.getDBConnection();
            String sql = "INSERT INTO products(pname, price, stock) VALUES(?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pname);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, stock);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(null, pstmt, conn);
        }

        listOrSearchProducts(request, response);
    }

    private void deleteProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            int pid = Integer.parseInt(request.getParameter("pid"));
            conn = DBManager.getDBConnection();
            String sql = "DELETE FROM products WHERE pid=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pid);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(null, pstmt, conn);
        }

        listOrSearchProducts(request, response);
    }

    // 상품 정보를 수정하는 메소드
    private void updateProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            int pid = Integer.parseInt(request.getParameter("pid"));
            String pname = request.getParameter("pname");
            double price = Double.parseDouble(request.getParameter("price"));
            int stock = Integer.parseInt(request.getParameter("stock"));

            conn = DBManager.getDBConnection();
            String sql = "UPDATE products SET pname=?, price=?, stock=? WHERE pid=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pname);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, stock);
            pstmt.setInt(4, pid);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(null, pstmt, conn);
        }

        listOrSearchProducts(request, response);
    }

    // 상품 수정 폼을 보여주는 메소드
    private void editProductForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int pid = Integer.parseInt(request.getParameter("pid"));
            ProductDTO product = productDAO.getProductById(pid);

            if (product != null) {
                request.setAttribute("product", product);
                request.getRequestDispatcher("/product_edit.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/product?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/product?action=list");
        }
    }

    private void getProductDetails(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pidParam = request.getParameter("pid");
        if (pidParam == null || pidParam.isEmpty()) {
            System.out.println("[ProductServlet] Missing or empty 'pid' parameter.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Missing or invalid product ID.\"}");
            return;
        }

        try {
            int pid = Integer.parseInt(pidParam);
            System.out.println("[ProductServlet] Fetching details for product ID: " + pid);
            ProductDTO product = productDAO.getProductById(pid);
            if (product != null) {
                System.out.println("[ProductServlet] Product found: " + product);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(String.format(
                        "{\"pname\":\"%s\",\"price\":%f,\"stock\":%d}",
                        product.getPname(), product.getPrice(), product.getStock()));
            } else {
                System.out.println("[ProductServlet] Product not found for ID: " + pid);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Product not found.\"}");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ProductServlet] Invalid 'pid' parameter: " + pidParam);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid product ID format.\"}");
        } catch (SQLException e) {
            System.out.println("[ProductServlet] SQL Exception: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }

}