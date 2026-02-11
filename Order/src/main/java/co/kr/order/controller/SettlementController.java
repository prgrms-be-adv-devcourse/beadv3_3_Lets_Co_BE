package co.kr.order.controller;

import co.kr.order.model.dto.SettlementInfo;
import co.kr.order.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/settlement")
public class SettlementController {

    /**
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * todo. 정민님 주석
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */

    private final SettlementService settlementService;

    @GetMapping("/{sellerIdx}")
    public List<SettlementInfo> getSettlementList(@PathVariable("sellerIdx") Long sellerIdx) {
        return settlementService.getSettlementList(sellerIdx);
    }

    @GetMapping("/{sellerIdx}/{paymentIdx}")
    public SettlementInfo getSettlement(
            @PathVariable("sellerIdx") Long sellerIdx,
            @PathVariable("paymentIdx") Long paymentIdx
    ) {
        return settlementService.getSettlement(sellerIdx, paymentIdx);
    }
}
