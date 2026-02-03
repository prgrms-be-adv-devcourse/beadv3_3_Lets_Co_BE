package co.kr.order.queue.controller;

import co.kr.order.queue.model.dto.QueueStatusInfo;
import co.kr.order.queue.service.QueueService;
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
     * 입장 대기열 (Rate Limiter)
     * - 1초마다 N명씩 접근 가능
     */
    @GetMapping("/enter/register")
    public String enterQueue(
            HttpServletRequest servletRequest
    ) {
        String queueToken;
        String userIdx = servletRequest.getHeader("X-USERS-IDX");

        if (userIdx != null && !userIdx.isBlank()) {
            queueToken = userIdx;
        }
        else {
            queueToken = UUID.randomUUID().toString();
        }

        queueService.registerEnter(queueToken);

        return queueToken;
    }

    @GetMapping("/enter/status")
    public QueueStatusInfo getEnterStatus(
            @RequestHeader("X-QUEUE-TOKEN") String queueToken
    ) {
        return queueService.getEnterStatus(queueToken);
    }


    /*
     * 주문 대기열 (Capacity Limiter)
     * - 빈자리가 나야 입장
     */
    @GetMapping("/orders/enter")
    public String orderQueue(
            HttpServletRequest servletRequest
    ) {
        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        queueService.registerOrder(userIdx);

        return "주문 대기열 진입";
    }

    @GetMapping("/orders/status")
    public QueueStatusInfo getOrderStatus(
            HttpServletRequest servletRequest
    ) {
        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        return queueService.getOrderStatus(userIdx);
    }

    @PostMapping("/orders/success")
    public String exitOrder(
            HttpServletRequest servletRequest
    ) {
        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        queueService.exitQueue(userIdx);
        return "결제 완료 (대기열 퇴장)";
    }
}
