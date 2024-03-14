# Spring Sql Monitoring

## The idea behind this API

As part of a story, we had to add a new column to a database table and adjust the business logic and select queries 
accordingly. We completed the implementation, and the unit and integration tests ran successfully.

After rolling out our release to production via CI/CD, we received a call from operations just before 
the end of the day, reporting high load on the database and decreased performance. We had to roll back to 
the previous version to analyze and address the issue more precisely.

Ultimately, it turned out that the error was due to an unadjusted database index.

Such oversights are common. When considering the number of people involved in resolving such an issue, 
the effort adds up significantly. Especially when it affects a critical business component, costs can escalate 
and lead to dissatisfied customer feedback. Additionally, there is a possibility that the current sprint goal 
may not be achieved.

The idea behind this API emerged from the necessity of detecting these issues early on.

## Description

This API is designed to help analyze SQL queries executed through Hibernate ORM and identify performance
issues in a timely manner.

Resolving performance issues discovered in production requires significant cost and effort.

The API should only be used during development or in a testing environment.

The API comprises two main features:
* Statistics
* Explain

These features can be independently enabled, disabled, and configured. Additionally, they offer various functions:
* logger
* metric

Through these functions, log and metric entries can be analyzed in a local development environment or forwarded
to other monitoring systems like ELK, Graylog, Grafana, etc., where alarms, rules, and analyzes can be defined.

## Integration

Execute maven build and add dependency in pom.xml:

```xml
    <dependency>
        <groupId>de.mcworld.spring</groupId>
        <artifactId>spring-sql-monitoring</artifactId>
        <version>0.0.1</version>
        <scope>provided</scope>
    </dependency>
```

## Statistics

This feature focuses on Hibernate Statistics. The statistics values are analyzed to detect suspicions of the N+1 problem.

Statistics runs default asynchronously in a new thread. In unit tests, the extra configuration params 
`asyncDisabled: true` or `asyncTimeout: 5000` can be used to synchronize the new thread .

### Configuration:

Configure in application.yaml:

```yaml
spring:  
  jpa:
    properties:
      hibernate:
        
        stats:
          factory: de.mcworld.spring.sql.monitoring.statistics.StatisticsLogFactory
      sql-monitoring:
        statistics:
          enabled: true
          # executionMaxTime: 50
          # cronExpression: "*/10 * 9-17 * * MON-FRI"
          functionList:
            - "logger"
            - "metric"
          #  - "analyseMaxTime"
```

* Use ´cronExpression´ to schedule the API activation.
* Use ´analyseMaxTime´ and ´executionMaxTime: 50´ to enable additional metrics, see below metrics (3,4).

### Logging:

* message: `SQL-Monitoring-Statistics`;
* mdc: `monitoring.statistics.queryExecutionMaxTimeQuery`
* mdc: `monitoring.statistics.queryExecutionMaxTime`
* mdc: `monitoring.statistics.queryExecutionCount`
* mdc: `monitoring.statistics.entityFetchCount`
* mdc: `monitoring.statistics.prepareStatementCount`
* mdc: `monitoring.statistics.nPlusOneSuspected`
* mdc: `monitoring.statistics.query_`

#### Example:
* FetchType eager:

```bash
2024-01-01 00:00:27.209  INFO 65452 --- [onPool-worker-1] .s.s.m.s.l.f.SqlStatisticsLoggerFunction : 
{monitoring.statistics.entityFetchCount=10, monitoring.statistics.nPlusOneSuspected=true, 
monitoring.statistics.prepareStatementCount=11, monitoring.statistics.queryExecutionCount=1, 
monitoring.statistics.queryExecutionMaxTime=31, monitoring.statistics.queryExecutionMaxTimeQuery=SELECT a FROM 
Customer a, monitoring.statistics.query_0=SELECT a FROM Customer a} SQL-Monitoring-Statistics
```
* Strategy join fetch:
```bash

2024-01-01 00:00:30.864  INFO 65452 --- [onPool-worker-1] .s.s.m.s.l.f.SqlStatisticsLoggerFunction : 
{monitoring.statistics.entityFetchCount=0, monitoring.statistics.nPlusOneSuspected=false, 
monitoring.statistics.prepareStatementCount=1, monitoring.statistics.queryExecutionCount=1, 
monitoring.statistics.queryExecutionMaxTime=43, 
monitoring.statistics.queryExecutionMaxTimeQuery=SELECT a FROM Customer a JOIN FETCH 
a.company b, monitoring.statistics.query_0=SELECT a FROM Customer a JOIN FETCH a.company b} SQL-Monitoring-Statistics
```
### Metrics:

1. N plus one suspected: `monitoring_statistics_nplus1_suspected_count`
2. N plus one okay: `monitoring_statistics_nplus1_ok_count`
3. Execution max time too high: `monitoring_statistics_execution_max_time_to_high_count`
4. Execution max time max okay: `monitoring_statistics_execution_max_time_ok_count`

## Explain

This feature involves SQL Explain. By using SQL Explain, execution plans can be analyzed, and performance issues can
be identified early on.

Currently, the Explain syntax is intended for `postgres` and similar databases. Extending the syntax for other databases
like `Oracle` can be easily implemented.

Explain runs asynchronously in a new thread. In unit tests, the `SqlExplainWork` interface can be used to check 
whether `ExplainService` is in progress.

### Configuration:

Configure in application.yaml:

```yaml
spring:  
  jpa:
    properties:      
      sql-monitoring:
        explain:
            enabled: true
            costMax: 1000
            database: postgres
            #  cronExpression: "*/10 * 9-17 * * MON-FRI"
            functionList:
              - "logger"
              - "metric"
            ignoreList:
              - "select nextval"
              - "select * from information_schema.sequences"
```

* Use 'costMax' the maximum execution time of a SQL query. See below logging `monitoring.explain.cost`
  and metrics (2, 3).
* Use ´cronExpression´ to schedule the API activation.
* Use ´ignoreList´ to exclude specific SQL queries from `explain`.

### Logging:

* message: `SQL-Monitoring--Explain`;
* mdc: `monitoring.explain.cost`
* mdc: `monitoring.explain.sql`
* mdc: `monitoring.explain.sql.result`
* ´
#### Example:
```bash

2024-01-01 00:00:19.882  INFO 65452 --- [lTaskScheduler1] d.m.s.s.m.e.l.f.SqlExplainLoggerFunction : 
{monitoring.explain.cost=EXPLAIN_COST_OK, monitoring.explain.sql=explain select c1_0.customer_id,
c2_0.company_id,c2_0.company_name,c1_0.country,c1_0.customer_lastname,c1_0.customer_name,
c1_0.email from public.customer c1_0 join public.company c2_0 on c2_0.company_id=c1_0.company_id, 
monitoring.explain.sql.result=[Hash Join  (cost=3.21..6.65 rows=98 width=72),   
Hash Cond: (c2_0.company_id = c1_0.company_id),   ->  
Seq Scan on company c2_0  (cost=0.00..2.66 rows=166 width=21),   -> 
 Hash  (cost=1.98..1.98 rows=98 width=55),         -> 
  Seq Scan on customer c1_0  (cost=0.00..1.98 rows=98 width=55)]} SQL-Monitoring-Explain
```
### Metrics:

1. Ignore explain, `monitoring_explain_ignore_count`
2. Cost max too high. `monitoring_explain_to_high_count`
3. Cost max okay. `monitoring_explain_ok_count`
4. Exception by explain sql. `monitoring_explain_exception_count`
5. Explain not found. `monitoring_explain_not_found_count`
6. Explain unknown. `monitoring_explain_unknown_count`
