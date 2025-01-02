package com.orderManagement;

import java.sql.*;

public class JdbcUtil {
    private static final String url = "jdbc:mysql://localhost:3306/orderManagement?" +
            "useUnicode=true&characterEncoding=utf8&useSSL=true";
    private static final String username = "root";
    private static final String password = "123456";

    static  Connection conn = null;
    static  PreparedStatement preStatement = null;

    public static void startConnection() throws SQLException {
        conn = DriverManager.getConnection(url, username, password);
    }

    //执行SQL命令，并进行表的增删改查操作
    //params为可变参数
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        int statement = 0;
        //创建PreparedStatement对象，使用预编译SQL语句，防止SQL注入攻击
        preStatement = conn.prepareStatement(sql);
        //setObject 方法用于设置SQL语句中每个占位符（?）的值
        for (int i = 0; i < params.length; i++) {
            preStatement.setObject(i + 1, params[i]);
        }
        statement = preStatement.executeUpdate();
        preStatement.close();
        return statement;
    }

    //查询SQL命令，并返回查询结果
    //params为可变参数
    public static ResultSet executeQuery(String sql, Object... params) throws SQLException {
        //创建PreparedStatement对象，使用预编译SQL语句，防止SQL注入攻击
        preStatement = conn.prepareStatement(sql);

        //setObject 方法用于设置SQL语句中每个占位符（?）的值
        for (int i = 0; i < params.length; i++) {
            preStatement.setObject(i + 1, params[i]);
        }
        //执行查询，并返回查询结果
        return preStatement.executeQuery();
    }
}
