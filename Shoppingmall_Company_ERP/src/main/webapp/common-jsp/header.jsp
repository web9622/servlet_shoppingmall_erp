<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%-- header.jsp : 상단 네비게이션 공통 헤더 --%>

        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">


        <header>
            <nav>
                <ul class="main-menu">
                    <li><a href="index.jsp">메인 페이지</a></li>
                    <li>
                        <a href="#">사용자 관리 <span style="font-size: 0.7rem; opacity: 0.7;">▼</span></a>
                        <ul class="dropdown-menu">
                            <li><a href="employee_form.jsp">직원 등록</a></li>
                            <li><a href="EmployeeServlet?command=list">직원 관리</a></li>
                        </ul>
                    </li>

                    <li>
                        <a href="#">상품 관리 <span style="font-size: 0.7rem; opacity: 0.7;">▼</span></a>
                        <ul class="dropdown-menu">
                            <li><a href="ProductServlet?command=list">상품 목록</a></li>
                            <li><a href="product_add.jsp">상품 추가</a></li>
                        </ul>
                    </li>
                    <li><a href="OrderServlet?command=list">주문 관리</a></li>
                    <li><a href="CustomerServlet?command=list">고객 관리</a></li>
                    <li><a href="groupware.jsp">그룹웨어</a></li>
                </ul>
            </nav>
            <%-- Session을 사용해서 로그인 상태 확인하기! ( 한울 추가 )--%>
                <div class="user-info">
                    <% String loginUser=(String) session.getAttribute("loginUser"); if (loginUser==null ||
                        loginUser.trim().isEmpty()) { %>
                        <a href="login.jsp">로그인</a>
                        <% } else { String userName=(String) session.getAttribute("userName"); if (userName==null)
                            userName=loginUser; %>
                            <span class="welcome-text">
                                <%=userName%>님 환영합니다
                            </span>
                            <a href="LogoutServlet">로그아웃</a>
                            <% } %>
                </div>
        </header>
        </body>

        </html>