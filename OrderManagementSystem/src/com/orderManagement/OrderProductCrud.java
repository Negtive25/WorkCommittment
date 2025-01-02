package com.orderManagement;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderProductCrud {
    //定义一个StringBuilder对象，用于查询结果的拼接
    static StringBuilder sb = new StringBuilder();

    //OrderProduct表是创建的一个额外的关联表，用来表示一个订单中存在的多个商品之间的关系。
    //订单ID（作为外键关联到Order表）
    //商品ID（作为外键关联到Product表）
    //商品数量
    //
    //OrderProduct表的设计如下：
    //OrderID	ProductID	Quantity
    //   1	       101	       2
    //   1	       102	       1
    //   2	       101	       1
    //   2	       103	       3
    //在这个设计中，每个订单可以有多个商品
    //因此对这个表的增删改查操作相当于对订单包含的商品进行增删改查操作

    //对OrderProduct表进行的增删改查操作
    //往数据库OrderProduct表中插入一条记录,相当于向订单中添加一个商品
    public static void insertOrderProduct(String OrderID, String ProductID, String Quantity) throws SQLException {
        ResultSet resultSet = null;

        //当插入OrderProduct记录时，若order表中不存在该订单，则要报错
        //因此需要先查看order表中是否存在该订单
        String sql = "SELECT EXISTS(SELECT 1 FROM Orders WHERE OrderID = ?)";
        resultSet = JdbcUtil.executeQuery(sql, OrderID);
        if (!resultSet.next()&&resultSet.getInt(1)!=1) {
            resultSet.close();
            System.out.println("The order you want to insert the product into does not exist in the order table.\n-----------------------------");
            return;
        }
        resultSet.close();

        //若order表中存在该订单，则需要查看插入的商品编号是否正确的
        //查看产品编号是否存在
        sql = "SELECT EXISTS(SELECT 1 FROM Product WHERE ProductID = ?)";
        resultSet = JdbcUtil.executeQuery(sql, ProductID);
        if (!resultSet.next()&&resultSet.getInt(1)!=1) {
            resultSet.close();
            System.out.println("The product inserted does not exist.\n-----------------------------");
            return;
        }

        //查看添加的商品是否已经在订单中存在
        sql = "SELECT EXISTS(SELECT 1 FROM OrderProduct WHERE OrderID = ? AND ProductID = ?)";
        resultSet = JdbcUtil.executeQuery(sql, OrderID, ProductID);
        if (resultSet.next() && resultSet.getInt(1) == 1) {
            resultSet.close();
            System.out.println("The product already exists in the order.\n-----------------------------");
            return;
        }

        //若首先订单存在，商品编号正确，且商品不在订单中，则可以把新增的商品插入OrderProduct表
        sql = "INSERT INTO OrderProduct(OrderID, ProductID, Quantity) VALUES(?,?,?)";
        JdbcUtil.executeUpdate(sql, OrderID, ProductID, Quantity);
        //插入了一条OrderProduct记录，则更新order表中的记录

        OrdersCrud.updateOrdersTotalPrice(OrderID);
        System.out.println("Successfully inserted product: " + ProductID + " into order: " + OrderID + "\n-----------------------------");
    }

    //从数据库OrderProduct表中删除一条记录,相当于从订单中删除一个商品
    public static void deleteOrderProduct(String OrderID, String ProductID) throws SQLException {
        ResultSet resultSet =null;

        //删除OrderProduct表中的记录
        String sql1 = "DELETE FROM OrderProduct WHERE OrderID =? AND ProductID =?";
        JdbcUtil.executeUpdate(sql1, OrderID, ProductID);

        //如果删除了最后一条OrderProduct记录，则删除order表中的记录
        String sql2 = "SELECT EXISTS(SELECT 1 FROM OrderProduct WHERE OrderID = ?)";
        resultSet = JdbcUtil.executeQuery(sql2, OrderID);
        if (!resultSet.next()&&resultSet.getInt(1)!=1) {
            resultSet.close();
            OrdersCrud.deleteOrder(OrderID);
        }
        resultSet.close();

        //如果删除的商品不是最后一条商品，订单表中还存在其他的商品，订单的总价需要重新计算
        OrdersCrud.updateOrdersTotalPrice(OrderID);
        System.out.println("Successfully deleted product: " + ProductID + " from order: " + OrderID + "\n-----------------------------");
    }

    //这个方法是把OrderProduct这个表中所有含有相同订单ID的记录都删除，为了更好进行订单的更新
    public static void deleteAllSameOrderID(String OrderID) throws SQLException {
        String sql1 = "DELETE FROM OrderProduct WHERE OrderID =?";
        JdbcUtil.executeUpdate(sql1, OrderID);
    }

    //从数据库OrderProduct表中查询某个订单号的所有商品信息
    public static String queryOrderProduct(String OrderID) throws SQLException {
        String sql = "SELECT OrderProduct.ProductID,OrderProduct.Quantity,Product.ProductName " +
                "FROM OrderProduct " +
                "INNER JOIN Product " +
                "ON OrderProduct.ProductID = Product.ProductID " +
                "WHERE OrderID =?;";
        ResultSet resultSet = JdbcUtil.executeQuery(sql, OrderID);

        while (resultSet.next()) {
            sb.append("ProductID: " + resultSet.getString(1))
                    .append("  ProductName: " + resultSet.getString(3))
                    .append("  Quantity: " + resultSet.getString(2) + "\n");
        }

        resultSet.close();
        String result=sb.toString();
        sb.setLength(0);
        return result;
    }

}
