package com.auditchain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Default (SQLite) path excludes JDBC/Flyway auto-config so the app starts
 * without a spring.datasource.url. Activate postgres profile to enable them.
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        FlywayAutoConfiguration.class
})
public class AuditChainApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditChainApplication.class, args);
    }
}
