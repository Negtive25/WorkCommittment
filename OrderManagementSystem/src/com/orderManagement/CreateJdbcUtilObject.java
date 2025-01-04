package com.orderManagement;

import java.sql.*;

public class CreateJdbcUtilObject {
    //因为我JdbcUtil类中的conn成员变量是私有的，成员方法不能是静态的，但是这个工具类是所有功能的基础
    //我在其他类都是直接调用这个工具类的方法，这就和JdbcUtil类中的成员方法不能是静态的冲突了，
    //所以我再创建一个工具类，用来创建JdbcUtil类的实例，并把创建的对象设置为静态的
    //这样就可以保证JdbcUtil类成员conn的私有性和其函数的非静态性的同时，又可以在多个类中静态调用

    // 创建数据库方法类的实例
    public static final JdbcUtil jdbcUtil = new JdbcUtil();
}
