package co.kr.order.client;

import co.kr.order.model.dto.request.ClientRefundReq;
import co.kr.order.model.dto.response.ClientPaymentRes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/*
 * FeignClient
 * Payment-Service에 API 요청
 */
@FeignClient(name = "payment-service", path = "/client/payments", url = "http://payment-service:8080")
public interface PaymentClient {

    /*
     * 환불 요청 (POST)
     * @param refundRequest: 환불 요청 정보
     */
    @PostMapping("/refund")
    ClientPaymentRes refundPayment(
            @RequestBody ClientRefundReq refundRequest
    );

}