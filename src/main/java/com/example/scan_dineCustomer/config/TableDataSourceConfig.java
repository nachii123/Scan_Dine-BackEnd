package com.example.scan_dineCustomer.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages            = "com.example.scan_dineCustomer.table.repository",
        entityManagerFactoryRef = "tableEntityManagerFactory",
        transactionManagerRef   = "tableTransactionManager"
)
public class TableDataSourceConfig {

    @Value("${spring.datasource.table-ds.url}")
    private String url;

    @Value("${spring.datasource.table-ds.username}")
    private String username;

    @Value("${spring.datasource.table-ds.password}")
    private String password;

    @Value("${spring.datasource.table-ds.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.table-ds.maximum-pool-size:5}")
    private int maxPoolSize;

    @Value("${spring.datasource.table-ds.minimum-idle:2}")
    private int minIdle;

    @Value("${spring.datasource.table-ds.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${spring.datasource.table-ds.pool-name:TablePool}")
    private String poolName;

    @Bean(name = "tableDataSource")
    public DataSource tableDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName(driverClassName);
        ds.setMaximumPoolSize(maxPoolSize);
        ds.setMinimumIdle(minIdle);
        ds.setConnectionTimeout(connectionTimeout);
        ds.setPoolName(poolName);
        return ds;
    }

    @Bean(name = "tableEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean tableEntityManagerFactory(
            @Qualifier("tableDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.example.scan_dineCustomer.table.entity");
        factory.setPersistenceUnitName("tablePU");

        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setGenerateDdl(true);
        factory.setJpaVendorAdapter(adapter);
        factory.setJpaPropertyMap(jpaProperties());
        return factory;
    }

    @Bean(name = "tableTransactionManager")
    public PlatformTransactionManager tableTransactionManager(
            @Qualifier("tableEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    private Map<String, Object> jpaProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto",   "update");
        props.put("hibernate.show_sql",        "false");
        props.put("hibernate.format_sql",      "true");
        props.put("hibernate.default_schema",  "tables");
        return props;
    }
}
