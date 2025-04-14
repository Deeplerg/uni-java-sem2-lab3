<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<jsp:useBean id="errors" scope="request" type="java.util.ArrayList<java.lang.String>"/>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Login</title>
    <link href="${pageContext.request.contextPath}/styles.css" rel="stylesheet"/>
</head>
<body>
<h1>Login</h1>
<form action="" method="post">
    Username: <input type="text" name="username"><br>
    Password: <input type="password" name="password"><br>
    <input type="submit" value="Login">
</form>
<p>
    <a href="../register">Register here</a>
</p>
<c:if test="${not empty errors}">
    <p style="color:red;">Error:</p>
    <c:forEach var="error" items="${errors}">
        <p style="color:red;">${error}</p>
    </c:forEach>
</c:if>
</body>
</html>