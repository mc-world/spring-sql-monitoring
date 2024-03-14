package de.mcworld.spring.sql.monitoring.explain.analyse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SqlExplainCostType {

    EXPLAIN_IGNORE(0, "Ignore explain.", "monitoring_explain_ignore_count"),
    EXPLAIN_COST_TO_HIGH(1, "Cost max too high.", "monitoring_explain_to_high_count"),
    EXPLAIN_COST_OK(2, "Cost max okay.", "monitoring_explain_ok_count"),
    EXPLAIN_EXCEPTION(3, "Exception by explain sql!!!", "monitoring_explain_exception_count"),
    EXPLAIN_NOT_FOUND(4, "Explain not found.", "monitoring_explain_not_found_count"),
    EXPLAIN_UNKNOWN(4, "Explain unknown.", "monitoring_explain_unknown_count");

    private final int code;
    private final String description;
    private final String metric;
}
