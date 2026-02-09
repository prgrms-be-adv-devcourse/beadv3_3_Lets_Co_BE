package co.kr.payment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

// 토스페이 결제 처리 테스트를 위한 임시 뷰 컨트롤러, 추후 삭제 예정

@Controller
@RequestMapping("/payment/view/payments")
public class PaymentViewController {

    @GetMapping("/checkout")
    public String checkout(Model model) {
        // 테스트용 더미 데이터
        model.addAttribute("accountCode", "test-customer-key-001");
        model.addAttribute("description", Map.of(
                "totalPrice", 50000,
                "items", List.of(
                        Map.of("code", "ITEM-001", "productName", "테스트 상품")
                )
        ));
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