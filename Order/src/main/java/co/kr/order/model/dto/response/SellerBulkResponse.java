package co.kr.order.model.dto.response;

import co.kr.order.model.dto.SellerInfo;

import java.util.List;

/**
 * User 서비스 Bulk 판매자 조회 응답 wrapper
 * - resultCode: 성공 여부 ("SUCCESS")
 * - data: 판매자 계좌 정보 리스트
 */
public record SellerBulkResponse(
        String resultCode,
        List<SellerInfo> data
) {}
