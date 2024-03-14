package de.mcworld.spring.sql.monitoring.explain.config;

import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "spring.jpa.properties.sql-monitoring.explain")
@ConditionalOnProperty("spring.jpa.properties.sql-monitoring.explain.enabled")
@Data
@Valid
public class SqlExplainConfigurationProperties {

    private int costMax;
    private boolean asyncEnabled;
    private long asyncTimeout;
    private List<String> ignoreList;
    private String cronExpression;
    private List<String> functionList;
}
