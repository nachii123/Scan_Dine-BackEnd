//package com.example.scan_dineCustomer.config;
//
//import com.zaxxer.hikari.HikariDataSource;
//import jakarta.persistence.EntityManagerFactory;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.*;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.orm.jpa.*;
//import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import javax.sql.DataSource;
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//@EnableTransactionManagement
//@EnableJpaRepositories(
//        basePackages = {"com.example.scan_dineCustomer.repo", "com.example.scan_dineCustomer.restaurant.repository"},
//        entityManagerFactoryRef = "entityManagerFactory",
//        transactionManagerRef = "transactionManager"
//)
//public class DataSourceConfig {
//
//    @Value("${spring.datasource.url}")
//    private String url;
//
//    @Value("${spring.datasource.username}")
//    private String username;
//
//    @Value("${spring.datasource.password}")
//    private String password;
//
//    @Value("${spring.datasource.driver-class-name}")
//    private String driverClassName;
//
//    @Value("${spring.datasource.hikari.maximum-pool-size:10}")
//    private int maxPoolSize;
//
//    @Value("${spring.datasource.hikari.minimum-idle:5}")
//    private int minIdle;
//
//    @Value("${spring.datasource.hikari.connection-timeout:30000}")
//    private long connectionTimeout;
//
//    @Value("${spring.datasource.hikari.pool-name:PrimaryPool}")
//    private String poolName;
//
//    @Primary
//    @Bean(name = "dataSource")
//    public DataSource dataSource() {
//        HikariDataSource ds = new HikariDataSource();
//        ds.setJdbcUrl(url);
//        ds.setUsername(username);
//        ds.setPassword(password);
//        ds.setDriverClassName(driverClassName);
//        ds.setMaximumPoolSize(maxPoolSize);
//        ds.setMinimumIdle(minIdle);
//        ds.setConnectionTimeout(connectionTimeout);
//        ds.setPoolName(poolName);
//        return ds;
//    }
//
//    @Primary
//    @Bean(name = "entityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
//            @Qualifier("dataSource") DataSource dataSource) {
//
//        LocalContainerEntityManagerFactoryBean factory =
//                new LocalContainerEntityManagerFactoryBean();
//        factory.setDataSource(dataSource);
//        factory.setPackagesToScan("com.example.scan_dineCustomer.entity", "com.example.scan_dineCustomer.restaurant.entity");
//        factory.setPersistenceUnitName("primary");
//
//        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
//        adapter.setGenerateDdl(true);
//        factory.setJpaVendorAdapter(adapter);
//        factory.setJpaPropertyMap(jpaProperties());
//        return factory;
//    }
//
//    @Primary
//    @Bean(name = "transactionManager")
//    public PlatformTransactionManager transactionManager(
//            @Qualifier("entityManagerFactory")
//            EntityManagerFactory emf) {
//        return new JpaTransactionManager(emf);
//    }
//
//    private Map<String, Object> jpaProperties() {
//        Map<String, Object> props = new HashMap<>();
//        props.put("hibernate.hbm2ddl.auto", "update");
//        props.put("hibernate.show_sql", "true");
//        props.put("hibernate.format_sql", "true");
//        return props;
//    }
//}
