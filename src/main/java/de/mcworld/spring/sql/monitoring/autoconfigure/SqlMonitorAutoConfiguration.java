package de.mcworld.spring.sql.monitoring.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ConditionalOnProperty("spring.jpa.properties.sql-monitoring.explain.enabled")
@ComponentScan(basePackages = {"de.mcworld.spring.sql.monitoring"})
public class SqlMonitorAutoConfiguration {

}
