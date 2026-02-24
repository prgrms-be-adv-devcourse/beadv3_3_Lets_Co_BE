package co.kr.user.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CryptoConverter(java/co/kr/user/util/CryptoConverter.java) 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CryptoConverter 단위 테스트")
class CryptoConverterTest {

    @Mock
    private AESUtil aesUtil;

    @InjectMocks
    private CryptoConverter.DeterministicConverter cryptoConverter;

    @Test
    @DisplayName("데이터베이스 저장 전 암호화 테스트")
    void convertToDatabaseColumnTest() {
        // Given
        String plainText = "my-secret-data";
        String encryptedText = "encrypted-hash-value";
        when(aesUtil.CBCencrypt(plainText)).thenReturn(encryptedText);

        // When
        String result = cryptoConverter.convertToDatabaseColumn(plainText);

        // Then
        assertEquals(encryptedText, result);
        verify(aesUtil).CBCencrypt(plainText);
    }

    @Test
    @DisplayName("데이터베이스 조회 후 복호화 테스트")
    void convertToEntityAttributeTest() {
        // Given
        String dbData = "encrypted-hash-value";
        String decryptedText = "my-secret-data";
        when(aesUtil.CBCdecrypt(dbData)).thenReturn(decryptedText);

        // When
        String result = cryptoConverter.convertToEntityAttribute(dbData);

        // Then
        assertEquals(decryptedText, result);
        verify(aesUtil).CBCdecrypt(dbData);
    }
}