package de.mcworld.spring.sql.monitoring.timer;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

class SqlCronTimerTest {

    private final Logger logger = LoggerFactory.getLogger(SqlCronTimer.class);

    @BeforeEach
    public void init() {
        ((ch.qos.logback.classic.Logger) logger).setLevel(Level.DEBUG);
    }

    @Test
    void isNextDateTime() {
        LocalDateTime testTime = LocalDateTime.of(2023, 1, 13, 9, 0, 0);
        LocalDateTime testTime2 = LocalDateTime.of(2023, 1, 13, 10, 0, 5);
        LocalDateTime testTime3 = LocalDateTime.of(2023, 1, 12, 9, 0, 20);
        LocalDateTime testTime4 = LocalDateTime.of(2023, 1, 12, 8, 0, 20);
        LocalDateTime testTime5 = LocalDateTime.of(2023, 1, 14, 9, 0, 20);
        LocalDateTime testTime6 = LocalDateTime.of(2023, 1, 16, 18, 0, 0);

        SqlCronTimer sqlCronTimer = SqlCronTimer.parse("*/10 * 9-18 * * MON-FRI");
        Assertions.assertTrue(sqlCronTimer.isNextDateTime(testTime, SqlCronTimer.NEXT_FUNCTION));
        Assertions.assertFalse(sqlCronTimer.isNextDateTime(testTime2, SqlCronTimer.NEXT_FUNCTION));
        Assertions.assertTrue(sqlCronTimer.isNextDateTime(testTime3, SqlCronTimer.NEXT_FUNCTION));
        Assertions.assertFalse(sqlCronTimer.isNextDateTime(testTime4, SqlCronTimer.NEXT_FUNCTION));
        Assertions.assertFalse(sqlCronTimer.isNextDateTime(testTime5, SqlCronTimer.NEXT_FUNCTION));
        Assertions.assertTrue(sqlCronTimer.isNextDateTime(testTime6, SqlCronTimer.NEXT_FUNCTION));
    }
}