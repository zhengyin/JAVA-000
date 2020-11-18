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
