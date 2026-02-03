package co.kr.order.queue.controller;

import co.kr.order.queue.model.dto.QueueStatusInfo;
import co.kr.order.queue.service.QueueService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/queue")
public class QueueController {

    private final QueueService queueService;

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
    public QueueStatusInfo getStatus(
            HttpServletRequest servletRequest
    ) {
        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        return queueService.getStatus(userIdx);
    }

    @PostMapping("/orders/success")
    public String exitB(@RequestParam String name) {
        queueService.exitQueue(name);
        return "결제 완료 (대기열 퇴장)";
    }
}
