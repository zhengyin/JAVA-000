## (必做)设计对前面的订单表数据进行水平分库分表，拆分2个库，每个库16张表。 并在新结构在演示常见的增删改查操作。代码、sql 和配置文件，上传到 Github。

> 数据库分库分表，并使用Shardingsphere-jdbc进行数据操作
> 注：为了手动验证数据方便，只配置了3张表

### 1. 创建库 order_1 \ order_2 导入下面的表



``` 
DROP TABLE IF EXISTS t_order_0;
CREATE TABLE `t_order_0` (
  `order_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `seller_id` int(11) unsigned NOT NULL,
  `buyer_id` int(11) unsigned NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单最后修改时间',
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';


DROP TABLE IF EXISTS t_order_1;
CREATE TABLE `t_order_1` (
  `order_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `seller_id` int(11) unsigned NOT NULL,
  `buyer_id` int(11) unsigned NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单最后修改时间',
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';


DROP TABLE IF EXISTS t_order_2;
CREATE TABLE `t_order_2` (
  `order_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `seller_id` int(11) unsigned NOT NULL,
  `buyer_id` int(11) unsigned NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单最后修改时间',
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

```

### 2. 修改数据库连接配置

``` 
spring.datasource.order_1.driver-class-name = com.mysql.cj.jdbc.Driver
spring.datasource.order_1.jdbc-url=jdbc:mysql://localhost/order_1
spring.datasource.order_1.username=root
spring.datasource.order_1.password=zhengyin

spring.datasource.order_2.driver-class-name = com.mysql.cj.jdbc.Driver
spring.datasource.order_2.jdbc-url=jdbc:mysql://localhost/order_2
spring.datasource.order_2.username=root
spring.datasource.order_2.password=zhengyin
```

### 3. 运行测试

``` 
    @Test
    public void getCount(){
        Assert.assertEquals(orderService.count(),9);
    }

    @Test
    public void getList(){
        List<Order> orderList = orderService.getList(5,2);
        System.out.println(JSON.toJSONString(orderList));
        Assert.assertEquals(orderList.size(),2);
        Assert.assertEquals(orderList.get(0).getOrderId().intValue(),6);
        Assert.assertEquals(orderList.get(1).getOrderId().intValue(),7);
    }

    @Test
    public void delMulti(){
         orderService.delMulti(Arrays.asList(1,2,3,4));
         Assert.assertFalse(orderService.exists(1));
         Assert.assertFalse(orderService.exists(2));
         Assert.assertFalse(orderService.exists(3));
         Assert.assertFalse(orderService.exists(4));
    }
```

源码地址： https://github.com/zhengyin/spring-boot-example/tree/master/spring-boot-example-shardingsphere-jdbc
