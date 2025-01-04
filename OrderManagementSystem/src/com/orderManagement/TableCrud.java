package com.orderManagement;

import java.sql.SQLException;

//用于创建、插入、删除、更新、查询、排序等操作的类
//该类中包含了对Orders、Product、OrderProduct三个表的操作
//对表操作的每一个方法都包含了事务处理，保证数据一致性
public class TableCrud {

    //创建表
    public static void CreateTable(String tableSql) throws SQLException {
        try {
            //设置事务自动提交为false，开启事务
            Test.jdbcUtil.getConnection().setAutoCommit(false);

            //执行创建表的SQL语句
            Test.jdbcUtil.executeUpdate(tableSql);

            //提交事务
            Test.jdbcUtil.getConnection().commit();
        }catch (SQLException e) {
            //回滚事务
            Test.jdbcUtil.getConnection().rollback();
            System.out.println("Transaction rolled back.");
            throw e;
        }
    }
    //插入产品数据
    public static void InsertProduct(String ProductID, String ProductName, String ProductPrice) throws SQLException {
        try {
            //设置事务自动提交为false，开启事务
            Test.jdbcUtil.getConnection().setAutoCommit(false);

            //执行插入产品数据的方法
            ProductCrud.insertProduct(ProductID, ProductName, ProductPrice);

            //提交事务
            Test.jdbcUtil.getConnection().commit();
        }catch (SQLException e) {
            //回滚事务
            Test.jdbcUtil.getConnection().rollback();
            System.out.println("Transaction rolled back.");
            //释放资源
            Test.jdbcUtil.releaseSources();
            throw e;
        }
        Test.jdbcUtil.releaseSources();
    }

    //插入订单数据
    public static void InsertOrder(String OrderID,String OrderDate,String... ProductIDAndQuantity) throws SQLException {
        try {
            //设置事务自动提交为false，开启事务
            Test.jdbcUtil.getConnection().setAutoCommit(false);

            //执行插入订单数据的方法
            OrdersCrud.insertOrder(OrderID,OrderDate,ProductIDAndQuantity);

            //提交事务
            Test.jdbcUtil.getConnection().commit();
        }catch (SQLException e) {
            //回滚事务
            Test.jdbcUtil.getConnection().rollback();
            System.out.println("Transaction rolled back.");
            //释放资源
            Test.jdbcUtil.releaseSources();
            throw e;
        }
        Test.jdbcUtil.releaseSources();
    }

    //插入订单商品数据
    public static void InsertOrderProduct(String OrderID, String ProductID, String Quantity) throws SQLException {
        try {
            //设置事务自动提交为false，开启事务
            Test.jdbcUtil.getConnection().setAutoCommit(false);

            //执行插入订单商品数据的方法
            OrderProductCrud.insertOrderProduct(OrderID, ProductID, Quantity);

            //提交事务
            Test.jdbcUtil.getConnection().commit();
        }catch (SQLException e) {
            //回滚事务
            Test.jdbcUtil.getConnection().rollback();
            System.out.println("Transaction rolled back.");
            //释放资源
            Test.jdbcUtil.releaseSources();
            throw e;
        }
        Test.jdbcUtil.releaseSources();
    }

    //删除订单中某个商品数据
    public static void DeleteOrderProduct(String OrderID, String ProductID) throws SQLException {
        try {
            //设置事务自动提交为false，开启事务
            Test.jdbcUtil.getConnection().setAutoCommit(false);

            //执行删除订单商品数据的方法
            OrderProductCrud.deleteOrderProduct(OrderID, ProductID);

            //提交事务
            Test.jdbcUtil.getConnection().commit();
        }catch (SQLException e) {
            //回滚事务
            Test.jdbcUtil.getConnection().rollback();
            System.out.println("Transaction rolled back.");
            //释放资源
            Test.jdbcUtil.releaseSources();
            throw e;
        }
        Test.jdbcUtil.releaseSources();
    }

    //删除整个订单数据，第一个参数为订单ID
    public static void DeleteOrder(String OrderID) throws SQLException {
        try {
            //设置事务自动提交为false，开启事务
            Test.jdbcUtil.getConnection().setAutoCommit(false);

            //执行删除整个订单数据的方法
            OrdersCrud.deleteOrder(OrderID);

            //提交事务
            Test.jdbcUtil.getConnection().commit();
        }catch (SQLException e) {
            //回滚事务
            Test.jdbcUtil.getConnection().rollback();
            System.out.println("Transaction rolled back.");
            //释放资源
            Test.jdbcUtil.releaseSources();
            throw e;
        }
        Test.jdbcUtil.releaseSources();
    }

    //删除产品，随带删除订单内相关商品数据，更新订单总价
    public static void DeleteProduct(String ProductID) throws SQLException {
        try {
            //设置事务自动提交为false，开启事务
            Test.jdbcUtil.getConnection().setAutoCommit(false);

            //执行删除产品数据的方法
            ProductCrud.deleteProduct(ProductID);

            //提交事务
            Test.jdbcUtil.getConnection().commit();
        }catch (SQLException e) {
            //回滚事务
            Test.jdbcUtil.getConnection().rollback();
            System.out.println("Transaction rolled back.");
            //释放资源
            Test.jdbcUtil.releaseSources();
            throw e;
        }
        Test.jdbcUtil.releaseSources();
    }

    //查询存在产品数据
    public static void QueryProductByID(String ProductID) throws SQLException {
        try {
            //设置事务自动提交为false，开启事务
            Test.jdbcUtil.getConnection().setAutoCommit(false);

            //执行查询存在产品数据的方法
            ProductCrud.queryProductByID(ProductID);

            //提交事务
            Test.jdbcUtil.getConnection().commit();
        }catch (SQLException e) {
            //回滚事务
            Test.jdbcUtil.getConnection().rollback();
            System.out.println("Transaction rolled back.");
            //释放资源
            Test.jdbcUtil.releaseSources();
            throw e;
        }
        Test.jdbcUtil.releaseSources();
    }

    //查询存在订单数据
    public static void QueryOrderByID(String OrderID) throws SQLException {
        try {
            //设置事务自动提交为false，开启事务
            Test.jdbcUtil.getConnection().setAutoCommit(false);

            //执行查询存在订单数据的方法
            OrdersCrud.querySingleOrderID(OrderID);

            //提交事务
            Test.jdbcUtil.getConnection().commit();
        }catch (SQLException e) {
            //回滚事务
            Test.jdbcUtil.getConnection().rollback();
            System.out.println("Transaction rolled back.");
            //释放资源
            Test.jdbcUtil.releaseSources();
            throw e;
        }
        Test.jdbcUtil.releaseSources();
    }
    //商品排序（价格、名字）
    //sortRule==0,代表不排序
    //sortRule==1代表按照价格排序，sortRule==2代表按照名字排序
    //descOrAsc==1代表降序，descOrAsc==2代表升序
    public static void SortProductAndDisplay(int sortRule, int descOrAsc) throws SQLException {
        try {
            //设置事务自动提交为false，开启事务
            Test.jdbcUtil.getConnection().setAutoCommit(false);

            //执行商品排序并显示的方法
            ProductCrud.sortProductAndDisplay(1, 2);

            //提交事务
            Test.jdbcUtil.getConnection().commit();
        }catch (SQLException e) {
            //回滚事务
            Test.jdbcUtil.getConnection().rollback();
            System.out.println("Transaction rolled back.");
            //释放资源
            Test.jdbcUtil.releaseSources();
            throw e;
        }
        Test.jdbcUtil.releaseSources();
    }

    //订单排序（日期、总价)
    //sortRule==1代表按照价格排序，sortRule==2代表按照日期排序
    //descOrAsc==1代表降序，descOrAsc==2代表升序
    public static void SortOrderAndDisplay(int sortRule, int descOrAsc) throws SQLException {
        try {
            //设置事务自动提交为false，开启事务
            Test.jdbcUtil.getConnection().setAutoCommit(false);

            //执行订单排序并显示的方法
            OrdersCrud.sortOrderAndDisplay(1, 2);

            //提交事务
            Test.jdbcUtil.getConnection().commit();
        }catch (SQLException e) {
            //回滚事务
            Test.jdbcUtil.getConnection().rollback();
            System.out.println("Transaction rolled back.");
            //释放资源
            Test.jdbcUtil.releaseSources();
            throw e;
        }
        Test.jdbcUtil.releaseSources();
    }

    //更新产品数据
    public static void UpdateProduct(String ProductID, String ProductNewName, String ProductNewPrice) throws SQLException {
        try {
            //设置事务自动提交为false，开启事务
            Test.jdbcUtil.getConnection().setAutoCommit(false);

            //执行更新产品数据的方法
            ProductCrud.updateProduct(ProductID, ProductNewName, ProductNewPrice);

            //提交事务
            Test.jdbcUtil.getConnection().commit();
        }catch (SQLException e) {
            //回滚事务
            Test.jdbcUtil.getConnection().rollback();
            System.out.println("Transaction rolled back.");
            //释放资源
            Test.jdbcUtil.releaseSources();
            throw e;
        }
        Test.jdbcUtil.releaseSources();
    }

    //更新订单数据
    public static void UpdateOrder(String OrderID,String NewOrderDate,String... newProductIDAndQuantity) throws SQLException {
        try {
            //设置事务自动提交为false，开启事务
            Test.jdbcUtil.getConnection().setAutoCommit(false);

            //执行更新订单数据的方法
            OrdersCrud.updateOrder(OrderID,NewOrderDate,newProductIDAndQuantity);

            //提交事务
            Test.jdbcUtil.getConnection().commit();
        }catch (SQLException e) {
            //回滚事务
            Test.jdbcUtil.getConnection().rollback();
            System.out.println("Transaction rolled back.");
            //释放资源
            Test.jdbcUtil.releaseSources();
            throw e;
        }
        Test.jdbcUtil.releaseSources();
    }

    //通过产品名字查询产品数据，支持模糊查询
    //第一个参数为产品名，第二个参数为匹配规则，0代表完全匹配，1代表模糊匹配
    public static void QueryProductByName(String ProductName, int matchRule) throws SQLException {
        try {
            //设置事务自动提交为false，开启事务
            Test.jdbcUtil.getConnection().setAutoCommit(false);

            //执行查询产品数据的方法
            ProductCrud.queryProductByName(ProductName, matchRule);

            //提交事务
            Test.jdbcUtil.getConnection().commit();
        }catch (SQLException e) {
            //回滚事务
            Test.jdbcUtil.getConnection().rollback();
            System.out.println("Transaction rolled back.");
            //释放资源
            Test.jdbcUtil.releaseSources();
            throw e;
        }
        Test.jdbcUtil.releaseSources();
    }
}
