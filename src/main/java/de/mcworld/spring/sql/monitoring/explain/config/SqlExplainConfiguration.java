package de.mcworld.spring.sql.monitoring.explain.config;

import lombok.extern.log4j.Log4j2;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.sql.DataSource;

@Log4j2
@Configuration
@EnableAsync
@ConditionalOnProperty("spring.jpa.properties.sql-monitoring.explain.enabled")
public class SqlExplainConfiguration {

    public static final String EXPLAIN_PROXY_DATA_SOURCE = "Explain Proxy DataSource";

    @Bean
    public DataSource getDataSource(DataSourceProperties dataSourceProperties,
                                    ProxyDataSourceBuilder.SingleQueryExecution queryExecution) {
        DataSource originalDatasource = dataSourceProperties.initializeDataSourceBuilder().build();
        return ProxyDataSourceBuilder.create(originalDatasource)
                .name(EXPLAIN_PROXY_DATA_SOURCE)
                //.logQueryByCommons(CommonsLogLevel.DEBUG)
                .afterQuery(queryExecution)
                .build();
    }
}
