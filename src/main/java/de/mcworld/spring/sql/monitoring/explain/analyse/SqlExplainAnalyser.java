package de.mcworld.spring.sql.monitoring.explain.analyse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SqlExplainAnalyser {

    private static final Pattern COST_PATTERN = Pattern.compile("cost=(.*?) rows");
    private static final String COST_SPLIT_REGEX = "\\.\\.";

    public static SqlExplainCostType analyseResult(List<String> resultList, int costMaxRef) {
        if (!resultList.isEmpty()) {
            String explainResult = resultList.get(0);
            Matcher matcher = COST_PATTERN.matcher(explainResult);
            if (matcher.find()) {
                String costStr = matcher.group(1);
                String[] costArray = costStr.split(COST_SPLIT_REGEX);
                float costMax = Float.parseFloat(costArray[1]);
                if (costMax < costMaxRef) {
                    return SqlExplainCostType.EXPLAIN_COST_OK;
                }
                return SqlExplainCostType.EXPLAIN_COST_TO_HIGH;
            }
        }
        return SqlExplainCostType.EXPLAIN_NOT_FOUND;
    }
}
