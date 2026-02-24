package co.kr.product.product.model.dto.response;

import org.aspectj.apache.bcel.classfile.Code;

public record ImageInfoRes(
        // Code의 역할을 대신 storedFileName로 사용
        String storedFileName,
        String urls
) {
}
