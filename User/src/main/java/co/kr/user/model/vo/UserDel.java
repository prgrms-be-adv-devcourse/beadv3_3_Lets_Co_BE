package co.kr.user.model.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserDel {
    ACTIVE(0, "정상"),
    DELETED(1, "탈퇴"),
    PENDING(2, "대기");

    private final int value;
    private final String description;

    public static UserDel fromValue(int value) {
        for (UserDel status : UserDel.values()) {
            if (status.getValue() == value) return status;
        }
        throw new IllegalArgumentException("유효하지 않은 상태값입니다: " + value);
    }
}