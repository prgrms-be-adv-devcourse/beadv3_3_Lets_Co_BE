package co.kr.order.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// 토스페이 결제 처리 테스트를 위한 임시 뷰 컨트롤러, 추후 삭제 예정

@Controller
@RequestMapping("/order/view/payments")
public class PaymentViewController {

    @GetMapping("/checkout")
    public String checkout() {
        return "payments/checkout";
    }

    @GetMapping("/success")
    public String success() {
        return "payments/success";
    }

    @GetMapping("/fail")
    public String fail() {
        return "payments/fail";
    }
}
