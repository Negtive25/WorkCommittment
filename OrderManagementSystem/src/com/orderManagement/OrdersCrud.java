package com.orderManagement;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OrdersCrud {
    //定义一个StringBuilder对象，用于查询结果的拼接
    static StringBuilder sb = new StringBuilder();

    //对order表进行的增删改查操作
    //往数据库order表中插入订单信息,同时插入OrderProduct表，表示订单中包含的商品和商品数量
    public static void insertOrder(String OrderID,String OrderDate,String... ProductIDAndQuantity) throws SQLException {
        //如果插入了重复的orderID或者错误的productID,或者日期格式不正确，则mark=1，表示插入失败
        int mark=0;
        //首先检查是否有重复的orderID和不存在的productID,商品数量是否合法
        mark= ProductCrud.checkProductExistOrRepeatedOrder(OrderID, ProductIDAndQuantity);
        //再检查日期格式是否正确
        if(!CheckDateFormat.isValidDate(OrderDate)){
            mark=1;
            System.out.println("Incorrect date format!");
        }
        if(mark==1){
            System.out.println("Insert Order failed!\n-----------------------------");
            return;
        }

        //如果没有重复的orderID和错误的productID，则插入order和OrderProduct表
        String sql1 = "INSERT INTO Orders(OrderID,OrderDate,TotalPrice) VALUES(?,?,?)";

        //首先插入一条order记录
        JdbcUtil.executeUpdate(sql1, OrderID,OrderDate, 0);

        //在插入订单信息后，还需要插入一条OrderProduct记录，表示订单中包含的商品数量
        //我的productIDAndQuantity样子是这样的：{"101","2","102","1"}，表示订单中有两个商品，分别是101和102，数量分别是2和1
        //所以我先从productIDAndQuantity数组中取出productID和quantity
        String sql2 = "INSERT INTO OrderProduct(OrderID, ProductID, Quantity) VALUES(?,?,?)";
        for (int i = 0; i < ProductIDAndQuantity.length; i += 2) {
            String ProductID = ProductIDAndQuantity[i];
            String Quantity = ProductIDAndQuantity[i+1];
            //然后再插入一条OrderProduct记录
            JdbcUtil.executeUpdate(sql2, OrderID, ProductID, Quantity);
        }
        //然后计算订单的总价
        String sql3 ="UPDATE Orders\n" +
                "SET Orders.TotalPrice = (\n" +
                "    SELECT SUM(Product.ProductPrice * OrderProduct.Quantity)\n" +
                "    FROM OrderProduct\n" +
                "    INNER JOIN Product ON OrderProduct.ProductID = Product.ProductID\n" +
                "    WHERE OrderProduct.OrderID = Orders.OrderID\n" +
                ")\n" +
                "WHERE Orders.OrderID = ?;";
        JdbcUtil.executeUpdate(sql3,OrderID);
    }

    //从数据库order表中删除订单信息
    public static void deleteOrder(String OrderID) throws SQLException {
        //首先删除Orders表中的记录
        String sql = "DELETE FROM Orders WHERE OrderID =?";
        JdbcUtil.executeUpdate(sql, OrderID);
        //然后删除OrderProduct表中的记录
        String sql2 = "DELETE FROM OrderProduct WHERE OrderID =?";
        JdbcUtil.executeUpdate(sql2, OrderID);
        System.out.println("Successfully deleted order: " + OrderID+"\n-----------------------------");
    }

    //更新数据库order表中的一条订单信息
    //不允许更新订单的编号
    //更新的范围是：：
    //Orders表中的OrderDate、TotalPrice
    //OrderProduct表中的产品数量Quantity
    public static void updateOrder(String OrderID,String NewOrderDate,String... newProductIDAndQuantity) throws SQLException {
        //如果要更新的OrderID不存在,日期格式不正确,productID不存在,商品数量不合法,则mark=1，表示更新失败
        int mark=0;

        //首先检查要更新的OrderID存不存在
        String sql0 = "SELECT EXISTS(SELECT 1 FROM Orders WHERE OrderID = ?)";
        ResultSet resultSet=JdbcUtil.executeQuery(sql0, OrderID);
        if(resultSet.next()){
            if(resultSet.getInt(1)!=1){
                mark=1;
                sb.append("Order not found!\n");
            }
        }
        resultSet.close();

        //再检查要更新的productID是否正确和商品数量是否合法
        if(1==ProductCrud.checkProductExistOrRepeatedOrder(null,newProductIDAndQuantity))
            mark=1;

        //再检查日期格式是否正确
        if(!CheckDateFormat.isValidDate(NewOrderDate)){
            mark=1;
            sb.append("Incorrect date format!\n");
        }

        //如果mark=1，表示更新失败，打印错误信息并返回
        if(mark==1){
            sb.append("\nUpdate Order Information failed!\n-----------------------------");
            System.out.println(sb.toString());
            sb.setLength(0);
            return;
        }

        //如果productID全部正确，则更新OrderProduct和Orders和表

        //先更新OrderProduct表中的记录
        //步骤是先把表中的所有拥有相同订单ID的商品都删除，然后再插入更新后的数据
        OrderProductCrud.deleteAllSameOrderID(OrderID);

        //先从ProductIDAndQuantity这个可变参数中取出产品编号ProductID, 产品数量Quantity
        //然后再一个一个插入OrderProduct表
        String sql2 = "INSERT INTO OrderProduct(OrderID,ProductID,Quantity) VALUES(?,?,?)";
        for (int i = 0; i < newProductIDAndQuantity.length; i += 2) {
            String newProductID = newProductIDAndQuantity[i];
            String newQuantity = newProductIDAndQuantity[i+1];
            //然后再插入一条OrderProduct记录
            JdbcUtil.executeUpdate(sql2, OrderID, newProductID, newQuantity);
        }

        //然后更新orders表中的记录,主键OrderID不作更新，更新的字段是OrderDate,TotalPrice
        //先更新
        String sql1 = "UPDATE Orders SET OrderDate =? WHERE OrderID =?";
        JdbcUtil.executeUpdate(sql1,NewOrderDate,OrderID);
        //更新订单总价
        OrdersCrud.updateOrdersTotalPrice(OrderID);

        System.out.println(sb.append("Successfully updated order: ")
                .append(OrderID)
                .append("\n-----------------------------").toString());
        sb.setLength(0);
    }

    //从数据库order表中查询某个订单信息
    public static String queryOrder(String OrderID) throws SQLException {
        String sql = "SELECT OrderID, OrderDate,TotalPrice FROM Orders WHERE OrderID =?";
        ResultSet resultSet = JdbcUtil.executeQuery(sql, OrderID);

        if (resultSet.next()) {
            sb.append("Order ID: " + resultSet.getString("OrderID") + "\n")
                    .append("Order Date: " + resultSet.getString("OrderDate") + "\n")
                    .append("Total Price: " + resultSet.getString("TotalPrice") + "\n");
            resultSet.close();
            String result=sb.toString();
            sb.setLength(0);
            return result;
        } else {
            return null;
        }
    }

    //查询一个订单的所有信息，包括订单号、订单日期、总价、商品信息
    public static void querySingleOrderID(String OrderID) throws SQLException {
        System.out.println("Querying single order information...\n");
        String result=OrdersCrud.queryOrder(OrderID);
        if(result==null){
            System.out.println("Order not found.\n-----------------------------");
            return;
        }
        sb.append(result)
                .append(OrderProductCrud.queryOrderProduct(OrderID))
                .append("-----------------------------");
        System.out.println(sb.toString());
        sb.setLength(0);
    }

    //订单排序（价格、下单时间）
    //sortRule==0代表按照不排序
    //sortRule==1代表按照价格排序，sortRule==2代表按照下单时间排序
    //descOrAsc==1代表降序，descOrAsc==2代表升序
    public static void sortOrderAndDisplay(int sortRule, int descOrAsc) throws SQLException {
        String sql=null;
        ResultSet resultSet=null;

        if(sortRule==1&&descOrAsc==1){
            sql = "SELECT OrderID, OrderDate,TotalPrice FROM Orders ORDER BY TotalPrice DESC";
        }
        else if(sortRule==1&&descOrAsc==2){
            sql = "SELECT OrderID, OrderDate,TotalPrice FROM Orders ORDER BY TotalPrice ASC";
        }
        else if(sortRule==2&&descOrAsc==1){
            sql = "SELECT OrderID, OrderDate,TotalPrice FROM Orders ORDER BY OrderDate DESC";
        }
        else if(sortRule==2&&descOrAsc==2)
        {
            sql = "SELECT OrderID, OrderDate,TotalPrice FROM Orders ORDER BY OrderDate ASC";
        }
        else{
            sql = "SELECT OrderID, OrderDate,TotalPrice FROM Orders";
        }
        try {
            resultSet=JdbcUtil.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("All Orders Information as Follows:\n");
        while (resultSet.next()) {
            sb.append("Order ID: " + resultSet.getString("OrderID") + "\n")
                    .append("Order Date: " + resultSet.getString("OrderDate") + "\n")
                    .append("Total Price: " + resultSet.getString("TotalPrice") + "\n")
                    .append("This order contains the following products:\n")
                    .append(OrderProductCrud.queryOrderProduct(resultSet.getString("OrderID")))
                    .append("-----------------------------\n");
        }
        resultSet.close();
        String result=sb.toString();
        sb.setLength(0);
        System.out.println(result);
    }
    public static void updateOrdersTotalPrice(String OrderID) throws SQLException {
        String sql3 ="UPDATE Orders\n" +
                "SET Orders.TotalPrice = (\n" +
                "    SELECT SUM(Product.ProductPrice * OrderProduct.Quantity)\n" +
                "    FROM OrderProduct\n" +
                "    INNER JOIN Product ON OrderProduct.ProductID = Product.ProductID\n" +
                "    WHERE OrderProduct.OrderID = Orders.OrderID\n" +
                ")\n" +
                "WHERE Orders.OrderID = ?;";
        JdbcUtil.executeUpdate(sql3,OrderID);;
    }
}
