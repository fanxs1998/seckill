<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="APP_PATH" value="${pageContext.request.contextPath}"/>

<html>
<head>
    <title>Title</title>
    <link href="${APP_PATH}/bootstrap/css/bootstrap.min.css" type="text/css" rel="stylesheet">
</head>
<body>
<%--
<a href="<%=request.getContextPath() %>/seckill/list">ddd</a>
--%>
<a href="${APP_PATH}/seckill/list" class="btn btn-info">秒杀列表</a>
</body>

<script src="${APP_PATH}/bootstrap/jquery-3.1.1.min.js"></script>

<script src="${APP_PATH}/bootstrap/js/bootstrap.min.js"></script>

</html>
