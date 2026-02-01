package com.company1.dao;

import com.company1.dto.ProductDTO;
import com.company1.DBManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // 상품 추가
    public void addProduct(ProductDTO product) throws SQLException {
        String sql = "INSERT INTO PRODUCTS (PNAME, PRICE, STOCK) VALUES (?, ?, ?)";
        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getPname());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getStock());
            pstmt.executeUpdate();
        }
    }

    // 모든 상품 조회
    public List<ProductDTO> getAllProducts() throws SQLException {
        List<ProductDTO> products = new ArrayList<>(); // list가 productDTO 타입의 객체를 담을 수 있도록 변경
        String sql = "SELECT * FROM PRODUCTS ORDER BY PID";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ProductDTO product = new ProductDTO();
                product.setPid(rs.getInt("PID"));
                product.setPname(rs.getString("PNAME"));
                product.setPrice(rs.getDouble("PRICE"));
                product.setStock(rs.getInt("STOCK"));
                products.add(product);
            }
        }
        return products;
    }

    // 상품 ID로 조회
    public ProductDTO getProductById(int pid) throws SQLException {
        ProductDTO product = null;
        String sql = "SELECT * FROM PRODUCTS WHERE PID = ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pid);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    product = new ProductDTO();
                    product.setPid(rs.getInt("PID"));
                    product.setPname(rs.getString("PNAME"));
                    product.setPrice(rs.getDouble("PRICE"));
                    product.setStock(rs.getInt("STOCK"));
                }
            }
        }
        return product;
    }

    // 상품 수정
    public void updateProduct(ProductDTO product) throws SQLException {
        String sql = "UPDATE PRODUCTS SET PNAME = ?, PRICE = ?, STOCK = ? WHERE PID = ?";
        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getPname());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getStock());
            pstmt.setInt(4, product.getPid());
            pstmt.executeUpdate();
        }
    }

    // 상품 삭제
    public void deleteProduct(int pid) throws SQLException {
        String sql = "DELETE FROM PRODUCTS WHERE PID = ?";
        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pid);
            pstmt.executeUpdate();
        }
    }

    // 페이징 처리된 상품 목록 조회
    public List<ProductDTO> getAllProducts(int offset, int limit) throws SQLException {
        List<ProductDTO> products = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCTS ORDER BY PID DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ProductDTO product = new ProductDTO();
                    product.setPid(rs.getInt("PID"));
                    product.setPname(rs.getString("PNAME"));
                    product.setPrice(rs.getDouble("PRICE"));
                    product.setStock(rs.getInt("STOCK"));
                    products.add(product);
                }
            }
        }
        return products;
    }

    // 페이징 처리된 검색 결과 조회
    public List<ProductDTO> searchProducts(String keyword, int offset, int limit) throws SQLException {
        List<ProductDTO> products = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCTS " +
                "WHERE UPPER(PNAME) LIKE UPPER(?) " +
                "ORDER BY PID DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + escapeLike(keyword) + "%");
            pstmt.setInt(2, limit);
            pstmt.setInt(3, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ProductDTO product = new ProductDTO();
                    product.setPid(rs.getInt("PID"));
                    product.setPname(rs.getString("PNAME"));
                    product.setPrice(rs.getDouble("PRICE"));
                    product.setStock(rs.getInt("STOCK"));
                    products.add(product);
                }
            }
        }
        return products;
    }

    // 전체 레코드 수 조회
    public int getNumberOfRecords() throws SQLException {
        String sql = "SELECT COUNT(*) FROM PRODUCTS";
        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // 검색 결과의 전체 레코드 수 조회
    public int getNumberOfRecords(String keyword) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PRODUCTS WHERE UPPER(PNAME) LIKE UPPER(?)";
        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + escapeLike(keyword) + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // LIKE 검색을 위한 특수문자 이스케이프 처리
    private String escapeLike(String keyword) {
        if (keyword == null)
            return "";
        return keyword.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    // 상품 상세 정보 조회
    public ProductDTO getProductDetails(int pid) throws SQLException {
        ProductDTO product = null;
        String sql = "SELECT PID, PNAME, PRICE, STOCK FROM PRODUCTS WHERE PID = ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pid);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    product = new ProductDTO();
                    product.setPid(rs.getInt("PID"));
                    product.setPname(rs.getString("PNAME"));
                    product.setPrice(rs.getDouble("PRICE"));
                    product.setStock(rs.getInt("STOCK"));
                }
            }
        }
        return product;
    }
}