package org.lamisplus.modules.dqr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

// CBS reads many clinical tables in bulk and writes its own cbs_* tables in batches,
// which is simpler and much faster with plain JDBC than with JPA entities.
@Configuration
@EnableScheduling
public class CbsConfig {

    @Bean(name = "cbsJdbcTemplate")
    public NamedParameterJdbcTemplate cbsJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean(name = "cbsTransactionTemplate")
    public TransactionTemplate cbsTransactionTemplate(DataSource dataSource) {
        return new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    }
}
