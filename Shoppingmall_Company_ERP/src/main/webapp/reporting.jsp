<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ page
        import="java.util.*, com.company1.dto.MonthlySale, com.company1.dto.MonthlyCancel, com.company1.dto.MonthlyRanking "
        %>

        <!DOCTYPE html>
        <html lang="ko">

        <head>
            <meta charset="UTF-8">
            <title>비즈니스 인텔리전스 - 통계 보고서</title>
        </head>

        <body>
            <%@ include file="common-jsp/header.jsp" %>
                <div class="container" style="max-width: 1000px;">

                    <div class="hero-section" style="padding: 3rem 2rem; margin-bottom: 3rem; border-radius: 1.5rem;">
                        <h1 style="margin:0; font-size: 2.5rem;">📊 비즈니스 통계 리포트</h1>
                        <p style="opacity: 0.9; margin-top: 1rem;">월별 판매 추이와 수익 성과를 한눈에 확인하세요.</p>
                    </div>

                    <!-- 월별 판매 통계 -->
                    <div class="list-section" style="margin-bottom: 3rem;">
                        <h2>📈 월별 판매 및 매출 현황</h2>
                        <table>
                            <thead>
                                <tr>
                                    <th>정산 월</th>
                                    <th style="text-align: center;">총 판매 수량</th>
                                    <th style="text-align: right;">총 매출 실적</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% List<MonthlySale> monthlySales = (List<MonthlySale>)
                                        request.getAttribute("monthlySales");
                                        if (monthlySales == null || monthlySales.isEmpty()) {
                                        %>
                                        <tr>
                                            <td colspan="3" class="no-data">집계된 판매 데이터가 없습니다.</td>
                                        </tr>
                                        <% } else { for (MonthlySale sale : monthlySales) { %>
                                            <tr>
                                                <td style="font-weight: 700; color: var(--primary-color);">
                                                    <%= sale.getMonth() %>
                                                </td>
                                                <td style="text-align: center; font-weight: 600;">
                                                    <%= sale.getTotalQuantity() %> 개
                                                </td>
                                                <td
                                                    style="text-align: right; font-weight: 700; color: var(--text-main);">
                                                    ₩<%= String.format("%,.0f", sale.getTotalSales()) %>
                                                </td>
                                            </tr>
                                            <% } } %>
                            </tbody>
                        </table>
                    </div>

                    <!-- 월별 상품별 매출 랭킹 -->
                    <div class="list-section">
                        <h2>🏆 월간 상품 매출 TOP 5</h2>
                        <table>
                            <thead>
                                <tr>
                                    <th>해당 월</th>
                                    <th>베스트 셀러 상품명</th>
                                    <th style="text-align: right;">상품별 매출 총액</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% List<MonthlyRanking> monthlyRankings = (List<MonthlyRanking>)
                                        request.getAttribute("monthlyRankings");
                                        if (monthlyRankings == null || monthlyRankings.isEmpty()) {
                                        %>
                                        <tr>
                                            <td colspan="3" class="no-data">순위 집계 데이터가 없습니다.</td>
                                        </tr>
                                        <% } else { for (MonthlyRanking ranking : monthlyRankings) { %>
                                            <tr>
                                                <td style="font-weight: 600;">
                                                    <%= ranking.getMonth() %>
                                                </td>
                                                <td style="font-weight: 700; color: var(--text-main);">
                                                    <%= ranking.getProductName() %>
                                                </td>
                                                <td
                                                    style="text-align: right; font-weight: 700; color: var(--primary-color);">
                                                    ₩<%= String.format("%,.0f", ranking.getTotalSales()) %>
                                                </td>
                                            </tr>
                                            <% } } %>
                            </tbody>
                        </table>
                    </div>
                </div>
        </body>

        </html>