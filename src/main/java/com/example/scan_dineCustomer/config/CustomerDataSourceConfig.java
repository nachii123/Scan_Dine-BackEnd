package com.example.scan_dineCustomer.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages            = {
                "com.example.scan_dineCustomer.repo",
                "com.example.scan_dineCustomer.demo.repository"
        },
        entityManagerFactoryRef = "customerEntityManagerFactory",
        transactionManagerRef   = "customerTransactionManager"
)
public class CustomerDataSourceConfig {

    @Value("${spring.datasource.customer-ds.url}")
    private String url;

    @Value("${spring.datasource.customer-ds.username}")
    private String username;

    @Value("${spring.datasource.customer-ds.password}")
    private String password;

    @Value("${spring.datasource.customer-ds.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.customer-ds.maximum-pool-size:5}")
    private int maxPoolSize;

    @Value("${spring.datasource.customer-ds.minimum-idle:2}")
    private int minIdle;

    @Value("${spring.datasource.customer-ds.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${spring.datasource.customer-ds.pool-name:CustomerPool}")
    private String poolName;

    @Primary
    @Bean(name = "customerDataSource")
    public DataSource customerDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);                    // ← HikariDataSource uses setJdbcUrl()
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName(driverClassName);
        ds.setMaximumPoolSize(maxPoolSize);
        ds.setMinimumIdle(minIdle);
        ds.setConnectionTimeout(connectionTimeout);
        ds.setPoolName(poolName);
        return ds;
    }

    @Primary
    @Bean(name = "customerEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean customerEntityManagerFactory(
            @Qualifier("customerDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean factory =
                new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan(
                "com.example.scan_dineCustomer.entity",
                "com.example.scan_dineCustomer.demo.entity"
        );
        factory.setPersistenceUnitName("customerPU");

        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setGenerateDdl(true);
        factory.setJpaVendorAdapter(adapter);
        factory.setJpaPropertyMap(jpaProperties("customers"));
        return factory;
    }

    @Primary
    @Bean(name = "customerTransactionManager")
    public PlatformTransactionManager customerTransactionManager(
            @Qualifier("customerEntityManagerFactory")
            EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    private Map<String, Object> jpaProperties(String schema) {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto",        "update");
        props.put("hibernate.show_sql",             "false");
        props.put("hibernate.format_sql",           "true");
        props.put("hibernate.default_schema",       schema);
        props.put("hibernate.dialect",              "org.hibernate.dialect.PostgreSQLDialect");
        return props;
    }
}
