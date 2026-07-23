package com.example.scan_dineCustomer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
// Scan both the original package and the new com.jarvis.aiagent package
@ComponentScan(basePackages = {
        "com.example.scan_dineCustomer",
        "com.jarvis.aiagent"
})
@ConfigurationPropertiesScan(basePackages = {"com.example.scan_dineCustomer", "com.jarvis.aiagent"})
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
