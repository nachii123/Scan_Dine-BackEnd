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
        basePackages            = "com.example.scan_dineCustomer.restaurant.repository",
        entityManagerFactoryRef = "restaurantEntityManagerFactory",
        transactionManagerRef   = "restaurantTransactionManager"
)
public class RestaurantDataSourceConfig {

    @Value("${spring.datasource.restaurant-ds.url}")
    private String url;

    @Value("${spring.datasource.restaurant-ds.username}")
    private String username;

    @Value("${spring.datasource.restaurant-ds.password}")
    private String password;

    @Value("${spring.datasource.restaurant-ds.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.restaurant-ds.maximum-pool-size:5}")
    private int maxPoolSize;

    @Value("${spring.datasource.restaurant-ds.minimum-idle:2}")
    private int minIdle;

    @Value("${spring.datasource.restaurant-ds.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${spring.datasource.restaurant-ds.pool-name:RestaurantPool}")
    private String poolName;

    @Bean(name = "restaurantDataSource")
    public DataSource restaurantDataSource() {
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

    @Bean(name = "restaurantEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean restaurantEntityManagerFactory(
            @Qualifier("restaurantDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean factory =
                new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.example.scan_dineCustomer.restaurant.entity");
        factory.setPersistenceUnitName("restaurantPU");

        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setGenerateDdl(true);
        factory.setJpaVendorAdapter(adapter);
        factory.setJpaPropertyMap(jpaProperties("restaurants"));
        return factory;
    }

    @Bean(name = "restaurantTransactionManager")
    public PlatformTransactionManager restaurantTransactionManager(
            @Qualifier("restaurantEntityManagerFactory")
            EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    private Map<String, Object> jpaProperties(String schema) {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto",        "update");
        props.put("hibernate.show_sql",             "false");
        props.put("hibernate.format_sql",           "true");
        props.put("hibernate.default_schema",       "restaurants");
        props.put("hibernate.dialect",              "org.hibernate.dialect.PostgreSQLDialect");
        return props;
    }
}
