package clone.twitter.config;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@PropertySource("classpath:/application.yml")
public class DataSourceConfig {

    @Bean
    @Qualifier(value = "primaryMySqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary-mysql")
    public DataSource primaryMySqlDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Qualifier(value = "replicaMySqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.replica-mysql")
    public DataSource replicaMySqlDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public DataSource routingDataSource(
            @Qualifier(value = "primaryMySqlDataSource") DataSource primaryMySqlDataSource,
            @Qualifier(value = "replicaMySqlDataSource") DataSource replicaMySqlDataSource) {

        Map<Object, Object> targetDataSources = new HashMap<>();

        targetDataSources.put("primary-mysql", primaryMySqlDataSource);
        targetDataSources.put("replica-mysql", replicaMySqlDataSource);

        AbstractRoutingDataSource routingDataSource = new RoutingDataSource();

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(primaryMySqlDataSource);

        return routingDataSource;
    }

    @Bean
    public DataSource lazyConnectionProxyDataSource(
            @Qualifier(value = "routingDataSource") DataSource routingDataSource) {

        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    @Bean
    public PlatformTransactionManager transactionManager(
            @Qualifier(value = "lazyConnectionProxyDataSource") DataSource lazyConnectionProxyDataSource) {

        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();

        transactionManager.setDataSource(lazyConnectionProxyDataSource);

        return transactionManager;
    }
}
