package co.kr.user.model.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 계정 및 관련 데이터의 논리적 삭제 상태를 관리하는 Enum 클래스입니다.
 * 데이터베이스에는 정수값으로 저장되며, 서비스 로직에서는 Enum으로 처리됩니다.
 */
@Getter
@RequiredArgsConstructor
public enum UserDel {
    /** 정상 이용 중인 상태 (DB값: 0) */
    ACTIVE(0, "정상"),
    /** 탈퇴 처리가 완료된 상태 (DB값: 1) */
    DELETED(1, "탈퇴"),
    /** 회원가입 후 이메일 인증 등을 기다리는 상태 (DB값: 2) */
    PENDING(2, "대기");

    /** 데이터베이스에 저장되는 정수 값 */
    private final int value;
    /** 상태에 대한 설명 문구 */
    private final String description;

    /**
     * 데이터베이스의 정수 값을 받아 해당하는 UserDel Enum을 반환합니다.
     * @param value DB에서 조회한 정수 값
     * @return 매칭되는 UserDel Enum
     * @throws IllegalArgumentException 유효하지 않은 값이 들어올 경우 발생
     */
    public static UserDel fromValue(int value) {
        for (UserDel status : UserDel.values()) {
            if (status.getValue() == value) return status;
        }
        throw new IllegalArgumentException("유효하지 않은 상태값입니다: " + value);
    }
}