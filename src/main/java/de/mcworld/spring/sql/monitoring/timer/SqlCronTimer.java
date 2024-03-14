package de.mcworld.spring.sql.monitoring.timer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.BiPredicate;

@Slf4j
public class SqlCronTimer {

    protected static final BiPredicate<LocalDateTime, CronExpression> NEXT_FUNCTION = (dateTime, dateTimeExpression) -> {
        LocalDateTime truncatedTime = dateTime.truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime truncatedExpressionOffset = truncatedTime.minusNanos(1L);
        LocalDateTime nextExpression = dateTimeExpression.next(truncatedExpressionOffset);
        log.debug("Current time [{}], next [{}] matches expression [{}]: [{}]", truncatedTime,
                nextExpression, dateTimeExpression, truncatedTime.equals(nextExpression));
        return truncatedTime.equals(nextExpression);
    };
    protected static final BiPredicate<LocalDateTime, CronExpression> MOCK_NEXT_FUNCTION =
            (dateTime, dateTimeExpression) -> true;
    private BiPredicate<LocalDateTime, CronExpression> nextFunction = MOCK_NEXT_FUNCTION;
    private CronExpression expression = null;

    public SqlCronTimer(String cronExpression) {
        if (StringUtils.hasLength(cronExpression)) {
            expression = CronExpression.parse(cronExpression);
            nextFunction = NEXT_FUNCTION;
        }
    }

    public static SqlCronTimer parse(String cronExpression) {
        return new SqlCronTimer(cronExpression);
    }

    public boolean isCurrentNextDateTime() {
        return isNextDateTime(LocalDateTime.now(), nextFunction);
    }

    public boolean isNextDateTime(LocalDateTime dateTime,
                                  BiPredicate<LocalDateTime, CronExpression> isNextFunction) {
        return isNextFunction.test(dateTime, expression);
    }
}
