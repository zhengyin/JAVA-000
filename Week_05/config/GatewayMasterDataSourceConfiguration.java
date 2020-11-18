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
        return DatasourceFactory.createDatasource(environment,"datasource."+DB_NAME);
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
