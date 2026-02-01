package com.company1.dao;

import com.company1.DBManager; // 우리가 만든 DBManager
import com.company1.dto.EmployeeDTO; // 직원 바구니
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// 데이터베이스에 접속해 직원관련 SQL(추가, 조회, 수정, 삭제)을 실행하는 역할 담당 [ 쉽게 표현하면 해결사 ]
public class EmployeeDAO {

    /**
     * 로그인 처리를 위해 아이디와 비밀번호가 일치하는 사용자가 있는지 확인하는 메서드
     * 
     * @param empId 사용자 아이디
     * @param empPw 사용자 비밀번호
     * @return 로그인 성공 시 EmployeeDTO 객체, 실패 시 null
     */
    public EmployeeDTO checkLogin(String empId, String empPw) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        EmployeeDTO emp = null;

        System.out.println("[Debug] Starting login check for ID: " + empId);

        String sql = "SELECT emp_id, emp_name, auth, email, position FROM employees WHERE emp_id = ? AND emp_pw = ?";

        try {
            conn = DBManager.getDBConnection();
            if (conn == null) {
                System.out.println("[Debug] FATAL: Database connection is NULL!");
                return null;
            }
            System.out.println("[Debug] DB connected successfully.");

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, empId);
            pstmt.setString(2, empPw);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                emp = new EmployeeDTO();
                emp.setEmpId(rs.getString("emp_id"));
                emp.setEmpName(rs.getString("emp_name"));
                emp.setAuth(rs.getString("auth"));
                emp.setEmail(rs.getString("email"));
                emp.setPosition(rs.getString("position"));
                System.out.println("[Debug] Login SUCCESS for: " + emp.getEmpName());
            } else {
                System.out.println("[Debug] Login FAILED: No matching record found for ID: " + empId
                        + " with the provided password.");

                // 디버깅: 해당 아이디가 존재는 하는지 확인
                String checkIdSql = "SELECT count(*) FROM employees WHERE emp_id = ?";
                try (PreparedStatement checkPstmt = conn.prepareStatement(checkIdSql)) {
                    checkPstmt.setString(1, empId);
                    try (ResultSet checkRs = checkPstmt.executeQuery()) {
                        if (checkRs.next()) {
                            System.out.println("[Debug] ID exists check: Count = " + checkRs.getInt(1));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[Debug] Exception during login: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBManager.close(rs, pstmt, conn);
        }
        return emp;
    }

    /**
     * 모든 직원 목록을 조회하는 메서드 (기존 selectAllEmployees 유지)
     * 
     * @return 직원 목록 (List<EmployeeDTO>)
     */
    public List<EmployeeDTO> selectAllEmployees() {
        List<EmployeeDTO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM employees ORDER BY emp_id ASC";

        try {
            conn = DBManager.getDBConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                EmployeeDTO emp = new EmployeeDTO();
                emp.setEmpId(rs.getString("emp_id"));
                emp.setEmpName(rs.getString("emp_name"));
                emp.setPosition(rs.getString("position"));
                emp.setEmail(rs.getString("email")); // ✨ 추가된 부분
                emp.setAuth(rs.getString("auth"));

                list.add(emp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(rs, pstmt, conn);
        }
        return list;
    }

    /**
     * 그룹웨어 수신자 목록용: 모든 직원의 기본 정보를 조회 (emp_id, emp_name, position, auth)
     */
    public List<EmployeeDTO> getAllEmployees() {
        List<EmployeeDTO> employees = new ArrayList<>();
        // ✨ 수정된 부분: email도 함께 조회
        String sql = "SELECT emp_id, emp_name, position, email, auth FROM employees ORDER BY emp_name";
        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                EmployeeDTO emp = new EmployeeDTO();
                emp.setEmpId(rs.getString("emp_id"));
                emp.setEmpName(rs.getString("emp_name"));
                emp.setPosition(rs.getString("position"));
                emp.setEmail(rs.getString("email")); // ✨ 추가된 부분
                emp.setAuth(rs.getString("auth"));
                employees.add(emp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    /**
     * 직원 아이디(empId)를 이용해 특정 직원 한 명의 모든 정보를 조회하는 메소드
     * 
     * @param empId 조회할 직원의 아이디
     * @return 직원 정보가 담긴 EmployeeDTO 객체, 해당 직원이 없으면 null
     */
    public EmployeeDTO selectEmployeeById(String empId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        EmployeeDTO emp = null;

        String sql = "SELECT * FROM employees WHERE emp_id = ?";

        try {
            conn = DBManager.getDBConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, empId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                emp = new EmployeeDTO();
                emp.setEmpId(rs.getString("emp_id"));
                emp.setEmpPw(rs.getString("emp_pw"));
                emp.setEmpName(rs.getString("emp_name"));
                emp.setPosition(rs.getString("position"));
                emp.setEmail(rs.getString("email")); // ✨ 추가된 부분
                emp.setAuth(rs.getString("auth"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(rs, pstmt, conn);
        }
        return emp;
    }

    /**
     * 새로운 직원을 등록하는 메서드
     * 
     * @param empDto 등록할 직원의 정보가 담긴 DTO
     */
    public void insertEmployee(EmployeeDTO empDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        // ✨ 수정된 부분: email 컬럼 추가
        String sql = "INSERT INTO employees (emp_id, emp_pw, emp_name, position, email, auth) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            conn = DBManager.getDBConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, empDTO.getEmpId());
            pstmt.setString(2, empDTO.getEmpPw());
            pstmt.setString(3, empDTO.getEmpName());
            pstmt.setString(4, empDTO.getPosition());
            pstmt.setString(5, empDTO.getEmail()); // ✨ 추가된 부분
            pstmt.setString(6, empDTO.getAuth()); // ✨ 인덱스 번호 변경
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(null, pstmt, conn);
        }
    }

    /**
     * 직원의 정보를 수정하는 메소드
     * 
     * @param empDTO 수정할 정보가 담긴 DTO 객체
     * @return 수정 성공 시 1 이상의 값, 실패 시 0
     */
    public int updateEmployee(EmployeeDTO empDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int result = 0;

        String sql;
        boolean isPasswordChanged = empDTO.getEmpPw() != null && !empDTO.getEmpPw().isEmpty();

        if (isPasswordChanged) {
            // ✨ 수정된 부분: email 컬럼 추가
            sql = "UPDATE employees SET emp_pw=?, emp_name=?, position=?, email=?, auth=? WHERE emp_id=?";
        } else {
            // ✨ 수정된 부분: email 컬럼 추가
            sql = "UPDATE employees SET emp_name=?, position=?, email=?, auth=? WHERE emp_id=?";
        }

        try {
            conn = DBManager.getDBConnection();
            pstmt = conn.prepareStatement(sql);

            if (isPasswordChanged) {
                pstmt.setString(1, empDTO.getEmpPw());
                pstmt.setString(2, empDTO.getEmpName());
                pstmt.setString(3, empDTO.getPosition());
                pstmt.setString(4, empDTO.getEmail()); // ✨ 추가된 부분
                pstmt.setString(5, empDTO.getAuth()); // ✨ 인덱스 번호 변경
                pstmt.setString(6, empDTO.getEmpId()); // ✨ 인덱스 번호 변경
            } else {
                pstmt.setString(1, empDTO.getEmpName());
                pstmt.setString(2, empDTO.getPosition());
                pstmt.setString(3, empDTO.getEmail()); // ✨ 추가된 부분
                pstmt.setString(4, empDTO.getAuth()); // ✨ 인덱스 번호 변경
                pstmt.setString(5, empDTO.getEmpId()); // ✨ 인덱스 번호 변경
            }

            result = pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(null, pstmt, conn);
        }
        return result;
    }

    /**
     * 직원 아이디(empId)를 이용해 특정 직원을 삭제하는 메서드
     * 
     * @param empId 삭제할 직원의 아이디
     * @return 삭제 성공 시 1 이상의 값, 실패 시 0
     */
    public int deleteEmployee(String empId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int result = 0;
        String sql = "DELETE FROM employees WHERE emp_id = ?";

        try {
            conn = DBManager.getDBConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, empId);
            result = pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(null, pstmt, conn);
        }
        return result;
    }

    /**
     * 이름과 이메일로 아이디 찾기
     */
    public String findIdByNameAndEmail(String name, String email) {
        String foundId = null;
        String sql = "SELECT emp_id FROM employees WHERE emp_name = ? AND email = ?";

        System.out.println("=== DAO 아이디 찾기 디버깅 시작 ===");
        System.out.println("검색할 이름: [" + name + "]");
        System.out.println("검색할 이메일: [" + email + "]");
        System.out.println("실행할 SQL: " + sql);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBManager.getDBConnection();

            // DB 연결 확인
            if (conn == null) {
                System.out.println("❌ 데이터베이스 연결 실패!");
                return null;
            }
            System.out.println("✅ 데이터베이스 연결 성공");

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                foundId = rs.getString("emp_id");
                System.out.println("✅ DB에서 찾은 아이디: [" + foundId + "]");
            } else {
                System.out.println("❌ DB에서 일치하는 데이터를 찾지 못했습니다.");

                // 디버깅을 위해 전체 employees 테이블 확인
                String debugSql = "SELECT emp_id, emp_name, email FROM employees";
                try (PreparedStatement debugPstmt = conn.prepareStatement(debugSql);
                        ResultSet debugRs = debugPstmt.executeQuery()) {
                    System.out.println("--- 현재 DB의 전체 직원 데이터 ---");
                    while (debugRs.next()) {
                        System.out.println("아이디: [" + debugRs.getString("emp_id") +
                                "], 이름: [" + debugRs.getString("emp_name") +
                                "], 이메일: [" + debugRs.getString("email") + "]");
                    }
                    System.out.println("--- 전체 직원 데이터 끝 ---");
                } catch (SQLException debugE) {
                    System.out.println("❌ 디버그 쿼리 실행 중 오류: " + debugE.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("❌ SQL 실행 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBManager.close(rs, pstmt, conn);
        }

        System.out.println("최종 반환할 아이디: [" + foundId + "]");
        System.out.println("=== DAO 아이디 찾기 디버깅 끝 ===");

        return foundId;
    }

    // ================== 여기부터 다시 추가(한울) ==================

    /**
     * 아이디와 이메일로 비밀번호를 찾는 메서드 (새로 추가)
     * 
     * @param empId    사용자 아이디
     * @param empEmail 사용자 이메일
     * @return 찾은 비밀번호, 없으면 null
     */
    public String findPassword(String empId, String empEmail) {
        // ✨ 수정 포인트 1: 테이블 이름을 employees 로 변경!
        // ✨ 수정 포인트 2: 이메일 컬럼명을 email 로 변경!
        String sql = "SELECT emp_pw FROM employees WHERE emp_id = ? AND email = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String password = null;

        try {
            // 학생의 코드에 있는 DBManager를 그대로 사용합니다.
            conn = DBManager.getDBConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, empId);
            pstmt.setString(2, empEmail);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                // 비밀번호 컬럼명(emp_pw)은 학생 코드와 동일하네요!
                password = rs.getString("emp_pw");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 사용한 리소스는 반드시 닫아줍니다.
            DBManager.close(rs, pstmt, conn);
        }

        return password; // 비밀번호를 찾았으면 비밀번호 문자열을, 못 찾았으면 null을 반환합니다.
    }

    /**
     * 아이디와 이메일로 사용자 확인 (비밀번호 찾기용)
     * 
     * @param empId 사용자 아이디
     * @param email 사용자 이메일
     * @return 사용자가 존재하면 true, 없으면 false
     */
    public boolean checkUserByIdAndEmail(String empId, String email) {
        String sql = "SELECT emp_id FROM employees WHERE emp_id = ? AND email = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBManager.getDBConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, empId);
            pstmt.setString(2, email);
            rs = pstmt.executeQuery();

            return rs.next(); // 결과가 있으면 true, 없으면 false

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            DBManager.close(rs, pstmt, conn);
        }
    }

    /**
     * 비밀번호 재설정
     * 
     * @param empId       사용자 아이디
     * @param newPassword 새 비밀번호
     * @return 성공하면 true, 실패하면 false
     */
    public boolean resetPassword(String empId, String newPassword) {
        String sql = "UPDATE employees SET emp_pw = ? WHERE emp_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBManager.getDBConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPassword);
            pstmt.setString(2, empId);

            int result = pstmt.executeUpdate();
            return result > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            DBManager.close(null, pstmt, conn);
        }
    }
}