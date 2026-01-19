package co.kr.user.controller;

import co.kr.user.model.DTO.auth.AuthenticationReq;
import co.kr.user.model.DTO.seller.SellerRegisterDTO;
import co.kr.user.model.DTO.seller.SellerRegisterReq;
import co.kr.user.service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 판매자(Seller) 관련 API 요청을 처리하는 컨트롤러 클래스입니다.
 * 일반 회원의 판매자 등록 신청 및 등록 확인(검증) 기능을 제공합니다.
 */
@Validated // 요청 파라미터나 바디의 데이터 유효성 검증을 활성화합니다.
@RestController // RESTful API 컨트롤러임을 명시하며, 응답을 JSON 형태로 반환합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성을 주입받습니다.
@RequestMapping("/seller/users") // 이 클래스의 기본 URL 경로를 "/seller/users"로 설정합니다.
public class SellerController {
    // 판매자 관련 비즈니스 로직을 수행하는 서비스 객체입니다.
    private final SellerService sellerService;

    /**
     * 판매자 등록 신청 API
     * 회원이 판매자로 등록하기 위해 정보를 제출하는 요청을 처리합니다.
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자 (Gateway 등에서 주입)
     * @param sellerRegisterReq HTTP Body에 포함된 판매자 등록에 필요한 상세 정보 (사업자 번호 등)
     * @Valid 어노테이션을 통해 입력 데이터의 유효성을 검증합니다.
     * @return ResponseEntity<SellerRegisterDTO> 판매자 등록 신청 결과 정보를 포함한 응답 객체
     */
    @PostMapping("/register")
    public ResponseEntity<SellerRegisterDTO> sellerRegister(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                            @RequestBody @Valid SellerRegisterReq sellerRegisterReq) {
        // sellerService를 호출하여 판매자 등록 로직을 수행하고, 결과를 반환합니다.
        return ResponseEntity.ok(sellerService.sellerRegister(userIdx, sellerRegisterReq));
    }

    /**
     * 판매자 등록 확인(인증) API
     * 판매자 등록 과정에서 인증 코드 등을 통해 최종 확인을 수행하는 요청을 처리합니다.
     *
     * @param userIdx HTTP 헤더 "X-USERS-IDX"에서 추출한 사용자의 고유 식별자
     * @param authenticationReq HTTP Body에 포함된 인증 코드 정보
     * @Valid 어노테이션을 통해 인증 요청 데이터의 유효성을 검증합니다.
     * @return ResponseEntity<String> 인증 및 등록 확인 결과 메시지를 포함한 응답 객체
     */
    @PostMapping("/register/check")
    public ResponseEntity<String> sellerRegisterCheck(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                      @RequestBody @Valid AuthenticationReq authenticationReq) {
        // sellerService를 호출하여 인증 코드를 검증하고 판매자 등록을 완료/확인합니다.
        return ResponseEntity.ok(sellerService.sellerRegisterCheck(userIdx, authenticationReq.getCode()));
    }
}