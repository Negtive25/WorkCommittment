package com.orderManagement;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductCrud {
    //定义一个StringBuilder对象，用于查询结果的拼接
    static StringBuilder sb = new StringBuilder();

    //对product表进行的增删改查操作
    //往数据库product表中插入产品信息
    public static void insertProduct(String ProductID, String ProductName, String ProductPrice) throws SQLException {
        //mark=0表示插入成功，mark=1表示插入失败
        int mark=0;
        //判断价格是否合法
        if(Double.parseDouble(ProductPrice)<=0){
            System.out.println("Product Price should be greater than 0.");
            mark=1;
        }
        //判断productID是否合法
        if(Double.parseDouble(ProductID)<=0){
            System.out.println("Product ID should be greater than 0.");
            mark=1;
        }
        //检查是否会插入重复的productID
        String sql2 = "SELECT EXISTS(SELECT 1 FROM Product WHERE ProductID = ?)";
        ResultSet resultSet = JdbcUtil.executeQuery(sql2, ProductID);

        if (resultSet.next()&& resultSet.getInt(1)==1) {
            resultSet.close();
            System.out.println("Product ID:"+ProductID+" already exists.");
            mark=1;
        }
        if(mark==1){
            System.out.println("Insert failed.\n-----------------------------");
            return;
        }

        String sql = "INSERT INTO Product(ProductID, ProductName, ProductPrice) VALUES(?,?,?)";
        JdbcUtil.executeUpdate(sql, ProductID, ProductName, ProductPrice);
        System.out.println("Product ID:"+ProductID+" Inserted into Product table Successfully.\n-----------------------------");
    }

    //从数据库product表中删除产品信息，同时也会从所有订单中删除相关产品信息
    public static void deleteProduct(String ProductID) throws SQLException {
        //首先删除product表中的记录
        String sql1 = "DELETE FROM Product WHERE ProductID =?";
        int rowDeleted = JdbcUtil.executeUpdate(sql1, ProductID);
        if (rowDeleted == 0) {

            System.out.println(sb.append("Product ID:")
                    .append(ProductID)
                    .append(" not found in Product table.\n")
                    .append("-----------------------------")
                    .toString());
            sb.setLength(0);
            return;
        }

        ResultSet resultSet1 =null,resultSet2=null;
        //如果删除了产品信息，则也需要删除orderproduct表中的记录
        //但有的订单可能只有一个商品，所以删除OrderProduct表中对应的产品的记录时候
        //可能会把某个订单仅存的商品也删除，那对应的那个订单也要随之消失了

        //首先先从OrderProduct表中查找出这个产品对应的订单ID有哪些
        String sql2="SELECT OrderID FROM OrderProduct WHERE ProductID =?";
        resultSet1 = JdbcUtil.executeQuery(sql2, ProductID);

        while (resultSet1.next()) {
            //找到该产品了对应每个的订单号
            String OrderID = resultSet1.getString("OrderID");
            //然后再一个一个删除OrderProduct表中的记录,边删边检查订单是否还有商品
            String sql3 = "DELETE FROM OrderProduct WHERE OrderID =? AND ProductID =?";
            JdbcUtil.executeUpdate(sql3, OrderID, ProductID);

            //检查订单是否还有商品
            String sql4 = "SELECT EXISTS(SELECT 1 FROM OrderProduct WHERE OrderID = ?)";
            resultSet2 = JdbcUtil.executeQuery(sql4, OrderID);

            //如果发现订单中没有商品了，则删除Orders表中的记录
            if (!resultSet2.next()||resultSet2.getInt(1)!=1) {
                String sql5="DELETE FROM Orders WHERE OrderID =?";
                JdbcUtil.executeUpdate(sql5, OrderID);
                continue;
            }
            //更新删除产品后的订单的总价
            OrdersCrud.updateOrdersTotalPrice(OrderID);
        }
        resultSet1.close();
        System.out.println(sb.append("Product ID:").append(ProductID).
                append(" Deleted from Product table and OrderProduct table Successfully.\n-----------------------------")
                .toString());
        sb.setLength(0);
    }

    //更新数据库product表中的产品信息,同时也会更新所有订单中相关产品信息
    //主键ProductID不允许修改
    //更新的范围是：
    // Product表的ProductName、ProductPrice
    // Orders表的TotalPrice
    public static void updateProduct(String ProductID, String ProductNewName, String ProductNewPrice) throws SQLException {
        //判断价格是否合法
        if(Double.parseDouble(ProductNewPrice)<=0){
            sb.append("Product Price should be greater than 0.\n")
                    .append("-----------------------------");
            System.out.println(sb.toString());
            sb.setLength(0);
            return;
        }

        //更新product表中的记录
        String sql1 = "UPDATE Product SET ProductName =?, ProductPrice =? WHERE ProductID =?";
        int rowUpdated = JdbcUtil.executeUpdate(sql1, ProductNewName, ProductNewPrice, ProductID);
        if (rowUpdated == 0) {
            sb.append("Product ID:").append(ProductID).append(" not found in Product table.Update Failed.\n")
                    .append("-----------------------------");
            System.out.println(sb.toString());
            sb.setLength(0);
            return;
        }

        //更新包含该产品的订单的TotalPrice
        //首先先查找出这个产品对应的订单中订单号
        //然后再一个一个更新Orders表中的TotalPrice字段
        String sql2="SELECT OrderID FROM OrderProduct WHERE ProductID =?";
        ResultSet resultSet = JdbcUtil.executeQuery(sql2, ProductID);
        //然后再一个一个更新Orders表中的TotalPrice字段
        while (resultSet.next()) {
            String OrderID = resultSet.getString("OrderID");
            OrdersCrud.updateOrdersTotalPrice(OrderID);
        }
        resultSet.close();
        sb.append("New Product Name:").append(ProductNewName)
                .append("  New Product Price:").append(ProductNewPrice)
                .append("  Updated in Product table Successfully.\n")
                .append("-----------------------------");
        System.out.println(sb.toString());
        sb.setLength(0);
    }

    //从数据库product表中查询产品信息
    public static void queryProductByID(String ProductID) throws SQLException {
        System.out.println("Querying Product Information...\n");

        String sql = "SELECT ProductID, ProductName, ProductPrice FROM Product WHERE ProductID =?";
        ResultSet resultSet = JdbcUtil.executeQuery(sql, ProductID);
        printProductInfo(resultSet);
    }

    //查询产品数据，支持模糊查询
    //第一个参数为产品名，第二个参数为匹配规则，0代表完全匹配，1代表模糊匹配
    public static void queryProductByName(String ProductName, int matchRule) throws SQLException {
        System.out.println("Querying Product Information...\n");
        String sql=null;

        //0代表完全匹配，1代表模糊匹配
        if(matchRule==0)
        sql = "SELECT ProductID, ProductName, ProductPrice FROM Product WHERE ProductName LIKE ?";
        else
            sql ="SELECT ProductID, ProductName, ProductPrice " +
                    "FROM Product " +
                    "WHERE ProductName LIKE ?;";
        ProductName="%"+ProductName+"%";

        ResultSet resultSet = JdbcUtil.executeQuery(sql,ProductName);
        printProductInfo(resultSet);
    }

    //打印查询到的产品信息
    public static void printProductInfo(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            sb.append("Product ID: " + resultSet.getString("ProductID"))
                    .append("\tProduct Name: " + resultSet.getString("ProductName"))
                    .append("\tProduct Price: " + resultSet.getString("ProductPrice") + "\n")
                    .append("-----------------------------");
            resultSet.close();
            System.out.println(sb.toString());
            sb.setLength(0);
        }
        else {
            System.out.println("Product not found.\n-----------------------------");
        }
    }

    public static int checkProductExistOrRepeatedOrder(String OrderID, String... ProductIDAndQuantity) throws SQLException {
        int mark=0;//如果只要有一个productID不存在，则mark=1，表示插入失败

        String sql1 = "SELECT EXISTS(SELECT 1 FROM Orders WHERE OrderID = ?)";
        String sql2 = "SELECT EXISTS(SELECT 1 FROM Product WHERE productID = ?)";
        ResultSet resultSet=null;

        //首先检查是否会插入重复的orderID
        if(OrderID!=null){
            resultSet = JdbcUtil.executeQuery(sql1, OrderID);
            //若存在重复的orderID，则mark=1，表示插入失败
            if (resultSet.next()&& resultSet.getInt(1)==1) {
                mark=1;
                sb.append("OrderID:").append(OrderID).append(" Already Exists.\n");
            }
        }

        //然后检查是否有不存在的productID
        //我的productIDAndQuantity样子是这样的：{"101","2","102","1"}，表示订单中有两个商品，分别是101和102，数量分别是2和1
        //所以我从productIDAndQuantity数组中取出productID
        for (int i = 0; i < ProductIDAndQuantity.length; i += 2) {
            resultSet = JdbcUtil.executeQuery(sql2, ProductIDAndQuantity[i]);
            //若存在不存在的productID，则mark=1，表示插入失败
            if (!resultSet.next()||resultSet.getInt(1)!=1) {
                sb.append("ProductID:").append(ProductIDAndQuantity[i]).append(" Doesn't Exists.\n");
                mark=1;
            }

            //检查数量是否合法
            if(Double.parseDouble(ProductIDAndQuantity[i+1])<=0){
                sb.append("Quantity:").append(ProductIDAndQuantity[i+1]).append(" should be greater than 0.");
                mark=1;
            }
        }
        resultSet.close();
        if (mark==1){
            System.out.println(sb.toString());
            sb.setLength(0);
        };
        return mark;
    }

    //商品排序（价格、名字）
    //sortRule==0,代表不排序
    //sortRule==1代表按照价格排序，sortRule==2代表按照名字排序
    //descOrAsc==1代表降序，descOrAsc==2代表升序
    public static void sortProductAndDisplay(int sortRule, int descOrAsc) throws SQLException {
        String sql=null;
        ResultSet resultSet=null;
        if(sortRule==1&&descOrAsc==1){
            sql = "SELECT ProductID,ProductName,ProductPrice FROM Product ORDER BY ProductPrice DESC";
        }
        else if(sortRule==1&&descOrAsc==2){
            sql = "SELECT ProductID,ProductName,ProductPrice FROM Product ORDER BY ProductPrice ASC";
        }
        else if(sortRule==2&&descOrAsc==1){
            sql = "SELECT ProductID,ProductName,ProductPrice FROM Product ORDER BY ProductName DESC";
        }
        else if(sortRule==2&&descOrAsc==2)
        {
            sql = "SELECT ProductID,ProductName,ProductPrice FROM Product ORDER BY ProductName ASC";
        }
        else{
            sql = "SELECT ProductID,ProductName,ProductPrice FROM Product";
        }
        try {
            resultSet = JdbcUtil.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("All Products Information as Follows:\n");
        while (resultSet.next()) {
            sb.append("Product ID: " + resultSet.getString("ProductID"))
                    .append("\tProduct Name: " + resultSet.getString("ProductName"))
                    .append("\tProduct Price: " + resultSet.getString("ProductPrice") + "\n")
                    .append("------------------------\n");
        }
        resultSet.close();
        String result=sb.toString();
        sb.setLength(0);
        System.out.println(result);
    }
}
