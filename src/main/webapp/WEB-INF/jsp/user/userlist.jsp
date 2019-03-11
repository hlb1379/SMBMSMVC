<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" isELIgnored="false" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>查询JSP Page</title>
</head>
<body>
    <h1>查询用户列表</h1>
    <form action="${pageContext.request.contextPath }/user/userlist.html " method="post">
        用户名称：<input type="text" name="username" value=""/>
        <input type="submit" name="查询"/>
    </form>
    <c:forEach var="user" items="${queryUserList}">
        <div>
            id:${user.id}-------
            用户编码：id:${user.id}-------
            用户名称：id:${user.userName}-------
            用户密码：id:${user.userPassword}-------
            用户生日：id:${user.birthday}-------
            用户地址：id:${user.address}-------
        </div>
    </c:forEach>


</body>
</html>