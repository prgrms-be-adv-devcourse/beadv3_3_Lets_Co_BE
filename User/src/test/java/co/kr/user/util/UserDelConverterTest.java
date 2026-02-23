package co.kr.user.util;

import co.kr.user.model.vo.UserDel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserDelConverter(java/co/kr/user/util/UserDelConverter.java 참고) 단위 테스트
 */
@DisplayName("UserDelConverter 단위 테스트")
class UserDelConverterTest {

    private final UserDelConverter converter = new UserDelConverter();

    @Test
    @DisplayName("엔티티 -> DB 컬럼 변환 테스트: Enum에 맞는 정수값이 반환되어야 함")
    void convertToDatabaseColumnTest() {
        // When & Then
        assertEquals(0, converter.convertToDatabaseColumn(UserDel.ACTIVE));
        assertEquals(1, converter.convertToDatabaseColumn(UserDel.DELETED));
        assertEquals(2, converter.convertToDatabaseColumn(UserDel.PENDING));
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    @DisplayName("DB 컬럼 -> 엔티티 변환 테스트: 정수값에 맞는 Enum이 반환되어야 함")
    void convertToEntityAttributeTest() {
        // When & Then
        assertEquals(UserDel.ACTIVE, converter.convertToEntityAttribute(0));
        assertEquals(UserDel.DELETED, converter.convertToEntityAttribute(1));
        assertEquals(UserDel.PENDING, converter.convertToEntityAttribute(2));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    @DisplayName("유효하지 않은 DB 값 변환 시 예외 발생 테스트")
    void invalidValueTest() {
        // 0, 1, 2 이외의 값이 들어오면 UserDel.fromValue()에서 예외를 던짐
        assertThrows(IllegalArgumentException.class, () -> {
            converter.convertToEntityAttribute(99);
        });
    }
}