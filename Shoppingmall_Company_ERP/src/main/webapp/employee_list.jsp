<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ page import="java.util.List" %>
        <%@ page import="com.company1.dto.EmployeeDTO" %>

            <% // 서블릿에서 전달한 employeeList 속성을 List<EmployeeDTO> 타입으로 받습니다.
                List<EmployeeDTO> employeeList = (List<EmployeeDTO>) request.getAttribute("employeeList");

                        // ✨ 수정된 부분: 관리자/사용자 수 계산 로직 추가
                        int adminCount = 0;
                        int userCount = 0;
                        if (employeeList != null) {
                        for (EmployeeDTO emp : employeeList) {
                        if ("admin".equals(emp.getAuth())) {
                        adminCount++;
                        } else {
                        userCount++;
                        }
                        }
                        }
                        %>

                        <!DOCTYPE html>
                        <html lang="ko">

                        <head>
                            <meta charset="UTF-8">
                            <title>사용자 관리 - B2B Shoppingmall ERP</title>
                            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
                            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/employee.css">
                        </head>

                        <body>
                            <%@ include file="common-jsp/header.jsp" %>

                                <div class="container">
                                    <div class="hero-section"
                                        style="padding: 2.5rem 2rem; margin-bottom: 2.5rem; text-align: left; background: linear-gradient(135deg, var(--primary-color), var(--secondary-color)); border-radius: 1.5rem; color: white;">
                                        <h1 style="margin: 0; font-size: 2.2rem;">👥 사용자 관리 시스템</h1>
                                        <p style="margin: 0.5rem 0 0 0; opacity: 0.9;">전체 임직원의 계정 권한 및 직책 정보를 관리합니다.</p>
                                    </div>

                                    <div class="stats employee-stats"
                                        style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; margin-bottom: 3rem;">
                                        <div class="stat-item"
                                            style="background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(10px); padding: 2rem; border-radius: 1.2rem; border: 1px solid rgba(255,255,255,0.4); box-shadow: 0 8px 32px rgba(0,0,0,0.05); text-align: center;">
                                            <div class="stat-number"
                                                style="font-size: 2.5rem; font-weight: 800; color: var(--primary-color);">
                                                <%= employeeList !=null ? employeeList.size() : 0 %>
                                            </div>
                                            <div class="stat-label"
                                                style="font-size: 0.95rem; color: #666; font-weight: 500;">전체 임직원</div>
                                        </div>
                                        <div class="stat-item"
                                            style="background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(10px); padding: 2rem; border-radius: 1.2rem; border: 1px solid rgba(255,255,255,0.4); box-shadow: 0 8px 32px rgba(0,0,0,0.05); text-align: center;">
                                            <div class="stat-number"
                                                style="font-size: 2.5rem; font-weight: 800; color: var(--secondary-color);">
                                                <%= adminCount %>
                                            </div>
                                            <div class="stat-label"
                                                style="font-size: 0.95rem; color: #666; font-weight: 500;">관리자 계정</div>
                                        </div>
                                        <div class="stat-item"
                                            style="background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(10px); padding: 2rem; border-radius: 1.2rem; border: 1px solid rgba(255,255,255,0.4); box-shadow: 0 8px 32px rgba(0,0,0,0.05); text-align: center;">
                                            <div class="stat-number"
                                                style="font-size: 2.5rem; font-weight: 800; color: #444;">
                                                <%= userCount %>
                                            </div>
                                            <div class="stat-label"
                                                style="font-size: 0.95rem; color: #666; font-weight: 500;">일반 사용자</div>
                                        </div>
                                    </div>

                                    <div class="main-layout"
                                        style="display: grid; grid-template-columns: 350px 1fr; gap: 2.5rem; align-items: start;">
                                        <!-- 등록 폼 -->
                                        <div class="employee-form glass"
                                            style="background: white; padding: 2rem; border-radius: 1.5rem; box-shadow: 0 10px 40px rgba(0,0,0,0.04);">
                                            <h2
                                                style="margin-top: 0; font-size: 1.4rem; display: flex; align-items: center; gap: 0.5rem;">
                                                ✨ 신규 등록</h2>
                                            <form action="EmployeeServlet" method="post">
                                                <input type="hidden" name="action" value="insert">
                                                <div class="form-group" style="margin-bottom: 1.2rem;">
                                                    <label
                                                        style="display: block; margin-bottom: 0.5rem; font-size: 0.9rem; color: #555; font-weight: 600;">아이디</label>
                                                    <input type="text" name="empId" required placeholder="예: admin01"
                                                        style="width: 100%; border-radius: 0.8rem; border: 1px solid #ddd; padding: 0.8rem; font-size: 0.95rem;">
                                                </div>
                                                <div class="form-group" style="margin-bottom: 1.2rem;">
                                                    <label
                                                        style="display: block; margin-bottom: 0.5rem; font-size: 0.9rem; color: #555; font-weight: 600;">비밀번호</label>
                                                    <input type="password" name="empPw" required
                                                        style="width: 100%; border-radius: 0.8rem; border: 1px solid #ddd; padding: 0.8rem; font-size: 0.95rem;">
                                                </div>
                                                <div class="form-group" style="margin-bottom: 1.2rem;">
                                                    <label
                                                        style="display: block; margin-bottom: 0.5rem; font-size: 0.9rem; color: #555; font-weight: 600;">성함</label>
                                                    <input type="text" name="empName" required placeholder="성함을 입력하세요"
                                                        style="width: 100%; border-radius: 0.8rem; border: 1px solid #ddd; padding: 0.8rem; font-size: 0.95rem;">
                                                </div>
                                                <div class="form-group" style="margin-bottom: 1.2rem;">
                                                    <label
                                                        style="display: block; margin-bottom: 0.5rem; font-size: 0.9rem; color: #555; font-weight: 600;">직책</label>
                                                    <select name="position"
                                                        style="width: 100%; border-radius: 0.8rem; border: 1px solid #ddd; padding: 0.8rem; appearance: none; background: url('data:image/svg+xml;charset=US-ASCII,%3Csvg%20width%3D%2212%22%20height%3D%2212%22%20viewBox%3D%220%200%2012%2012%22%20fill%3D%22none%22%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%3E%3Cpath%20d%3D%22M2.5%204.5L6%208L9.5%204.5%22%20stroke%3D%22%23666%22%20stroke-width%3D%222%22%20stroke-linecap%3D%22round%22%20stroke-linejoin%3D%22round%22/%3E%3C/svg%3E') no-repeat right 1rem center; font-size: 0.95rem;">
                                                        <option value="사원">사원</option>
                                                        <option value="대리">대리</option>
                                                        <option value="과장">과장</option>
                                                        <option value="부장">부장</option>
                                                        <option value="이사">이사</option>
                                                    </select>
                                                </div>
                                                <div class="form-group" style="margin-bottom: 1.2rem;">
                                                    <label
                                                        style="display: block; margin-bottom: 0.5rem; font-size: 0.9rem; color: #555; font-weight: 600;">권한</label>
                                                    <div style="display: flex; gap: 1rem;">
                                                        <label
                                                            style="font-size: 0.9rem; display: flex; align-items: center; gap: 0.3rem;"><input
                                                                type="radio" name="auth" value="user" checked>
                                                            일반</label>
                                                        <label
                                                            style="font-size: 0.9rem; display: flex; align-items: center; gap: 0.3rem;"><input
                                                                type="radio" name="auth" value="admin"> 관리자</label>
                                                    </div>
                                                </div>
                                                <button type="submit"
                                                    style="width: 100%; background: var(--primary-color); color: white; padding: 1rem; border: none; border-radius: 1rem; font-size: 1rem; font-weight: 700; cursor: pointer; transition: 0.2s;">등록
                                                    완료</button>
                                            </form>
                                        </div>

                                        <!-- 목록 섹션 -->
                                        <div class="list-section"
                                            style="background: white; padding: 2rem; border-radius: 1.5rem; box-shadow: 0 10px 40px rgba(0,0,0,0.04);">
                                            <h2 style="margin-top: 0; font-size: 1.4rem;">📋 활성 직원 계정</h2>
                                            <table class="employee-table"
                                                style="width: 100%; border-collapse: collapse; margin-top: 1rem;">
                                                <thead>
                                                    <tr style="border-bottom: 2px solid #f0f0f0;">
                                                        <th
                                                            style="text-align: left; padding: 1rem; color: #888; font-size: 0.85rem; text-transform: uppercase;">
                                                            ID</th>
                                                        <th
                                                            style="text-align: left; padding: 1rem; color: #888; font-size: 0.85rem; text-transform: uppercase;">
                                                            성명</th>
                                                        <th
                                                            style="text-align: left; padding: 1rem; color: #888; font-size: 0.85rem; text-transform: uppercase;">
                                                            직책</th>
                                                        <th
                                                            style="text-align: left; padding: 1rem; color: #888; font-size: 0.85rem; text-transform: uppercase;">
                                                            권한</th>
                                                        <th
                                                            style="text-align: center; padding: 1rem; color: #888; font-size: 0.85rem; text-transform: uppercase;">
                                                            관리</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <% if (employeeList !=null && !employeeList.isEmpty()) { for
                                                        (EmployeeDTO employee : employeeList) { %>
                                                        <tr
                                                            style="border-bottom: 1px solid #f9f9f9; transition: background 0.2s;">
                                                            <td
                                                                style="padding: 1.2rem 1rem; font-family: monospace; font-weight: 600; color: #555;">
                                                                <%= employee.getEmpId() %>
                                                            </td>
                                                            <td
                                                                style="padding: 1.2rem 1rem; font-weight: 700; color: var(--primary-color); underline: none;">
                                                                <a href="EmployeeServlet?action=detail&empId=<%= employee.getEmpId() %>"
                                                                    style="color: inherit; text-decoration: none;">
                                                                    <%= employee.getEmpName() %>
                                                                </a>
                                                            </td>
                                                            <td
                                                                style="padding: 1.2rem 1rem; color: #666; font-size: 0.95rem;">
                                                                <%= employee.getPosition() !=null ?
                                                                    employee.getPosition() : "-" %>
                                                            </td>
                                                            <td style="padding: 1.2rem 1rem;">
                                                                <% if("admin".equals(employee.getAuth())) { %>
                                                                    <span
                                                                        style="background: rgba(100, 108, 255, 0.1); color: var(--primary-color); padding: 0.3rem 0.8rem; border-radius: 2rem; font-size: 0.8rem; font-weight: 700;">관리자</span>
                                                                    <% } else { %>
                                                                        <span
                                                                            style="background: #f0f0f0; color: #888; padding: 0.3rem 0.8rem; border-radius: 2rem; font-size: 0.8rem; font-weight: 700;">일반</span>
                                                                        <% } %>
                                                            </td>
                                                            <td style="padding: 1.2rem 1rem; text-align: center;">
                                                                <div
                                                                    style="display: flex; gap: 0.5rem; justify-content: center;">
                                                                    <a href="EmployeeServlet?action=edit&empId=<%= employee.getEmpId() %>"
                                                                        style="color: #666; font-size: 1.1rem; text-decoration: none;">✏️</a>
                                                                    <a href="EmployeeServlet?action=delete&empId=<%= employee.getEmpId() %>"
                                                                        onclick="return confirm('정말 삭제하시겠습니까?');"
                                                                        style="color: #666; font-size: 1.1rem; text-decoration: none;">🗑️</a>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                        <% } } else { %>
                                                            <tr>
                                                                <td colspan="5"
                                                                    style="padding: 3rem; text-align: center; color: #999;">
                                                                    등록된 직원이 없습니다.</td>
                                                            </tr>
                                                            <% } %>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                        </body>

                        </html>