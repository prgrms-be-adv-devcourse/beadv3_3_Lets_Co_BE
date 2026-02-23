package co.kr.order.controller;

import co.kr.order.model.redis.WaitingQueue;
import co.kr.order.service.QueueService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/queue")
public class QueueController {

    private final QueueService queueService;

    /*
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * 입장 대기열 (Rate Limiter)
     * 1초마다 N명씩 접근 가능
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */

    // 입장 대기열 등록 (POST)
    @PostMapping("/enter/register")
    public String enterQueue(
            HttpServletRequest servletRequest
    ) {
        // 해더의 Key가 "X-USERS-IDX"인 Value 가져오기
        String userIdx = servletRequest.getHeader("X-USERS-IDX");

        // 회원일 경우 토큰을 userIdx로 설정
        String queueToken;
        if (userIdx != null && !userIdx.isBlank()) {
            queueToken = userIdx;
        }
        else {
            // 비회원일 경우 토큰을 랜덤 UUID 값으로 설정
            queueToken = UUID.randomUUID().toString();
        }

        // 입장 대기열 등록
        queueService.registerEnter(queueToken);

        // 토큰 반환
        return queueToken;
    }

    // 대기열 번호 요청 (GET)
    @GetMapping("/enter/status")
    public WaitingQueue getEnterStatus(
            // Header의 Key가 "X-QUEUE-TOKEN"
            @RequestHeader("X-QUEUE-TOKEN") String queueToken
    ) {
        // 대기열 번호 조회 후 응답
        return queueService.getEnterStatus(queueToken);
    }


    /*
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * 주문 대기열 (Capacity Limiter)
     * 빈자리가 나야지 입장
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */

    // 주문 대기열 등록 (POST)
    @PostMapping("/orders/enter")
    public String orderQueue(
            HttpServletRequest servletRequest
    ) {
        // 해더의 Key가 "X-USERS-IDX"인 Value 가져오기
        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        // 주문 대기열 등록
        queueService.registerOrder(userIdx);

        // 메시지 응답
        return "주문 대기열 진입";
    }

    // 대기열 번호 요청 (GET)
    @GetMapping("/orders/status")
    public WaitingQueue getOrderStatus(
            HttpServletRequest servletRequest
    ) {
        // 해더의 Key가 "X-USERS-IDX"인 Value 가져오기
        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        // 대기열 번호 조회 후 응답
        return queueService.getOrderStatus(userIdx);
    }

    // 주문 완료 시 대기열 퇴장
    @PostMapping("/orders/success")
    public String exitOrder(
            HttpServletRequest servletRequest
    ) {
        // 해더의 Key가 "X-USERS-IDX"인 Value 가져오기
        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        // 대기열 퇴장
        queueService.exitQueue(userIdx);

        // 메시지 응답
        return "결제 완료 (대기열 퇴장)";
    }
}
