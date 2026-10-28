package com.auditchain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Default (SQLite) path excludes JDBC auto-configuration so the app starts
 * without a spring.datasource.url. The PostgreSQL profile supplies its own
 * DataSource and enables Flyway migrations.
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class
})
public class AuditChainApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditChainApplication.class, args);
    }
}
