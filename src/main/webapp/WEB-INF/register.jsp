<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<jsp:useBean id="errors" scope="request" type="java.util.ArrayList<java.lang.String>"/>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Register</title>
    <link href="${pageContext.request.contextPath}/styles.css" rel="stylesheet"/>
</head>
<body>
<h1>Register</h1>
<form action="" method="post">
    Username: <input type="text" name="username"><br>
    Email:    <input type="email" name="email"><br>
    Password: <input type="password" name="password"><br>
    <input type="submit" value="Register">
</form>
<p>
    <a href="../login">Login here</a>
</p>

<c:if test="${not empty errors}">
    <p style="color:red;">Error:</p>
    <c:forEach var="error" items="${errors}">
        <p style="color:red;">${error}</p>
    </c:forEach>
</c:if>
</body>
</html>