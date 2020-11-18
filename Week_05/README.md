# 第五周框架

> 老师你好，上周作业我以我对并发编程的总结作为作业内容，但作业被标注为未提交，可能是老师误以为是我拷贝别人的文章，下面是我的作业与原文地址，麻烦老师核实一下。 
> 作业 ：https://github.com/zhengyin/JAVA-000/tree/main/Week_04
> 原文 ：https://github.com/zhengyin/java-concurrent
> 另外本着学习为目的，不是完成作业为目的的原则，可能我的作业和老师布置的会有出入，但都是符合课程内容的，请老师见谅，如有不妥之处请指正。 

## 注解配置Bean与刷新


> 一个数据库连接池配置的例子

``` 
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Mybatis datasource 配置
 * @author zhengyin zhengyinit@outlook.com .
 */
@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = "xxx.mapper", sqlSessionFactoryRef = GatewayMasterDataSourceConfiguration.SESSION_FACTORY)
public class GatewayMasterDataSourceConfiguration implements EnvironmentAware {
    private final static String DB_NAME = "xxx.master";
    public final static String SESSION_FACTORY = "gatewaySqlSessionFactory";
    public final static String DATASOURCE = "gatewayDataSource";
    public final static String TRANSACTION_MANAGER = "gatewayTransactionManager";

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Primary
    @Qualifier(DATASOURCE)
    @Bean(name = DATASOURCE)
    public DataSource masterDataSource(){
        return DatasourceConfigFactory.createDatasource(environment,"datasource."+DB_NAME);
    }


    @Primary
    @Qualifier(SESSION_FACTORY)
    @Bean(name = SESSION_FACTORY)
    public SqlSessionFactoryBean masterSqlSessionFactory(){
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setVfs(SpringBootVFS.class);
        factoryBean.setDataSource(masterDataSource());
        return factoryBean;
    }

    @Bean(name = TRANSACTION_MANAGER)
    public DataSourceTransactionManager transactionManager(){
        return new DataSourceTransactionManager(masterDataSource());
    }
}

```

``` 
import com.alibaba.fastjson.JSON;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import javax.sql.DataSource;
import java.util.Objects;
import java.util.Optional;

/**
 * @author zhengyin zhengyinit@outlook.com
 */
@Slf4j
public class DatasourceConfigFactory {

    /**
     * 创建一个数据源
     * @param environment
     * @param dbName
     * return
     */
    public static DataSource createDatasource(Environment environment , final String dbName){
        final String driverClassName = environment.getProperty(dbName + ".driver-class-name");
        final String url = environment.getProperty(dbName + ".jdbc-url");
        final String username = environment.getProperty(dbName + ".username");
        final String password = environment.getProperty(dbName + ".password");
        final String maxActive = environment.getProperty(dbName + ".maximum-pool-size");
        final String minIdle = environment.getProperty(dbName + ".minimum-idle");
        final Long idleTimeout = Optional.ofNullable(environment.getProperty(dbName + ".idle-timeout")).map(Long::parseLong).orElse(60000L);
        final Long maxLifeTime = Optional.ofNullable(environment.getProperty(dbName + ".max-life-time")).map(Long::parseLong).orElse(110000L);

        Objects.requireNonNull(driverClassName);
        Objects.requireNonNull(url);
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        Objects.requireNonNull(maxActive);
        Objects.requireNonNull(minIdle);
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverClassName);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(Integer.parseInt(maxActive));
        config.setMinimumIdle(Integer.parseInt(minIdle));
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifeTime);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        HikariDataSource dataSource = new HikariDataSource(config);
        log.info(dbName+" Instanced {} , driverClassName {} , url {}  , maxActive {} , minIdle {} " ,
                dataSource.toString(),driverClassName,url,maxActive,minIdle
        );
        log.info("HikariConfig ["+dbName+"] -> "+ JSON.toJSONString(config).replace(password,"***").replace(username,"***"));
        return dataSource;
    }
}

```

## Spring-boot-cache-extend Spring-boot缓存扩展

https://github.com/zhengyin/spring-boot-cache-extend

> spring-boot-cache-extend  是对spring-cache的包装，提供在spring-boot中使用redis与caffeine缓存开箱即用的功能。

1. 统一的cache key 生成器

> 统一的cache key 有助于我们规范的管理缓存的key，特别是在neibu环境中共用同一redis时，避免缓存key重复:

缓存key命名规范
``` 
    key = cacheName :: application : targetClass . targetMethod : params
```

``` 
    如 : TTL_5::spring-boot-example-cache:TestController.hello:visitor 
```

2. @CacheTarget 注解

> @CacheTarget 是用于定义目标的缓存类，便于在清除缓存时可以随时在别处进行清除

3. 兼容所有的 spring-cache 功能

4. 缓存使用示例

https://github.com/zhengyin/spring-boot-example/blob/master/spring-boot-example-cache/src/test/java/com/izhengyin/springboot/example/cache/test/ApplicationTests.java

## 注意事项

### 1. 使用  @Cacheable(key = "#name") 自定义缓存key时不会使用统一的缓存生成器, 参考示例 customKeyTest

### 2. 使用  @Cacheable(keyGenerator = CacheKeyGeneratorConfig.MY_KEY_GENERATOR ) 自定义缓存key生成器时不会使用统一的缓存生成器, 参考示例 customKeyGeneratorTest

### 3. 由于 Aop 的特性，在类中使用 this 调用方法是不会触发 Aop 增强，因此缓存注解不会生效 . 参考此文章 https://www.ibm.com/developerworks/cn/opensource/os-cn-spring-cache/

