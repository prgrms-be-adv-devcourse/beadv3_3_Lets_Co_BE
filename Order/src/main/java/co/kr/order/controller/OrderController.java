package co.kr.order.controller;

import co.kr.order.model.dto.request.OrderReq;
import co.kr.order.model.dto.response.BaseResponse;
import co.kr.order.model.dto.response.OrderRes;
import co.kr.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    /*
     * 주문 요청 (POST)
     * @param request: 주문 요청 정보
     */
    @PostMapping
    public ResponseEntity<BaseResponse<OrderRes>> createOrder (
            HttpServletRequest servletRequest,
            @Valid @RequestBody OrderReq request
    ) {

        // 해더의 Key가 "X-USERS-IDX"인 Value 가져오기
        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        // 주문 생성
        OrderRes info = orderService.createOrder(userIdx, request);
        BaseResponse<OrderRes> body = new BaseResponse<>("ok", info);

        // 주문 정보 응답 (CREATED)
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /*
     * 주문 리스트 정보 요청 (GET)
     * @param pageable: 페이징
     */
    @GetMapping
    public ResponseEntity<BaseResponse<Page<OrderRes>>> getOrderList (
            HttpServletRequest servletRequest,
            @PageableDefault(
                    size = 10,  // 10개씩
                    sort = "createdAt",  // 생성일 기준으로
                    direction = Sort.Direction.DESC) Pageable pageable  // 내림차순
    ) {

        // 해더의 Key가 "X-USERS-IDX"인 Value 가져오기
        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        // 상품 리스트 조회
        Page<OrderRes> info = orderService.findOrderList(userIdx, pageable);
        BaseResponse<Page<OrderRes>> body = new BaseResponse<>("ok", info);

        // 상품 리스트 정보 응답 (OK)
        return ResponseEntity.ok(body);
    }

    /*
     * 주문 상세정보 요청 (GET)
     * @param orderCode: 주문 코드
     */
    @GetMapping("/{orderCode}")
    public ResponseEntity<BaseResponse<OrderRes>> getOrderDetails (
            @PathVariable("orderCode") String orderCode,
            HttpServletRequest servletRequest
    ) {

        // 해더의 Key가 "X-USERS-IDX"인 Value 가져오기
        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        // 주문 정보 조회
        OrderRes info = orderService.findOrder(userIdx, orderCode);
        BaseResponse<OrderRes> body = new BaseResponse<>("ok", info);

        // 상품 정보 응답 (OK)
        return ResponseEntity.ok(body);
    }
}
