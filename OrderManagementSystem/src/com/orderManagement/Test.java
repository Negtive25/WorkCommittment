package com.orderManagement;

import java.sql.Connection;
import java.sql.SQLException;

public class Test {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {

        //启动数据库连接
        Class.forName("com.mysql.cj.jdbc.Driver");
        CreateJdbcUtilObject.jdbcUtil.startConnection();

        //设置事务隔离级别,这里设置为串行化
        CreateJdbcUtilObject.jdbcUtil.getConnection().setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

//----------------------------------------------------
        //创建三张表，Product、Orders、OrderProduct
        //Product表：ProductID、ProductName、ProductPrice
        //Orders表：OrderID、OrderDate、TotalPrice
        //OrderProduct表：OrderID、ProductID、Quantity

        String createProduct =
                "CREATE TABLE IF NOT EXISTS `Product` (\n" +
                        "    ProductID INT(6) PRIMARY KEY,\n" +
                        "    ProductName VARCHAR(255) NOT NULL,\n" +
                        "    ProductPrice DECIMAL(10, 2) NOT NULL\n" +
                        ")ENGINE = InnoDB DEFAULT CHARSET=utf8;\n" ;
        String createOrder=
                "CREATE TABLE IF NOT EXISTS `Orders`(\n" +
                        "    OrderID INT(8) PRIMARY KEY,\n" +
                        "    OrderDate DATETIME DEFAULT CURRENT_TIMESTAMP,\n" +
                        "    TotalPrice DECIMAL(10, 2) NOT NULL\n" +
                        ")ENGINE = InnoDB DEFAULT CHARSET=utf8;\n";
        String createOrderProduct=
                "CREATE TABLE IF NOT EXISTS `OrderProduct` (\n" +
                        "    OrderID INT(8) NOT NULL,\n" +
                        "    ProductID INT(6) NOT NULL,\n" +
                        "    Quantity INT(6) NOT NULL DEFAULT 0,\n" +
                        "    PRIMARY KEY (OrderID, ProductID)\n" +
                        ")ENGINE = InnoDB DEFAULT CHARSET=utf8;";

        //执行创建表语句
        TableCrud.CreateTable(createProduct);
        TableCrud.CreateTable(createOrder);
        TableCrud.CreateTable(createOrderProduct);
//----------------------------------------------------
        //插入产品数据
        TableCrud.InsertProduct("1","A","10.00");
        TableCrud.InsertProduct("2","B","20.00");
        TableCrud.InsertProduct("3","C","30.00");
        TableCrud.InsertProduct("4","D","40.00");
        TableCrud.InsertProduct("5","E","50.00");
//-----------------------------------------------------
        //插入订单数据
        //第一个参数为订单ID，第二个参数为订单日期，第二个参数以后我用来可变参数传入订单商品数据
        //这个可变参数的形式为：ProductID,Quantity,ProductID,Quantity,ProductID,Quantity.......
        TableCrud.InsertOrder("100","2022-12-31","3","2");
        TableCrud.InsertOrder("101","2021-01-02","2","1","4","3");
        TableCrud.InsertOrder("102","2021-01-03","1","3","5","4","4","1");
        TableCrud.InsertOrder("103","2021-01-04","5","2","2","6","3","7","4","5");
        //重复插入订单数据，会提示订单已存在，插入失败
        //插入错误的日期格式，插入失败
        //插入不存在的产品，插入失败
        //插入不合法的数量，插入失败
        TableCrud.InsertOrder("100","2022-55-39","-5","-5");
//-----------------------------------------------------
        //插入订单商品数据，
        //第一个参数为订单ID，第二个参数为产品ID，第三个参数为数量
        TableCrud.InsertOrderProduct("100","2","2");
        //重复插入订单商品数据，会提示订单商品已存在，插入失败
        TableCrud.InsertOrderProduct("100","2","1");
        TableCrud.InsertOrderProduct("100","3","1");
        TableCrud.InsertOrderProduct("101","2","1");
        TableCrud.InsertOrderProduct("101","3","2");
        TableCrud.InsertOrderProduct("102","1","1");
        TableCrud.InsertOrderProduct("102","2","2");
//-----------------------------------------------------
        //删除订单中某个商品数据，第一个参数为订单ID，第二个参数为产品ID
        TableCrud.DeleteOrderProduct("100","1");
        TableCrud.DeleteOrderProduct("102","2");

        //删除整个订单数据，第一个参数为订单ID
        TableCrud.DeleteOrder("100");

        //删除产品，随带删除订单内相关商品数据，更新订单总价
        TableCrud.DeleteProduct("2");
        //删除不存在的产品，提示不存在
        TableCrud.DeleteProduct("-5");
//-----------------------------------------------------
        //通过产品ID查询产品数据
        TableCrud.QueryProductByID("1");
        //通过产品ID查询不存在产品数据，提示不存在
        TableCrud.QueryProductByID("-5");

        //通过产品名字查询产品数据，支持模糊查询
        //第一个参数为产品名，第二个参数为匹配规则，0代表完全匹配，1代表模糊匹配
        TableCrud.QueryProductByName("A",1);

        //查询存在订单数据
        TableCrud.QueryOrderByID("101");
        //查询不存在订单数据,提示不存在
        TableCrud.QueryOrderByID("-5");

//-----------------------------------------------------
        //商品排序（价格、名字）
        //sortRule==0,代表不排序
        //sortRule==1代表按照价格排序，sortRule==2代表按照名字排序
        //descOrAsc==1代表降序，descOrAsc==2代表升序
        TableCrud.SortProductAndDisplay(2,2);

        //订单排序（日期、总价)
        //sortRule==1代表按照价格排序，sortRule==2代表按照日期排序
        //descOrAsc==1代表降序，descOrAsc==2代表升序
        TableCrud.SortOrderAndDisplay(1,2);
//-----------------------------------------------------
        //更新产品数据,第一个参数为产品ID，第二个参数为新产品名，第三个参数为新产品价格
        //更新成功后，会用新数据更新订单总价
        TableCrud.UpdateProduct("1","A1","10.00");
        //更新不存在产品数据，提示不存在
        TableCrud.UpdateProduct("-5","A1","10.00");
        //更新产品价格小于等于0，提示价格错误
        TableCrud.UpdateProduct("1","A1","0.00");

        //更新订单数据，第一个参数为订单ID，第二个参数为新订单日期，第三个参数以后为可变参数传入新的订单商品数据
        //这个可变参数的形式为：ProductID,Quantity,ProductID,Quantity,ProductID,Quantity.......
        //更新成功后，会用新数据更新订单总价
        TableCrud.UpdateOrder("101","2021-01-02","5","1","4","3");
        //若更新订单的数据包含:不存在的订单号，错误的日期格式，不存在的产品，不合法的数量，更新失败
        TableCrud.UpdateOrder("-5","2021-25-60","-2","-1","4","3");

//-----------------------------------------------------

        //我在之前的所有方法每次执行结束后都及时释放了相关资源，这里只进行关闭数据库连接
        CreateJdbcUtilObject.jdbcUtil.getConnection().close();
    }
}
