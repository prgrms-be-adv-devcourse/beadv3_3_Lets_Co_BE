package co.kr.order.controller;

import co.kr.order.model.dto.SettlementInfo;
import co.kr.order.model.dto.response.BaseResponse;
import co.kr.order.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<BaseResponse<List<SettlementInfo>>> getSettlementList(
            @PathVariable("sellerIdx") Long sellerIdx) {
        List<SettlementInfo> info = settlementService.getSettlementList(sellerIdx);
        BaseResponse<List<SettlementInfo>> body = new BaseResponse<>("ok", info);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{sellerIdx}/{paymentIdx}")
    public ResponseEntity<BaseResponse<SettlementInfo>> getSettlement(
            @PathVariable("sellerIdx") Long sellerIdx,
            @PathVariable("paymentIdx") Long paymentIdx
    ) {
        SettlementInfo info = settlementService.getSettlement(sellerIdx, paymentIdx);
        BaseResponse<SettlementInfo> body = new BaseResponse<>("ok", info);
        return ResponseEntity.ok(body);
    }
}
