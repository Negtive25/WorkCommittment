package com.orderManagement;

import java.sql.*;

public class JdbcUtil {
    private static final String url = "jdbc:mysql://localhost:3306/orderManagement?" +
            "useUnicode=true&characterEncoding=utf8&useSSL=true";
    private static final String username = "root";
    private static final String password = "123456";

    private Connection conn = null;
    private PreparedStatement preStatementForUpdate = null;
    //长度为4的PreparedStatement类型数组和长度为4的ResultSet类型数组一一配对，初始化为null
    private PreparedStatement[] preStatementForQuery={null,null,null,null};
    private ResultSet[] resultSet={null,null,null,null};
    private int useThis=0;

    public void startConnection() throws SQLException {
        conn = DriverManager.getConnection(url, username, password);
    }
    public Connection getConnection() {
        return conn;
    }

    //执行SQL命令，并进行表的增删改查操作
    //params为可变参数
    public int executeUpdate(String sql, Object... params) throws SQLException {
        int statement = 0;
        //创建PreparedStatement对象，使用预编译SQL语句，防止SQL注入攻击
        preStatementForUpdate = conn.prepareStatement(sql);
        //setObject 方法用于设置SQL语句中每个占位符（?）的值
        for (int i = 0; i < params.length; i++) {
            preStatementForUpdate.setObject(i + 1, params[i]);
        }
        statement = preStatementForUpdate.executeUpdate();
        preStatementForUpdate.close();
        return statement;
    }

    //查询SQL命令，并返回查询结果
    //params为可变参数
    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        for(int i=0;i<4;i++){
            if(preStatementForQuery[i]==null)
                useThis=i;
        }
        //创建PreparedStatement对象，使用预编译SQL语句，防止SQL注入攻击
        preStatementForQuery[useThis] = conn.prepareStatement(sql);

        //setObject 方法用于设置SQL语句中每个占位符（?）的值
        for (int i = 0; i < params.length; i++) {
            preStatementForQuery[useThis].setObject(i + 1, params[i]);
        }
        //执行查询，并返回查询结果
        return resultSet[useThis]= preStatementForQuery[useThis].executeQuery();
    }

    //每次执行完一个数据库的命令或者事务回滚之后，就调用这个函数，通过遍历静态数组释放资源
    public void releaseSources() throws SQLException {
        for(int i=0;i<4;i++){
            if(resultSet[i]!=null)
                resultSet[i].close();
            if(preStatementForQuery[i]!=null)
                preStatementForQuery[i].close();
        }
    }
}
