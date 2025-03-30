﻿<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<jsp:useBean id="directory" scope="request" type="labs.dirbrowser.Directory"/>
<jsp:useBean id="generationTime" scope="request" type="java.lang.String"/>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Index of ${directory.name}</title>
    <link href="${pageContext.request.contextPath}/styles.css" rel="stylesheet"/>
</head>
<body>
    <section id="main">
        <header>
            <p>
                Generated at ${generationTime}
            </p>

            <h1>
                Index of
                <a href="?path=${directory.parent.relativeDirectory}">
                    <c:choose>
                        <c:when test="${directory.relativeDirectory.blank}">
                            /
                        </c:when>
                        <c:otherwise>
                            ${directory.relativeDirectory}/
                        </c:otherwise>
                    </c:choose>
                </a>
            </h1>
        </header>
        <table id="index">
            <thead>
                <tr>
                    <th abbr="Name">Name</th>
                    <th abbr="Size">Size</th>
                    <th abbr="Modified">Last Modified</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="dir" items="${directory.directories}">
                    <tr class="directory">
                        <td class="name">
                            <a href="?path=${dir.relativePath}">
                                ${dir.name}/
                            </a>
                        </td>
                        <td></td>
                        <td class="modified">
                            ${dir.lastModified}
                        </td>
                    </tr>
                </c:forEach>

                <c:forEach var="file" items="${directory.files}">
                    <tr class="file">
                        <td class="name">
                            <a href="?path=${file.relativePath}">
                                ${file.name}
                            </a>
                        </td>
                        <td class="length">
                                ${file.size}
                        </td>
                        <td class="modified">
                                ${file.lastModified}
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </section>
</body>
</html>