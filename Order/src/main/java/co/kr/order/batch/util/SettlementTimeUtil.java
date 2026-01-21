package co.kr.order.batch.util;

import java.time.LocalDateTime;
import java.time.YearMonth;

public final class SettlementTimeUtil {

    private SettlementTimeUtil() {
    }

    public static YearMonth resolveTargetMonth(String targetMonth) {
        if (targetMonth == null || targetMonth.isBlank()) {
            return previousMonth();
        }
        return YearMonth.parse(targetMonth);
    }

    public static YearMonth previousMonth() {
        return YearMonth.now().minusMonths(1);
    }

    public static LocalDateTime startOfMonth(YearMonth yearMonth) {
        return yearMonth.atDay(1).atStartOfDay();
    }

    public static LocalDateTime endOfMonth(YearMonth yearMonth) {
        return yearMonth.atEndOfMonth().atTime(23, 59, 59, 999_999_999);
    }
}
