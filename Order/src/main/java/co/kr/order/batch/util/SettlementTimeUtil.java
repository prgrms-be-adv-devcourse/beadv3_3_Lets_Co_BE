package co.kr.order.batch.util;

import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * 정산 배치에서 사용하는 월 기준 시간 계산 유틸리티.
 * 기본 정책:
 *  targetMonth가 null 또는 blank인 경우 전월을 기본값으로 사용한다.
 *   조회 범위는 월 시작(포함) ~ 월 종료(포함) 기준이다.
 */
public final class SettlementTimeUtil {

    private SettlementTimeUtil() {
    }

    /**
     * targetMonth(yyyy-MM)를 YearMonth로 변환한다.
     * 값이 없으면 전월을 반환한다.
     */
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

    /** 해당 월의 종료 시각 (말일 23:59:59.999999999) */
    public static LocalDateTime endOfMonth(YearMonth yearMonth) {
        return yearMonth.atEndOfMonth().atTime(23, 59, 59, 999_999_999);
    }
}
