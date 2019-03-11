<%@page contentType="text/html" isELIgnored="false" %>
<%@page pageEncoding="UTF-8" %>
 <%@taglib prefix="fm" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>JSP Page</title>
    <script type="text/javascript" src="${pageContext.request.contextPath }/statics/calendar/WdatePicker.js"></script>
</head>
<body>
    <fm:form method="post" modelAttribute="user">
        <fm:errors path="userCode"/><br/>
        用户编码：<fm:input path="userCode"/><br/>
        <fm:errors path="userName"/><br/>
        用户名称：<fm:input path="userName"/><br/>
        <fm:errors path="userPassword"/><br/>
        用户密码：<fm:input path="userPassword"/><br/>
        <fm:errors path="birthday"/><br/>
        用户生日：<fm:input path="birthday" Class="Wdate" id="birthday" name="birthday"
                       readonly="readonly" onclick="WdatePicker();"/><br/>
        用户地址：<fm:input path="address"/><br/>
        联系电话：<fm:input path="phone"/><br/>
        用户角色：<fm:radiobutton path="userRole" value="1"/>系统管理员
        用户角色：<fm:radiobutton path="userRole" value="2"/>经理
        用户角色：<fm:radiobutton path="userRole" value="3" checked="checked"/>普通用户

        <br/>
        <input type="submit" value="保存"/>
    </fm:form>

</body>
</html>