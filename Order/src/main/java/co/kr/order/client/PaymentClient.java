package co.kr.order.client;

import co.kr.order.model.dto.request.ClientPaymentReq;
import co.kr.order.model.dto.request.ClientRefundReq;
import co.kr.order.model.dto.response.ClientPaymentRes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "PAYMENT-SERVICE", path = "/client/payments")
public interface PaymentClient {

    @PostMapping("/process")
    ClientPaymentRes processPayment(
            @RequestBody ClientPaymentReq paymentRequest
    );

    @PostMapping("/refund")
    ClientPaymentRes refundPayment(
            @RequestBody ClientRefundReq refundRequest
    );

    @GetMapping("/order/{ordersIdx}")
    ClientPaymentRes getPayment(@PathVariable Long ordersIdx);
}