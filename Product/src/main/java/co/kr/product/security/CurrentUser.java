package co.kr.product.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public final class CurrentUser {

    private CurrentUser() {
        // util class
    }

    /**
     * 로그인된 사용자의 userIdx(User_IDX)를 반환
     * - 인증되지 않았으면 IllegalStateException
     * - JWT 파싱 구조가 달라도 여기만 수정하면 됨
     */
    public static Long userIdxOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }

        Object principal = authentication.getPrincipal();

        /*
         * 🔹 케이스 1: principal 자체가 userIdx(Long)인 경우
         * (간단한 JWT 구현에서 종종 사용)
         */
        if (principal instanceof Long) {
            return (Long) principal;
        }

        /*
         * 🔹 케이스 2: principal이 String(userIdx)인 경우
         */
        if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("유효하지 않은 사용자 정보입니다.");
            }
        }

        /*
         * 🔹 케이스 3: principal이 Map 형태 (JWT Claims를 그대로 넣은 경우)
         * 예: {userIdx=1, role=USER, email=...}
         */
        if (principal instanceof Map<?, ?> map) {
            Object userIdx = map.get("userIdx");
            if (userIdx instanceof Number) {
                return ((Number) userIdx).longValue();
            }
        }

        /*
         * 🔹 케이스 4: CustomUserDetails 사용 시
         * (인증 담당자가 만든 UserDetails에 getUserIdx()가 있을 경우)
         *
         * 예:
         * if (principal instanceof CustomUserDetails cud) {
         *     return cud.getUserIdx();
         * }
         */

        throw new IllegalStateException("현재 사용자 정보를 확인할 수 없습니다.");
    }

    /**
     * 로그인 여부만 확인하고 싶을 때 사용 (비회원 허용 API)
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}

