<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="java.io.*" %>
<%@ page import="com.company1.DBManager" %>
    
<!DOCTYPE html>
<html>
<head>
<!-- CSS 연결 - common.css를 먼저, login.css를 나중에 -->
<link rel="stylesheet" type="text/css" href="css/common.css">
<link rel="stylesheet" type="text/css" href="css/login.css">  
<meta charset="UTF-8">
<title>로그인</title>
</head>
<body>

<!-- 헤더 포함 -->
<%@ include file="common-jsp/header.jsp" %>

<div class="page-content">
    <div class="login-container">
        <div class="logo">
            <h1>로그인</h1>
        </div>

        <%-- 로그인 실패 시 에러 메시지 출력 --%>
        <%
            String error = (String)request.getAttribute("loginError");
            if(error != null) {
        %>
            <div style="color: #ff4d4d; text-align: center; margin-bottom: 15px; font-weight: bold;">
                <%= error %>
            </div>
        <%
            }
        %>

        <form action="LoginServlet" method="post" class="login-form">
            <%-- 로그인 텍스트쓰는 공간(인풋) --%>
            <div class="input-group">
                <input type="text" name="empId" placeholder="아이디">
            </div>
            <div class="input-group">
                <input type="password" name="empPw" placeholder="비밀번호">
            </div>
            
            <%-- 로그인 버튼 --%>
            <button type="submit" class="login-btn">로그인</button>
            
            <div class="find-links">
                <a href="findPassword.jsp">비밀번호 찾기</a>
                <span>|</span>
                <a href="findId.jsp">아이디 찾기</a>
            </div>
        </form>
    </div>
</div>

</body>
</html>