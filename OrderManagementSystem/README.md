### 文件：`JdbcUtil.java`

#### 包声明与导入

- **包**：`com.orderManagement`
- **导入类**：
  - `java.sql.*`：用于数据库操作的SQL相关类

#### 类定义

- **类名**：`JdbcUtil`
- **成员变量**：
  - `private static final String url`：数据库连接URL，包含数据库类型、主机地址、端口和数据库名称等信息。
  - `private static final String username`：数据库用户名。
  - `private static final String password`：数据库密码。
  - `static Connection conn`：静态的数据库连接对象。
  - `static PreparedStatement preStatement`：静态的预编译SQL语句对象。

#### 方法概述

1. **启动数据库连接 (`startConnection`)**
   
   - **功能**：建立与数据库的连接。
   - **参数**：无。
   - **返回值**：无。
   - **异常**：可能抛出 `SQLException` 异常。
   - **说明**：使用 `DriverManager.getConnection` 方法根据提供的URL、用户名和密码建立数据库连接，并将连接对象赋值给静态变量 `conn`。

2. **执行更新操作 (`executeUpdate`)**
   
   - **功能**：执行SQL更新命令（增删改），并返回受影响的行数。
   - **参数**：
     - `String sql`：要执行的SQL语句。
     - `Object... params`：可变参数列表，用于设置SQL语句中的占位符（?）的值。
   - **返回值**：受影响的行数。
   - **异常**：可能抛出 `SQLException` 异常。
   - **说明**：
     - 创建 `PreparedStatement` 对象，使用预编译SQL语句防止SQL注入攻击。
     - 使用 `setObject` 方法为SQL语句中的每个占位符设置值。

3. **执行查询操作 (`executeQuery`)**
   
   - **功能**：执行SQL查询命令，并返回查询结果集。
   - **参数**：
     - `String sql`：要执行的SQL语句。
     - `Object... params`：可变参数列表，用于设置SQL语句中的占位符（?）的值。
   - **返回值**：查询结果集（`ResultSet`）。
   - **异常**：可能抛出 `SQLException` 异常。
   - **说明**：
     - 创建 `PreparedStatement` 对象，使用预编译SQL语句防止SQL注入攻击。
     - 使用 `setObject` 方法为SQL语句中的每个占位符设置值。

### 

### 文件：`ProductCrud.java`

#### 包声明与导入

- **包声明**：`package com.orderManagement;`
- **导入的类**：
  - `java.sql.ResultSet`
  - `java.sql.SQLException`

#### 类定义

- **类名**：`ProductCrud`
- **静态成员变量**：
  - `static StringBuilder sb = new StringBuilder();`：用于拼接查询结果。

#### 方法概述

1. **插入产品信息 (`insertProduct`)**
   
   - **功能**：向数据库中的 `Product` 表插入新产品信息。
   - **参数**：
     - `String ProductID`：产品ID。
     - `String ProductName`：产品名称。
     - `String ProductPrice`：产品价格。
   - **逻辑**：
     - 检查价格和ID是否合法（大于0）。
     - 检查是否有重复的 `ProductID`。
     - 插入成功则输出成功信息，否则输出失败原因。

2. **删除产品信息 (`deleteProduct`)**
   
   - **功能**：从 `Product` 表中删除指定的产品信息，并同时删除所有相关订单中的该产品信息。
   - **参数**：
     - `String ProductID`：要删除的产品ID。
   - **逻辑**：
     - 删除 `Product` 表中的记录。
     - 查找并删除 `OrderProduct` 表中对应的记录。
     - 如果某个订单没有其他商品，则删除该订单。
     - 更新受影响订单的总价。
     - 输出操作结果。

3. **更新产品信息 (`updateProduct`)**
   
   - **功能**：更新 `Product` 表中的产品信息，并更新所有相关订单的总价。
   - **参数**：
     - `String ProductID`：要更新的产品ID。
     - `String ProductNewName`：新的产品名称。
     - `String ProductNewPrice`：新的产品价格。
   - **逻辑**：
     - 检查新价格是否合法。
     - 更新 `Product` 表中的记录。
     - 更新包含该产品的订单的总价。
     - 输出操作结果。

4. **按ID查询产品信息 (`queryProductByID`)**
   
   - **功能**：根据产品ID查询产品信息。
   - **参数**：
     - `String ProductID`：要查询的产品ID。
   - **逻辑**：
     - 执行查询并调用 `printProductInfo` 方法打印结果。

5. **按名称查询产品信息 (`queryProductByName`)**
   
   - **功能**：根据产品名称查询产品信息，支持完全匹配和模糊匹配。
   - **参数**：
     - `String ProductName`：要查询的产品名称。
     - `int matchRule`：匹配规则（0为完全匹配，1为模糊匹配）。
   - **逻辑**：
     - 构建SQL查询语句并执行。
     - 调用 `printProductInfo` 方法打印结果。

6. **打印查询到的产品信息 (`printProductInfo`)**
   
   - **功能**：格式化并打印查询到的产品信息。
   - **参数**：
     - `ResultSet resultSet`：查询结果集。
   - **逻辑**：
     - 遍历结果集并打印每条记录。

7. **检查产品存在或重复订单 (`checkProductExistOrRepeatedOrder`)**
   
   - **功能**：检查订单ID是否重复以及产品ID是否存在且数量是否合法。
   - **参数**：
     - `String OrderID`：订单ID。
     - `String... ProductIDAndQuantity`：产品ID及其数量。
   - **逻辑**：
     - 检查订单ID是否已存在。
     - 检查每个产品ID是否存在且数量合法。
     - 返回检查结果标志。

8. **排序并显示产品信息 (`sortProductAndDisplay`)**
   
   - **功能**：根据指定规则对产品进行排序并显示。
   - **参数**：
     - `int sortRule`：排序规则（1为按价格，2为按名称）。
     - `int descOrAsc`：排序顺序（1为降序，2为升序）。
   - **逻辑**：
     - 构建SQL查询语句并执行。
     - 打印排序后的结果。



### 文件：`OrdersCrud.java`

#### 包声明与导入

- **包**：`com.orderManagement`
- **导入类**：
  - `java.sql.ResultSet`
  - `java.sql.SQLException`

#### 类定义

- **类名**：`OrdersCrud`
- **静态成员变量**：
  - `static StringBuilder sb = new StringBuilder();`：用于拼接查询结果。

#### 方法概述

1. **插入订单信息 (`insertOrder`)**
   
   - **功能**：向数据库的 `Orders` 表中插入订单信息，并同时在 `OrderProduct` 表中插入订单包含的商品及其数量。
   - **参数**：
     - `String OrderID`：订单编号。
     - `String OrderDate`：订单日期。
     - `String... ProductIDAndQuantity`：可变参数，表示商品编号和数量（如："101", "2", "102", "1"）。
   - **逻辑**：
     - 检查是否有重复的订单编号、不存在的商品编号或不合法的商品数量。
     - 检查日期格式是否正确。
     - 插入订单记录到 `Orders` 表。
     - 插入订单商品记录到 `OrderProduct` 表。
     - 计算并更新订单总价。

2. **删除订单信息 (`deleteOrder`)**
   
   - **功能**：从数据库的 `Orders` 和 `OrderProduct` 表中删除指定订单的所有信息。
   - **参数**：
     - `String OrderID`：订单编号。
   - **逻辑**：
     - 删除 `Orders` 表中的订单记录。
     - 删除 `OrderProduct` 表中的相关商品记录。

3. **更新订单信息 (`updateOrder`)**
   
   - **功能**：更新数据库中指定订单的信息，包括订单日期和商品数量。
   - **参数**：
     - `String OrderID`：订单编号。
     - `String NewOrderDate`：新的订单日期。
     - `String... newProductIDAndQuantity`：可变参数，表示新的商品编号和数量。
   - **逻辑**：
     - 检查订单是否存在、商品编号是否正确、商品数量是否合法以及日期格式是否正确。
     - 更新 `OrderProduct` 表中的商品记录。
     - 更新 `Orders` 表中的订单日期和总价。

4. **查询订单信息 (`queryOrder`)**
   
   - **功能**：从数据库的 `Orders` 表中查询指定订单的基本信息（订单号、订单日期、总价）。
   - **参数**：
     - `String OrderID`：订单编号。
   - **返回值**：订单信息字符串，如果未找到则返回 `null`。

5. **查询单个订单的详细信息 (`querySingleOrderID`)**
   
   - **功能**：查询并打印指定订单的详细信息，包括订单号、订单日期、总价和商品信息。
   - **参数**：
     - `String OrderID`：订单编号。

6. **排序并显示订单信息 (`sortOrderAndDisplay`)**
   
   - **功能**：根据指定规则对订单进行排序并显示所有订单信息。
   - **参数**：
     - `int sortRule`：排序规则（0：不排序，1：按价格排序，2：按下单时间排序）。
     - `int descOrAsc`：排序顺序（1：降序，2：升序）。
   - **逻辑**：
     - 根据排序规则构建 SQL 查询语句。
     - 执行查询并打印所有订单信息及其包含的商品。

7. **更新订单总价 (`updateOrdersTotalPrice`)**
   
   - **功能**：根据订单中的商品信息更新订单的总价。
   - **参数**：
     - `String OrderID`：订单编号。
   - **逻辑**：
     - 使用子查询计算订单总价并更新 `Orders` 表中的 `TotalPrice` 字段。



### 文件：`OrderProductCrud.java`

#### 包声明与导入

- **包**：`com.orderManagement`
- **导入类**：
  - `java.sql.ResultSet`
  - `java.sql.SQLException`

#### 类定义

- **类名**：`OrderProductCrud`
- **静态成员变量**：
  - `static StringBuilder sb = new StringBuilder();`：用于拼接查询结果。

#### 表设计说明

`OrderProduct` 表是创建的一个额外的关联表，用来表示一个订单中存在的多个商品之间的关系。表结构如下：

- **OrderID**（作为外键关联到 `Orders` 表）
- **ProductID**（作为外键关联到 `Product` 表）
- **Quantity**（商品数量）

示例数据：
| OrderID | ProductID | Quantity |
|---------|-----------|----------|
| 1        | 101      | 2         |
| 1        | 102      | 1         |
| 2        | 101      | 1         |
| 2        | 103      | 3         |

每个订单可以有多个商品，因此对这个表的增删改查操作相当于对订单包含的商品进行增删改查操作。

#### 方法概述

1. **插入订单商品记录 (`insertOrderProduct`)**
   
   - **功能**：往数据库 `OrderProduct` 表中插入一条记录，相当于向订单中添加一个商品。
   - **参数**：
     - `String OrderID`：订单ID
     - `String ProductID`：商品ID
     - `String Quantity`：商品数量
   - **逻辑**：
     - 检查 `Orders` 表中是否存在该订单。
     - 检查 `Product` 表中是否存在该商品。
     - 检查该商品是否已经在订单中存在。
     - 如果以上条件都满足，则插入新记录，并更新 `Orders` 表中的总价。
   - **输出**：成功或失败信息。

2. **删除订单商品记录 (`deleteOrderProduct`)**
   
   - **功能**：从数据库 `OrderProduct` 表中删除一条记录，相当于从订单中删除一个商品。
   - **参数**：
     - `String OrderID`：订单ID
     - `String ProductID`：商品ID
   - **逻辑**：
     - 删除 `OrderProduct` 表中的记录。
     - 如果删除了最后一条记录，则删除 `Orders` 表中的订单记录。
     - 如果订单中还有其他商品，重新计算订单总价。
   - **输出**：成功或失败信息。

3. **删除所有相同订单ID的记录 (`deleteAllSameOrderID`)**
   
   - **功能**：把 `OrderProduct` 表中所有含有相同订单ID的记录都删除，以便更好地进行订单更新。
   - **参数**：
     - `String OrderID`：订单ID
   - **逻辑**：
     - 删除 `OrderProduct` 表中所有匹配的记录。

4. **查询订单商品信息 (`queryOrderProduct`)**
   
   - **功能**：从数据库 `OrderProduct` 表中查询某个订单号的所有商品信息。
   - **参数**：
     - `String OrderID`：订单ID
   - **逻辑**：
     - 使用 SQL 查询语句，通过内连接获取商品名称和数量。
     - 将查询结果拼接成字符串返回。
   - **返回值**：包含商品信息的字符串。



### 文件：`CheckDateFormat.java`

#### 包声明与导入

- **包**：`com.orderManagement`

#### 类定义

- **类名**：`CheckDateFormat`

#### 方法概述

- **方法名**：`isValidDate(String date)`
  - **功能描述**：
    - 检查给定的日期字符串是否符合特定格式和有效范围。
  - **具体步骤**：
    1. **字符检查**：遍历日期字符串中的每个字符，确保它们只包含数字和连字符（`-`）。如果遇到其他字符，则返回 `false`。
    2. **分隔符检查**：使用连字符（`-`）将日期字符串分割成三部分（年、月、日），并检查分割后的数组长度是否为3。如果不是，则返回 `false`。
    3. **类型转换与验证**：
       - 将分割后的年、月、日部分分别转换为 `double` 和 `int` 类型。
       - 检查年、月、日是否为整数（即没有小数部分）。如果有小数部分，则返回 `false`。
    4. **范围验证**：
       - 年份必须在1900到2100之间。
       - 月份必须在1到12之间。
       - 日期必须在1到31之间。
  - **返回值**：
    - 如果所有检查都通过，则返回 `true`，表示日期格式有效；否则返回 `false`。



- 该方法仅对日期的基本格式和范围进行了简单验证，但未考虑每个月的具体天数（例如2月最多29天或30天等特殊情况）。



### 文件：`TableCrud.java`

#### 包声明与导入

- **包**：`com.orderManagement`
- **导入类**：`java.sql.SQLException`

#### 类定义

- **类名**：`TableCrud`
- **功能描述**：该类用于对 `Orders`、`Product` 和 `OrderProduct` 三个表进行创建、插入、删除、更新、查询和排序等操作。每个方法都包含事务处理，以确保数据的一致性。

#### 方法概述

1. **创建表 (`CreateTable`)**
   
   - **功能**：执行创建表的SQL语句。
   - **参数**：
     - `String tableSql`：创建表的SQL语句。
   - **事务处理**：开启事务，执行SQL语句后提交事务；若发生异常则回滚事务并抛出异常。

2. **插入产品数据 (`InsertProduct`)**
   
   - **功能**：调用 `ProductCrud.insertProduct` 方法插入产品数据。
   - **参数**：
     - `String ProductID`：产品ID。
     - `String ProductName`：产品名称。
     - `String ProductPrice`：产品价格。
   - **事务处理**：同上。

3. **插入订单数据 (`InsertOrder`)**
   
   - **功能**：调用 `OrdersCrud.insertOrder` 方法插入订单数据。
   - **参数**：
     - `String OrderID`：订单ID。
     - `String OrderDate`：订单日期。
     - `String... ProductIDAndQuantity`：可变参数，表示产品ID和数量。
   - **事务处理**：同上。

4. **插入订单商品数据 (`InsertOrderProduct`)**
   
   - **功能**：调用 `OrderProductCrud.insertOrderProduct` 方法插入订单商品数据。
   - **参数**：
     - `String OrderID`：订单ID。
     - `String ProductID`：产品ID。
     - `String Quantity`：数量。
   - **事务处理**：同上。

5. **删除订单中某个商品数据 (`DeleteOrderProduct`)**
   
   - **功能**：调用 `OrderProductCrud.deleteOrderProduct` 方法删除订单中的某个商品数据。
   - **参数**：
     - `String OrderID`：订单ID。
     - `String ProductID`：产品ID。
   - **事务处理**：同上。

6. **删除整个订单数据 (`DeleteOrder`)**
   
   - **功能**：调用 `OrdersCrud.deleteOrder` 方法删除整个订单数据。
   - **参数**：
     - `String OrderID`：订单ID。
   - **事务处理**：同上。

7. **删除产品 (`DeleteProduct`)**
   
   - **功能**：调用 `ProductCrud.deleteProduct` 方法删除产品，并随带删除订单内相关商品数据，更新订单总价。
   - **参数**：
     - `String ProductID`：产品ID。
   - **事务处理**：同上。

8. **查询存在产品数据 (`QueryProductByID`)**
   
   - **功能**：调用 `ProductCrud.queryProductByID` 方法查询指定ID的产品数据。
   - **参数**：
     - `String ProductID`：产品ID。
   - **事务处理**：同上。

9. **查询存在订单数据 (`QueryOrderByID`)**
   
   - **功能**：调用 `OrdersCrud.querySingleOrderID` 方法查询指定ID的订单数据。
   - **参数**：
     - `String OrderID`：订单ID。
   - **事务处理**：同上。

10. **商品排序并显示 (`SortProductAndDisplay`)**
    
    - **功能**：调用 `ProductCrud.sortProductAndDisplay` 方法对商品进行排序并显示。
    - **参数**：
      - `int sortRule`：排序规则（0代表不排序，1代表按价格排序，2代表按名字排序）。
      - `int descOrAsc`：排序顺序（1代表降序，2代表升序）。
    - **事务处理**：同上。

11. **订单排序并显示 (`SortOrderAndDisplay`)**
    
    - **功能**：调用 `OrdersCrud.sortOrderAndDisplay` 方法对订单进行排序并显示。
    - **参数**：
      - `int sortRule`：排序规则（1代表按价格排序，2代表按日期排序）。
      - `int descOrAsc`：排序顺序（1代表降序，2代表升序）。
    - **事务处理**：同上。

12. **更新产品数据 (`UpdateProduct`)**
    
    - **功能**：调用 `ProductCrud.updateProduct` 方法更新产品数据。
    - **参数**：
      - `String ProductID`：产品ID。
      - `String ProductNewName`：新产品名称。
      - `String ProductNewPrice`：新产品价格。
    - **事务处理**：同上。

13. **更新订单数据 (`UpdateOrder`)**
    
    - **功能**：调用 `OrdersCrud.updateOrder` 方法更新订单数据。
    - **参数**：
      - `String OrderID`：订单ID。
      - `String NewOrderDate`：新订单日期。
      - `String... newProductIDAndQuantity`：可变参数，表示新的产品ID和数量。
    - **事务处理**：同上。

14. **通过产品名字查询产品数据 (`QueryProductByName`)**
    
    - **功能**：调用 `ProductCrud.queryProductByName` 方法通过产品名字查询产品数据，支持模糊查询。
    - **参数**：
      - `String ProductName`：产品名称。
      - `int matchRule`：匹配规则（0代表完全匹配，1代表模糊匹配）。
    - **事务处理**：同上。



- 每个方法都使用了事务管理，确保在数据库操作失败时能够回滚，保证数据一致性。
- 依赖于 `JdbcUtil` 类提供的数据库连接和SQL执行功能。