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

## (必做)基于hmily TCC或ShardingSphere的Atomikos XA实现一个简单的分布式 事务应用demo(二选一)，提交到github。

### 单独测试 Atomikos 事务
 

#### 数据准备

1. 创建 orders 库 , 导入表
``` 
CREATE TABLE `t_order` (
  `order_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `seller_id` int unsigned NOT NULL,
  `item_id` int unsigned NOT NULL DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单最后修改时间',
  PRIMARY KEY (`order_id`),
  KEY `item_id` (`item_id`)
) ENGINE=InnoDB
```
2. 创建 inventory 库 , 导入表

``` 
CREATE TABLE `t_inventory` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `item_id` int unsigned NOT NULL DEFAULT '0',
  `amount` int unsigned NOT NULL DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单最后修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB

```
3. 配置数据源

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

#### 运行测试

```
    @Test
    public void count(){
        Assert.assertEquals(orderMapper.getList(0,10).size(),9);
        Assert.assertEquals(inventoryMapper.getList(0,10).size(),9);
    }

    @Test
    public void orderOnlyTransaction(){
        Assert.assertThrows(RuntimeException.class, () -> orderService.orderOnlyTransaction(1));
        Assert.assertNotEquals(orderMapper.countByItemId(1),0);
    }

    @Test
    public void inventoryOnlyTransaction() {
        Assert.assertThrows(RuntimeException.class, () -> orderService.inventoryOnlyTransaction(1));
        Assert.assertNotEquals(inventoryMapper.countByItemId(1),0);
    }

    @Test
    public void createOrder() {
        int itemId = 1;
        int orderNum = orderMapper.countByItemId(1);

        orderService.createOrder(itemId);
        Assert.assertEquals(2,inventoryMapper.getAmount(itemId));
        Assert.assertEquals(++orderNum ,orderMapper.countByItemId(itemId));

        orderService.createOrder(itemId);
        Assert.assertEquals(1,inventoryMapper.getAmount(itemId));
        Assert.assertEquals(++orderNum ,orderMapper.countByItemId(itemId));


        orderService.createOrder(itemId);
        Assert.assertEquals(0,inventoryMapper.getAmount(itemId));
        Assert.assertEquals(++orderNum ,orderMapper.countByItemId(itemId));

        Assert.assertThrows(RuntimeException.class, () -> orderService.createOrder(itemId));
        Assert.assertEquals(orderNum,orderMapper.countByItemId(itemId));
    }

```

#### 备注 Atomikos 默认配置
``` 
com.atomikos.icatch.oltp_max_retries = 5
com.atomikos.icatch.log_base_dir = ./
com.atomikos.icatch.tm_unique_name = 192.168.102.102.tm
com.atomikos.icatch.default_jta_timeout = 10000
com.atomikos.icatch.serial_jta_transactions = true
com.atomikos.icatch.allow_subtransactions = true
com.atomikos.icatch.automatic_resource_registration = true
java.naming.factory.initial = com.sun.jndi.rmi.registry.RegistryContextFactory
com.atomikos.icatch.log_base_name = tmlog
com.atomikos.icatch.oltp_retry_interval = 10000
java.naming.provider.url = rmi://localhost:1099
com.atomikos.icatch.checkpoint_interval = 500
com.atomikos.icatch.default_max_wait_time_on_shutdown = 9223372036854775807
com.atomikos.icatch.client_demarcation = false
com.atomikos.icatch.forget_orphaned_log_entries_delay = 86400000
com.atomikos.icatch.trust_client_tm = false
com.atomikos.icatch.force_shutdown_on_vm_exit = false
com.atomikos.icatch.rmi_export_class = none
com.atomikos.icatch.enable_logging = true
com.atomikos.icatch.max_timeout = 300000
com.atomikos.icatch.threaded_2pc = false
com.atomikos.icatch.recovery_delay = 10000
com.atomikos.icatch.max_actives = 50
```


源码地址： https://github.com/zhengyin/spring-boot-example/tree/master/spring-boot-example-atomikos


### ShardingSphere XA 事务 (待解决) 请问老师，这部分有没有什么参考资料？

> 抛出异常后事务没有回滚，单独测试atomikos是OK的，怀疑是数据源 Datasource 需要配置未 XaDatasource，但是还未找到shardingsphere的如何配置XaDatasource的API。


``` 
<!-- 使用 XA 事务时，需要引入此模块 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-xa-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

``` 
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean transaction(){
        //db 1 , table 1
        orderMapper.del(1,1);

        //db 0 , table 2
        if(orderMapper.del(2,2) > 0){
            //模拟抛出异常
            throw new RuntimeException("Throw Exception!");
        }
        return true;
    }
```

源码地址： https://github.com/zhengyin/spring-boot-example/tree/master/spring-boot-example-shardingsphere-jdbc
