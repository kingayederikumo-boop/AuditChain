package com.auditchain.config;

import com.auditchain.persistence.AuditStore;
import com.auditchain.persistence.PostgresAuditStore;
import com.auditchain.persistence.SqliteAuditStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.nio.file.Path;

@Configuration
public class AuditStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(AuditStoreConfig.class);

    @Bean
    @Primary
    @ConditionalOnProperty(name = "auditchain.store.type", havingValue = "sqlite", matchIfMissing = true)
    public AuditStore sqliteAuditStore(@Value("${auditchain.sqlite.path:data/auditchain.db}") String path) {
        log.info("Using SQLite AuditStore at {}", path);
        return new SqliteAuditStore(Path.of(path));
    }

    @Bean
    @ConditionalOnProperty(name = "auditchain.store.type", havingValue = "postgres")
    public AuditStore postgresAuditStore(DataSource dataSource) {
        log.info("Using PostgreSQL AuditStore");
        return new PostgresAuditStore(dataSource);
    }
}
