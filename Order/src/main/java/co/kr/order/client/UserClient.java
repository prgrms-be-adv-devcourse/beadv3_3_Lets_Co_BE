package co.kr.order.client;

import co.kr.order.model.dto.response.SellerBulkResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/*
 * FeignClient
 * User-Service에 API 요청
 */
@FeignClient(name="user-service", path = "/client/users", url = "http://user-service:8080")
public interface UserClient {

    /**
     * 판매자 계좌 정보 Bulk 조회 (정산 배치에서 사용)
     * @param sellerIdxList 조회할 판매자 IDX 리스트
     * @return resultCode + 판매자 계좌 정보 리스트
     */
    @PostMapping("/seller")
    SellerBulkResponse getSellerDataBulk(
            @RequestBody List<Long> sellerIdxList
    );
}
